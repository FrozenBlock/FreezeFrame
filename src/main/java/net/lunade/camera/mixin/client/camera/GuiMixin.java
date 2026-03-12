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

package net.lunade.camera.mixin.client.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {
	@Shadow @Final private Minecraft minecraft;
	@Shadow private float scopeScale;
	@Shadow @Final private static Identifier SPYGLASS_SCOPE_LOCATION;

	private static final Identifier CAMERA_ZOOM_OVERLAY = CameraPortConstants.id("textures/gui/camera_zoom_overlay.png");

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void cameraPort$removeOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		if (CameraScreenshotManager.isUsingSelfRenderingCamera()) {
			info.cancel();
			return;
		}

		if (this.minecraft.player == null || !this.minecraft.options.getCameraType().isFirstPerson()) return;
		if (!ScopeItemHelper.isPlayerUsingScopeItem(this.minecraft.player)) return;

		this.scopeScale = Mth.lerp(0.5F * deltaTracker.getGameTimeDeltaTicks(), this.scopeScale, 1.125F);
		if (ScopeItemHelper.isPlayerUsingCamera(this.minecraft.player)) {
			cameraPort$renderScopeOverlay(guiGraphics, this.scopeScale, CAMERA_ZOOM_OVERLAY);
		} else {
			cameraPort$renderScopeOverlay(guiGraphics, this.scopeScale, SPYGLASS_SCOPE_LOCATION);
		}
		info.cancel();
	}

	@Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
	private void cameraPort$replaceSpyglassOverlay(GuiGraphics guiGraphics, float scopeScale, CallbackInfo info) {
		if (this.minecraft.player == null || !ScopeItemHelper.isPlayerUsingCamera(this.minecraft.player)) return;
		info.cancel();
		cameraPort$renderScopeOverlay(guiGraphics, scopeScale, CAMERA_ZOOM_OVERLAY);
	}

	private static void cameraPort$renderScopeOverlay(GuiGraphics guiGraphics, float scopeScale, Identifier overlayTexture) {
		final float minDimension = Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
		final float scopedScale = Math.min(
			guiGraphics.guiWidth() / minDimension,
			guiGraphics.guiHeight() / minDimension
		) * scopeScale;

		final int scopedWidth = Mth.floor(minDimension * scopedScale);
		final int scopedHeight = Mth.floor(minDimension * scopedScale);
		final int left = (guiGraphics.guiWidth() - scopedWidth) / 2;
		final int top = (guiGraphics.guiHeight() - scopedHeight) / 2;
		final int right = left + scopedWidth;
		final int bottom = top + scopedHeight;

		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, overlayTexture, left, top, 0F, 0F, scopedWidth, scopedHeight, scopedWidth, scopedHeight);
		guiGraphics.fill(RenderPipelines.GUI, 0, bottom, guiGraphics.guiWidth(), guiGraphics.guiHeight(), -16777216);
		guiGraphics.fill(RenderPipelines.GUI, 0, 0, guiGraphics.guiWidth(), top, -16777216);
		guiGraphics.fill(RenderPipelines.GUI, 0, top, left, bottom, -16777216);
		guiGraphics.fill(RenderPipelines.GUI, right, top, guiGraphics.guiWidth(), bottom, -16777216);
	}

}
