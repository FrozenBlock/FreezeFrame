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
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.scope.ScopePostEffectController;
import net.frozenblock.freezeframe.client.scope.ScopeZoomManager;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FFScreenshotUtil {
	private static boolean screenshotting = false;
	private static boolean handheld = false;
	private static RenderTarget renderTarget = null;
	private static EnvironmentAttributeProbe environmentAttributeProbe = null;
	private static Entity preCameraEntity = null;
	private static Pair<Float, Float> preEyeHeight = null;
	private static boolean preHideGui = false;
	private static Pair<Integer, Integer> preResolution = null;

	public static boolean screenshotting() {
		return screenshotting;
	}

	public static boolean screenshottingAndTripod() {
		return screenshotting() && !handheld;
	}

	public static boolean screenshottingAndHandheld() {
		return screenshotting() && handheld;
	}

	public static boolean notScreenshottingOrIsTripod() {
		return !screenshotting() || screenshottingAndHandheld();
	}

	@Nullable
	public static RenderTarget getRenderTarget() {
		return renderTarget;
	}

	@Nullable
	public static EnvironmentAttributeProbe environmentAttributeProbe() {
		return environmentAttributeProbe;
	}

	public static void restorePreScreenshotData(Minecraft minecraft) {
		minecraft.options.hideGui = preHideGui;
		if (preResolution != null) {
			minecraft.getWindow().setWidth(preResolution.getFirst());
			minecraft.getWindow().setHeight(preResolution.getSecond());
		}
		if (preEyeHeight != null) {
			final Camera camera = minecraft.gameRenderer.getMainCamera();
			camera.eyeHeightOld = preEyeHeight.getFirst();
			camera.eyeHeight = preEyeHeight.getSecond();
		}
		minecraft.setCameraEntity(preCameraEntity);
	}

	public static void reset(Minecraft minecraft) {
		screenshotting = false;
		handheld = false;
		renderTarget = null;
		environmentAttributeProbe = null;

		restorePreScreenshotData(minecraft);
		preEyeHeight = null;
		preCameraEntity = null;
	}

	public static void executeScreenshot(
		@Nullable Entity entity,
		boolean handheldCapture,
		boolean wasScoping,
		float zoom,
		FilmFilter filter,
		int resolution,
		Consumer<NativeImage> callback
	) {
		final Minecraft minecraft = Minecraft.getInstance();
		screenshotting = true;
		handheld = handheldCapture;
		preHideGui = minecraft.options.hideGui;
		minecraft.options.hideGui = true;

		if (handheldCapture) ScopeZoomManager.pushForcedZoom(zoom);
		screenshotAsCamera(resolution, resolution, filter, entity, callback);

		makeSnapSoundAndSmoke: {
			if (minecraft.level == null) break makeSnapSoundAndSmoke;

			final Entity camEntity = entity != null ? entity : minecraft.getCameraEntity();
			if (camEntity == null) break makeSnapSoundAndSmoke;

			final int smokeCount = minecraft.level.getRandom().nextInt(1, 5);
			for (int i = 0; i < smokeCount; i++) {
				minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE, camEntity.getX(), camEntity.getEyeY(), camEntity.getZ(), 0D, 0.15D, 0D);
			}
		}

		reset(minecraft);

		if (handheldCapture) ScopeZoomManager.clearForcedZoom();
		if (handheldCapture && wasScoping && !filter.isEmpty()) ScopePostEffectController.applyFromFilter(minecraft, filter);
	}

	private static void screenshotAsCamera(int width, int height, FilmFilter filter, @Nullable Entity entity, Consumer<NativeImage> callback) {
		final Minecraft minecraft = Minecraft.getInstance();

		final Window window = minecraft.getWindow();
		preResolution = Pair.of(window.getWidth(), window.getHeight());
		renderTarget = new TextureTarget("photograph", width, height, true);

		final GameRenderer gameRenderer = minecraft.gameRenderer;
		final Camera camera = gameRenderer.getMainCamera();
		gameRenderer.setRenderBlockOutline(false);
		if (entity != null) preCameraEntity = camera.entity();

		try {
			camera.enablePanoramicMode();
			window.setWidth(width);
			window.setHeight(height);

			if (entity != null) {
				preEyeHeight = Pair.of(camera.eyeHeightOld, camera.eyeHeight);
				camera.setEntity(entity);
			}

			ScopePostEffectController.applyFromFilter(minecraft, filter);

			gameRenderer.update(DeltaTracker.ONE, true);
			if (entity != null) {
				environmentAttributeProbe = new EnvironmentAttributeProbe();
				environmentAttributeProbe.tick(minecraft.level, camera.position());
			}
			gameRenderer.extract(DeltaTracker.ONE, true);
			gameRenderer.render(DeltaTracker.ONE, true);

			Screenshot.takeScreenshot(getRenderTarget(), callback);
		} catch (Exception ignored) {} finally {
			ScopePostEffectController.clearIfApplied(minecraft);
			gameRenderer.setRenderBlockOutline(true);
			camera.disablePanoramicMode();
		}
	}
}
