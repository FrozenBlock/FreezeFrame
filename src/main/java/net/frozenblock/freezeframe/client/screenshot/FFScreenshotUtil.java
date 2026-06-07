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
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.GpuFormat;

@Environment(EnvType.CLIENT)
public class FFScreenshotUtil {
	private static boolean screenshotting = false;
	private static boolean handheld = false;
	private static RenderTarget renderTarget = null;
	private static EnvironmentAttributeProbe environmentAttributeProbe = null;

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
		if (minecraft.level == null) return;

		final Window window = minecraft.getWindow();
		final Camera camera = minecraft.gameRenderer.mainCamera();
		final Pair<Integer, Integer> preResolution = Pair.of(window.getWidth(), window.getHeight());
		final Entity preCameraEntity = entity != null ? minecraft.getCameraEntity() : null;
		final Pair<Float, Float> preEyeHeight = Pair.of(camera.eyeHeightOld, camera.eyeHeight);
		final boolean preHudHidden = minecraft.gui.hud.isHidden;

		screenshotting = true;
		handheld = handheldCapture;
		minecraft.gui.hud.isHidden = true;

		if (handheldCapture) {
			ScopeZoomManager.pushForcedZoom(zoom);
		} else if (entity != null) {
			camera.setEntity(entity);
		}
		screenshotAsCamera(minecraft, window, camera, resolution, resolution, filter, entity != null, callback);

		screenshotting = false;
		handheld = false;
		renderTarget = null;
		environmentAttributeProbe = null;
		minecraft.gui.hud.isHidden = preHudHidden;

		window.setWidth(preResolution.getFirst());
		window.setHeight(preResolution.getSecond());
		camera.eyeHeightOld = preEyeHeight.getFirst();
		camera.eyeHeight = preEyeHeight.getSecond();

		if (entity != null) minecraft.setCameraEntity(preCameraEntity);
		if (handheldCapture) ScopeZoomManager.clearForcedZoom();
		if (handheldCapture && wasScoping && !filter.isEmpty()) ScopePostEffectController.applyFromFilter(minecraft, filter);
	}

	private static void screenshotAsCamera(
		Minecraft minecraft,
		Window window,
		Camera camera,
		int width,
		int height,
		FilmFilter filter,
		boolean updateAttributeProbe,
		Consumer<NativeImage> callback
	) {
		renderTarget = new TextureTarget("photograph", width, height, true, GpuFormat.RGBA8_UNORM);

		final GameRenderer gameRenderer = minecraft.gameRenderer;
		gameRenderer.setRenderBlockOutline(false);

		try {
			camera.enablePanoramicMode();
			window.setWidth(width);
			window.setHeight(height);

			ScopePostEffectController.applyFromFilter(minecraft, filter);

			gameRenderer.update(DeltaTracker.ONE);
			if (updateAttributeProbe) {
				environmentAttributeProbe = new EnvironmentAttributeProbe();
				environmentAttributeProbe.tick(minecraft.level, camera.position());
			}
			gameRenderer.extract(DeltaTracker.ONE, true);
			gameRenderer.render(DeltaTracker.ONE, true);

			Screenshot.takeScreenshot(renderTarget, callback);
		} catch (Exception ignored) {} finally {
			ScopePostEffectController.clearIfApplied(minecraft);
			gameRenderer.setRenderBlockOutline(true);
			camera.disablePanoramicMode();
		}
	}
}
