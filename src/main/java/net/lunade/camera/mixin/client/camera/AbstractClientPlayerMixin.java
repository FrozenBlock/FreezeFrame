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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.ScopeZoomHelper;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.lunade.camera.util.client.CameraZoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

	@Unique
	private static final float CAMERA_PORT$DEFAULT_FOV = 70F;

	@ModifyReturnValue(method = "getFieldOfViewModifier", at = @At("RETURN"))
	private float cameraPort$applyZoomWheelToScopeFov(float original, boolean firstPerson, float fovEffectScale) {
		final AbstractClientPlayer player = AbstractClientPlayer.class.cast(this);
		if (!ScopeItemHelper.isPlayerUsingScopeItem(player) && !CameraScreenshotManager.isUsingHandheldCamera()) return original;

		final float playerSettingFov = Mth.clamp(Minecraft.getInstance().options.fov().get().floatValue(), 1F, 180F);
		final float fovNormalization = CAMERA_PORT$DEFAULT_FOV / playerSettingFov;
		final float cameraFovModifier = ScopeZoomHelper.toFovModifier(CameraZoomManager.getZoom());
		return cameraFovModifier * fovNormalization;
	}
}
