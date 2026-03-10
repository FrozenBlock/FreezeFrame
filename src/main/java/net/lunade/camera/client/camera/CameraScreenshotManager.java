package net.lunade.camera.client.camera;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.CameraPortMain;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

@Environment(EnvType.CLIENT)
public class CameraScreenshotManager {
	private static final String PLAYER_UUID = Minecraft.getInstance().getGameProfile().id().toString();

	private static boolean wasGuiHidden = false;
	private static boolean possessingCamera = false;
	private static boolean isCameraHandheld = false;
	@Nullable
	public static Entity previousCameraEntity = null;

	public static boolean isPossessingCamera() {
		return CameraScreenshotManager.possessingCamera;
	}

	public static boolean isUsingSelfRenderingCamera() {
		return isPossessingCamera() && !isCameraHandheld;
	}

	public static boolean isUsingHandheldCamera() {
		return isPossessingCamera() && isCameraHandheld;
	}

	public static void executeScreenshot(@Nullable Entity entity, boolean handheld) {
		final Minecraft minecraft = Minecraft.getInstance();
		isCameraHandheld = handheld;
		previousCameraEntity = minecraft.getCameraEntity();
		if (entity != null) minecraft.setCameraEntity(entity);

		wasGuiHidden = minecraft.options.hideGui;
		minecraft.options.hideGui = true;
		possessingCamera = true;

		if (minecraft.level != null) {
			final Entity camEntity = minecraft.getCameraEntity();
			if (camEntity != null) minecraft.level.playLocalSound(minecraft.player, CameraPortMain.CAMERA_SNAP, SoundSource.PLAYERS, 0.5F, 1F);
		}

		grabCameraScreenshot(minecraft.gameDirectory, 256, 256);

		if (minecraft.level != null) {
			Entity camEntity = minecraft.getCameraEntity();
			if (camEntity != null) {
				int smokeCount = minecraft.level.getRandom().nextInt(1, 5);
				for (int i = 0; i < smokeCount; i++) {
					minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE, camEntity.getX(), camEntity.getEyeY(), camEntity.getZ(), 0D, 0.15D, 0D);
				}
			}
		}

		if (previousCameraEntity != null) {
			minecraft.setCameraEntity(previousCameraEntity);
			previousCameraEntity = null;
		}

		minecraft.options.hideGui = wasGuiHidden;
		possessingCamera = false;
		isCameraHandheld = false;
	}

	public static void grabCameraScreenshot(File gameDirectory, int width, int height) {
		final Minecraft minecraft = Minecraft.getInstance();
		final Window window = minecraft.getWindow();
		final int prevWidth = window.getWidth();
		final int prevHeight = window.getHeight();
		final GameRenderer gameRenderer = minecraft.gameRenderer;
		final Camera camera = gameRenderer.getMainCamera();
		final RenderTarget renderTarget = minecraft.getMainRenderTarget();
		gameRenderer.setRenderBlockOutline(false);

		try {
			camera.enablePanoramicMode();
			window.setWidth(width);
			window.setHeight(height);
			renderTarget.resize(width, height);
			gameRenderer.update(DeltaTracker.ONE, true);
			gameRenderer.extract(DeltaTracker.ONE, true);
			gameRenderer.renderLevel(DeltaTracker.ONE);
			grab(gameDirectory, renderTarget, (text) -> minecraft.execute(() -> minecraft.gui.getChat().addClientSystemMessage(text)));
		} catch (Exception ignored) {
		} finally {
			gameRenderer.setRenderBlockOutline(true);
			window.setWidth(prevWidth);
			window.setHeight(prevHeight);
			renderTarget.resize(prevWidth, prevHeight);
			camera.disablePanoramicMode();
		}
	}

	private static void grab(File workDir, RenderTarget target, Consumer<Component> callback) {
		Screenshot.takeScreenshot(target, 1, nativeImage -> {
			final File photographPath = workDir.toPath()
				.resolve("photographs")
				.resolve(".local")
				.toFile();
			photographPath.mkdirs();

			Optional<Path> iconPath = Optional.empty();
			Minecraft minecraft = Minecraft.getInstance();
			/*
			if (CameraPortConfig.get().useLatestPhotoAsWorldIcon && minecraft.isLocalServer()) {
				IntegratedServer integratedServer = minecraft.getSingleplayerServer();
				if (integratedServer != null) {
					iconPath = minecraft.getSingleplayerServer().getWorldScreenshotFile();
					iconPath.ifPresent(path -> path.toFile().mkdirs());
				}
			}
			 */

			final File photographFile = getPhotographFile(photographPath);
			Optional<Path> finalIconPath = iconPath;

			Util.ioPool().execute(() -> {
				try {
					nativeImage.writeToFile(photographFile);
					finalIconPath.ifPresent(path -> copyPhotographToFileWithSize(nativeImage, path, 64, 64));

					Component component = Component.literal(photographFile.getName())
						.withStyle(ChatFormatting.UNDERLINE)
						.withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(photographFile.getAbsoluteFile())));

					callback.accept(Component.translatable("screenshot.success", component));
				} catch (Exception e) {
					CameraPortConstants.warn("Couldn't save screenshot " + e, true);
					callback.accept(Component.translatable("screenshot.failure", e.getMessage()));
				} finally {
					nativeImage.close();
				}
			});
		});
	}

	private static void copyPhotographToFileWithSize(NativeImage image, Path path, int width, int height) {
		int i = image.getWidth();
		int j = image.getHeight();
		int k = 0;
		int l = 0;
		if (i > j) {
			k = (i - j) / 2;
			i = j;
		} else {
			l = (j - i) / 2;
			j = i;
		}

		try (NativeImage nativeImage2 = new NativeImage(64, 64, false)) {
			image.resizeSubRectTo(k, l, i, j, nativeImage2);
			nativeImage2.writeToFile(path);
		} catch (IOException e) {
			CameraPortConstants.LOGGER.warn("Couldn't save photograph to world icon", e);
		} finally {
			image.close();
		}
	}

	public static File getPhotographFile(File directory) {
		String fileName = PLAYER_UUID + "_" + System.currentTimeMillis();
		int fileIndex = 1;

		while (true) {
			File file = new File(directory, fileName + (fileIndex == 1 ? "" : "_" + fileIndex) + ".png");
			if (!file.exists()) return file;
			++fileIndex;
		}
	}

}
