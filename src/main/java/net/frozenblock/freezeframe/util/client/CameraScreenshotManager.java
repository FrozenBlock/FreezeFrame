/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
 *
 * This program is free software; you can modify it under
 * the terms of version 1 of the FrozenBlock Modding Oasis License
 * as published by FrozenBlock Modding Oasis.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * FrozenBlock Modding Oasis License for more details.
 *
 * You should have received a copy of the FrozenBlock Modding Oasis License
 * along with this program; if not, see <https://github.com/FrozenBlock/Licenses>.
 */

package net.frozenblock.freezeframe.util.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.frozenblock.lib.networking.FrozenNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

@Environment(EnvType.CLIENT)
public class CameraScreenshotManager {
	private static final String PLAYER_UUID = Minecraft.getInstance().getGameProfile().id().toString();

	private static boolean possessingCamera = false;
	private static boolean isCameraHandheld = false;
	private static RenderTarget renderTarget = null;

	public static boolean isScreenshotting() {
		return CameraScreenshotManager.possessingCamera;
	}

	public static boolean isScreenshottingFromTripodCamera() {
		return isScreenshotting() && !isCameraHandheld;
	}

	public static boolean isScreenshottingFromHandheldCamera() {
		return isScreenshotting() && isCameraHandheld;
	}

	public static void executeScreenshot(@Nullable Entity entity, boolean handheldCapture, @Nullable String fileName, float zoom) {
		final Minecraft minecraft = Minecraft.getInstance();
		isCameraHandheld = handheldCapture;
		if (handheldCapture) {
			ScopeZoomManager.pushForcedZoom(zoom);
		}
		final Entity previousCameraEntity = minecraft.getCameraEntity();
		if (entity != null) minecraft.setCameraEntity(entity);

		final boolean wasGuiHidden = minecraft.options.hideGui;
		minecraft.options.hideGui = true;
		possessingCamera = true;

		final int resolution = Math.clamp(FFConfig.PHOTOGRAPH_RESOLUTION.get(), 128, 1024);
		grabCameraScreenshot(minecraft.gameDirectory, resolution, resolution, fileName, !handheldCapture);

		makeSnapSoundAndSmoke: {
			if (minecraft.level == null) break makeSnapSoundAndSmoke;

			final Entity camEntity = entity != null ? entity : minecraft.getCameraEntity();
			if (camEntity == null) break makeSnapSoundAndSmoke;

			final int smokeCount = minecraft.level.getRandom().nextInt(1, 5);
			for (int i = 0; i < smokeCount; i++) {
				minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE, camEntity.getX(), camEntity.getEyeY(), camEntity.getZ(), 0D, 0.15D, 0D);
			}
		}

		if (previousCameraEntity != null) minecraft.setCameraEntity(previousCameraEntity);

