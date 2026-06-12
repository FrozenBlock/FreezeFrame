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

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.frozenblock.lib.networking.FrozenNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
public class PhotographScreenshotter {
	private static final String PLAYER_UUID = Minecraft.getInstance().getGameProfile().id().toString();

	public static void executeScreenshot(@Nullable Entity entity, boolean handheldCapture, boolean wasScoping, @Nullable String fileName, float zoom, FilmFilter filter) {
		final Minecraft minecraft = Minecraft.getInstance();
		FFScreenshotUtil.executeScreenshot(
			entity,
			handheldCapture,
			wasScoping,
			zoom,
			filter,
			Math.clamp(FFConfig.PHOTOGRAPH_RESOLUTION.get(), 128, 1024),
			screenshot -> saveAndSendPhotograph(
				screenshot,
				minecraft,
				fileName,
				entity,
				text -> minecraft.execute(() -> minecraft.gui.getChat().addClientSystemMessage(text))
			)
		);
	}

	private static void saveAndSendPhotograph(NativeImage screenshot, Minecraft minecraft, @Nullable String fileName, @Nullable Entity entity, Consumer<Component> callback) {
		makeSnapSoundAndSmoke: {
			if (entity == null) break makeSnapSoundAndSmoke;

			final int smokeCount = entity.getRandom().nextInt(1, 5);
			for (int i = 0; i < smokeCount; i++) {
				minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getEyeY(), entity.getZ(), 0D, 0.15D, 0D);
			}
		}

		Optional<Path> iconPath = Optional.empty();
		if (FFConfig.USE_LATEST_PHOTO_AS_WORLD_ICON.get() && FrozenNetworking.connectedToIntegratedServer()) {
			iconPath = minecraft.getSingleplayerServer().getWorldScreenshotFile();
			iconPath.ifPresent(path -> path.toFile().mkdirs());
		}

		final File photographFile = resolvePhotographFile(minecraft.gameDirectory, fileName);
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
}
