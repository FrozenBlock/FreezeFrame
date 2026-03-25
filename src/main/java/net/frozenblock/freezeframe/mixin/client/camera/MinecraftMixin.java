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

package net.frozenblock.freezeframe.mixin.client.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.frozenblock.freezeframe.util.client.CameraScreenshotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	public int missTime;

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$cancelAttackWhileUsingCamera(CallbackInfoReturnable<Boolean> info) {
		final LocalPlayer player = Minecraft.class.cast(this).player;
		if (player == null || !ScopeItemHelper.isPlayerHoldingPhotoTakingCamera(player)) return;
		this.missTime = 10;
		info.setReturnValue(false);
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$cancelBreakWhileUsingCamera(boolean down, CallbackInfo info) {
		final LocalPlayer player = Minecraft.class.cast(this).player;
		if (player == null || !ScopeItemHelper.isPlayerHoldingPhotoTakingCamera(player)) return;
		info.cancel();
	}

	@WrapOperation(
		method = "startUseItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"
		)
	)
	private boolean freezeFrame$cancelOffHandUsageWhileHoldingCameraInMainHand(
		ItemStack instance, FeatureFlagSet enabledFeatures, Operation<Boolean> original,
		@Local(name = "hand") InteractionHand hand
	) {
		if (!original.call(instance, enabledFeatures)) return false;
		return this.player == null || hand != InteractionHand.OFF_HAND || !ScopeItemHelper.isPlayerHoldingPhotoTakingCamera(this.player);
	}

	// TODO: see if this actually fixes anything with Iris.
	@ModifyReturnValue(method = "getMainRenderTarget", at = @At("RETURN"))
	public RenderTarget freezeFrame$getMainRenderTarget(RenderTarget original) {
		if (!CameraScreenshotManager.isScreenshotting()) return original;
		final RenderTarget cameraTarget = CameraScreenshotManager.getRenderTarget();
		return cameraTarget != null ? cameraTarget : original;
	}
}