		minecraft.options.hideGui = wasGuiHidden;
		possessingCamera = false;
		isCameraHandheld = false;
		if (handheldCapture) ScopeZoomManager.clearForcedZoom();
	}

	public static void grabCameraScreenshot(File workDir, int width, int height, @Nullable String fileName, boolean handheldCapture) {
		final Minecraft minecraft = Minecraft.getInstance();
		final Window window = minecraft.getWindow();
		final int prevWidth = window.getWidth();
		final int prevHeight = window.getHeight();
		final GameRenderer gameRenderer = minecraft.gameRenderer;
		final Camera camera = gameRenderer.getMainCamera();
		renderTarget = new TextureTarget("photograph", width, height, true);
		gameRenderer.setRenderBlockOutline(false);

		try {
			camera.enablePanoramicMode();
			window.setWidth(width);
			window.setHeight(height);
			gameRenderer.update(DeltaTracker.ONE, true);
			gameRenderer.extract(DeltaTracker.ONE, true);
			if (handheldCapture) {
				gameRenderer.render(DeltaTracker.ONE, true);
			} else {
				gameRenderer.renderLevel(DeltaTracker.ONE);
			}
			grab(workDir, fileName, renderTarget, (text) -> minecraft.execute(() -> minecraft.gui.getChat().addClientSystemMessage(text)));
		} catch (Exception ignored) {
		} finally {
			gameRenderer.setRenderBlockOutline(true);
			window.setWidth(prevWidth);
			window.setHeight(prevHeight);
			renderTarget = null;
			camera.disablePanoramicMode();
		}
	}

	private static void grab(File workDir, @Nullable String fileName, RenderTarget target, Consumer<Component> callback) {
		Screenshot.takeScreenshot(target, screenshot -> {
			final Minecraft minecraft = Minecraft.getInstance();

			Optional<Path> iconPath = Optional.empty();
			if (FFConfig.USE_LATEST_PHOTO_AS_WORLD_ICON.get() && FrozenNetworking.connectedToIntegratedServer()) {
				iconPath = minecraft.getSingleplayerServer().getWorldScreenshotFile();
				iconPath.ifPresent(path -> path.toFile().mkdirs());
			}

			final File photographFile = resolvePhotographFile(workDir, fileName);
			Optional<Path> finalIconPath = iconPath;

			Util.ioPool().execute(() -> {
				try {
					screenshot.writeToFile(photographFile);
					finalIconPath.ifPresent(path -> copyPhotographToFileWithSize(screenshot, path, 64, 64));

					callback.accept(
						Component.translatable("photograph.success")
							.withStyle(ChatFormatting.UNDERLINE)
							.withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(photographFile.getAbsoluteFile())))
					);
					sendToServer:{
						if (StringUtil.isNullOrEmpty(fileName) || FrozenNetworking.connectedToIntegratedServer()) break sendToServer;

						final ClientPacketListener connection = minecraft.getConnection();
						if (connection == null) break sendToServer;

						try {
							FileTransferPacket.create("photographs", photographFile).forEach(
								packet -> connection.send(new ServerboundCustomPayloadPacket(packet))
							);
						} catch (Exception e) {
							FFConstants.error("Unable to send photograph to server", e);
						}
					}
				} catch (Exception e) {
					FFConstants.warn("Couldn't save screenshot " + e, true);
					callback.accept(Component.translatable("photograph.failure", e.getMessage()));
				} finally {
					screenshot.close();
				}
			});
		});
	}

	private static void copyPhotographToFileWithSize(NativeImage image, Path path, int width, int height) {
		int sourceWidth = image.getWidth();
		int sourceHeight = image.getHeight();
		int newX = 0;
		int newY = 0;
		if (sourceWidth > sourceHeight) {
			newX = (sourceWidth - sourceHeight) / 2;
			sourceWidth = sourceHeight;
		} else {
			newY = (sourceHeight - sourceWidth) / 2;
			sourceHeight = sourceWidth;
		}

		try (NativeImage scaled = new NativeImage(width, height, false)) {
			image.resizeSubRectTo(newX, newY, sourceWidth, sourceHeight, scaled);
			scaled.writeToFile(path);
		} catch (IOException e) {
			FFConstants.LOGGER.warn("Couldn't save photograph as icon", e);
		}
	}

	private static File resolvePhotographFile(File workDir, @Nullable String fileName) {
		final File photographPath = workDir.toPath()
			.resolve("photographs")
			.resolve(".local")
			.toFile();
		photographPath.mkdirs();

	 	return StringUtil.isNullOrEmpty(fileName)
			? getPhotographFile(PLAYER_UUID, photographPath)
			: getPhotographFile(fileName, photographPath);
	}

	public static File getPhotographFile(String fileName, File directory) {
		int fileIndex = 1;
		while (true) {
			File file = new File(directory, fileName + (fileIndex == 1 ? "" : "_" + fileIndex) + "." + FFConfig.PHOTOGRAPH_FORMAT.get().extension());
			if (!file.exists()) return file;
			++fileIndex;
		}
	}

	@Nullable
	public static RenderTarget getRenderTarget() {
		return renderTarget;
	}
}
