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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void cameraPort$cancelAttackWhileUsingCamera(CallbackInfoReturnable<Boolean> info) {
		final LocalPlayer player = Minecraft.class.cast(this).player;
		if (player == null || !ScopeItemHelper.isPlayerHoldingPhotoTakingCamera(player)) return;
		info.setReturnValue(false);
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void cameraPort$blockBreakWhileUsingCamera(boolean down, CallbackInfo info) {
		final LocalPlayer player = Minecraft.class.cast(this).player;
		if (player == null || !ScopeItemHelper.isPlayerHoldingPhotoTakingCamera(player)) return;
		info.cancel();
	}
}
