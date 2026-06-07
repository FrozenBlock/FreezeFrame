/*
 * Copyright 2026 FrozenBlock
 * This file is part of Freeze Frame.
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

package net.frozenblock.freezeframe.client.screenshot;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.scope.ScopePostEffectController;
import net.frozenblock.freezeframe.client.scope.ScopeZoomManager;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.frozenblock.lib.networking.FrozenNetworking;
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
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

@Environment(EnvType.CLIENT)
public class CameraScreenshotManager {
	private static final String PLAYER_UUID = Minecraft.getInstance().getGameProfile().id().toString();
	private static final ScreenshotData SCREENSHOT_DATA = new ScreenshotData();

	public static ScreenshotData screenshotData() {
		return SCREENSHOT_DATA;
	}

	public static void resetScreenshotDataAndRestorePrevious(Minecraft minecraft) {
		SCREENSHOT_DATA.reset(minecraft);
	}

	public static void executeScreenshot(@Nullable Entity entity, boolean handheldCapture, boolean wasScoping, @Nullable String fileName, float zoom, FilmFilter filter) {
		final Minecraft minecraft = Minecraft.getInstance();
		SCREENSHOT_DATA.startScreenshotting(handheldCapture);
		SCREENSHOT_DATA.extractPreHideGuiAndSetHidden(minecraft);

		final int resolution = Math.clamp(FFConfig.PHOTOGRAPH_RESOLUTION.get(), 128, 1024);
		if (handheldCapture) ScopeZoomManager.pushForcedZoom(zoom);
		grabCameraScreenshot(minecraft.gameDirectory, resolution, resolution, fileName, filter, entity);

		makeSnapSoundAndSmoke: {
			if (minecraft.level == null) break makeSnapSoundAndSmoke;

			final Entity camEntity = entity != null ? entity : minecraft.getCameraEntity();
			if (camEntity == null) break makeSnapSoundAndSmoke;

			final int smokeCount = minecraft.level.getRandom().nextInt(1, 5);
			for (int i = 0; i < smokeCount; i++) {
				minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE, camEntity.getX(), camEntity.getEyeY(), camEntity.getZ(), 0D, 0.15D, 0D);
			}
		}

		resetScreenshotDataAndRestorePrevious(minecraft);

		if (handheldCapture) ScopeZoomManager.clearForcedZoom();
		if (handheldCapture && wasScoping && !filter.isEmpty()) ScopePostEffectController.applyFromFilter(minecraft, filter);
	}

	public static void grabCameraScreenshot(File workDir, int width, int height, @Nullable String fileName, FilmFilter filter, @Nullable Entity entity) {
		final Minecraft minecraft = Minecraft.getInstance();

		final Window window = minecraft.getWindow();
		SCREENSHOT_DATA.extractPreWindowResolution(window);
		SCREENSHOT_DATA.createAndSetRenderTarget(width, height);

		final GameRenderer gameRenderer = minecraft.gameRenderer;
		final Camera camera = gameRenderer.getMainCamera();
		gameRenderer.setRenderBlockOutline(false);
		if (entity != null) SCREENSHOT_DATA.extractPreCameraEntity(camera);

		try {
			camera.enablePanoramicMode();
			window.setWidth(width);
			window.setHeight(height);

			if (entity != null) {
				SCREENSHOT_DATA.extractPreEyeHeight(camera);
				camera.setEntity(entity);
			}

			ScopePostEffectController.applyFromFilter(minecraft, filter);

			gameRenderer.update(DeltaTracker.ONE, true);
			if (entity != null) SCREENSHOT_DATA.extractEnvironmentAttributeProbeAt(minecraft.level, camera.position());
			gameRenderer.extract(DeltaTracker.ONE, true);
			gameRenderer.render(DeltaTracker.ONE, true);

			grab(workDir, fileName, SCREENSHOT_DATA.getRenderTarget(), text -> minecraft.execute(() -> minecraft.gui.getChat().addClientSystemMessage(text)));
		} catch (Exception ignored) {} finally {
			ScopePostEffectController.clearIfApplied(minecraft);
			gameRenderer.setRenderBlockOutline(true);
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

	public static class ScreenshotData {
		private boolean screenshotting = false;
		private Boolean handheld = false;
		private RenderTarget renderTarget = null;
		private EnvironmentAttributeProbe environmentAttributeProbe = null;

		ScreenshotData() {}

		public boolean screenshotting() {
			return this.screenshotting;
		}

		public void startScreenshotting(boolean handheld) {
			this.screenshotting = true;
			this.handheld = handheld;
		}

		public boolean screenshottingAndTripod() {
			return this.screenshotting() && !this.handheld;
		}

		public boolean screenshottingAndHandheld() {
			return this.screenshotting() && this.handheld;
		}

		public boolean notScreenshottingOrIsTripod() {
			return !this.screenshotting() || this.screenshottingAndHandheld();
		}

		public void createAndSetRenderTarget(int width, int height) {
			this.renderTarget = new TextureTarget("photograph", width, height, true);
		}

		@Nullable
		public RenderTarget getRenderTarget() {
			return this.renderTarget;
		}

		public void extractEnvironmentAttributeProbeAt(Level level, Vec3 pos) {
			this.environmentAttributeProbe = new EnvironmentAttributeProbe();
			this.environmentAttributeProbe.tick(level, pos);
		}

		@Nullable
		public EnvironmentAttributeProbe environmentAttributeProbe() {
			return this.environmentAttributeProbe;
		}

		// Pre-screenshot data
		private Entity preCameraEntity = null;
		private Pair<Float, Float> preEyeHeight = null;
		private boolean preHideGui = false;
		private Pair<Integer, Integer> preResolution = null;

		public void extractPreCameraEntity(Camera camera) {
			this.preCameraEntity = camera.entity();
		}

		public void extractPreEyeHeight(Camera camera) {
			this.preEyeHeight = Pair.of(camera.eyeHeightOld, camera.eyeHeight);
		}

		public void extractPreHideGuiAndSetHidden(Minecraft minecraft) {
			this.preHideGui = minecraft.options.hideGui;
			minecraft.options.hideGui = true;
		}

		public void extractPreWindowResolution(Window window) {
			this.preResolution = Pair.of(window.getWidth(), window.getHeight());
		}

		public void restorePreScreenshotData(Minecraft minecraft) {
			minecraft.options.hideGui = this.preHideGui;
			if (this.preResolution != null) {
				minecraft.getWindow().setWidth(this.preResolution.getFirst());
				minecraft.getWindow().setHeight(this.preResolution.getSecond());
			}
			if (this.preEyeHeight != null) {
				final Camera camera = minecraft.gameRenderer.getMainCamera();
				camera.eyeHeightOld = this.preEyeHeight.getFirst();
				camera.eyeHeight = this.preEyeHeight.getSecond();
			}
			minecraft.setCameraEntity(this.preCameraEntity);
		}

		public void reset(Minecraft minecraft) {
			this.screenshotting = false;
			this.handheld = false;
			this.renderTarget = null;
			this.environmentAttributeProbe = null;

			this.restorePreScreenshotData(minecraft);
			this.preEyeHeight = null;
			this.preCameraEntity = null;
		}
	}
}
