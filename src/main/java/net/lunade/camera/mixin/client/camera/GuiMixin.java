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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private static final Identifier CAMERA_PORT$CAMERA_ZOOM = CameraPortConstants.id("textures/misc/camera_zoom.png");

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	public void cameraPort$removeOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo info) {
		if (CameraScreenshotManager.isScreenshottingFromTripodCamera()) info.cancel();
	}

	@ModifyExpressionValue(
		method = "extractSpyglassOverlay",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/gui/Gui;SPYGLASS_SCOPE_LOCATION:Lnet/minecraft/resources/Identifier;",
			opcode = Opcodes.GETSTATIC
		)
	)
	private Identifier cameraPort$useCameraOverlay(Identifier original) {
		if (this.minecraft.player == null || !ScopeItemHelper.isCameraItem(this.minecraft.player.getUseItem())) return original;
		return CAMERA_PORT$CAMERA_ZOOM;
	}

}
