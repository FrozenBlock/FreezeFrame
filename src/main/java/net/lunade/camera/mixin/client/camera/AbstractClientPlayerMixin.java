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
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.ScopeZoomHelper;
import net.lunade.camera.util.client.ScopeZoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {

	@ModifyConstant(
		method = "getFieldOfViewModifier",
		constant = @Constant(floatValue = 0.1F),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/player/AbstractClientPlayer;isScoping()Z"
			)
		)
	)
	private float cameraPort$applyZoomWheelToScopeFov(float original) {
		final AbstractClientPlayer player = AbstractClientPlayer.class.cast(this);
		if (!ScopeItemHelper.isPlayerUsingScopeItem(player)) return original;

		final float defaultFov = ScopeItemHelper.isPlayerUsingCamera(player) ? 90F : ScopeZoomManager.DEFAULT_FOV;
		final float playerSettingFov = Mth.clamp(Minecraft.getInstance().options.fov().get().floatValue(), 1F, 180F);
		final float fovNormalization = defaultFov / playerSettingFov;
		final float cameraFovModifier = ScopeZoomHelper.toFovModifier(ScopeZoomManager.getZoom());
		return cameraFovModifier * fovNormalization;
	}
}
