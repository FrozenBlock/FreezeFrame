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

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.item.CameraItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@Inject(
		method = "evaluateWhichHandsToRender",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z",
			ordinal = 0
		),
		cancellable = true
	)
	private static void cameraPort$renderOnlyMainHandWhenUsingCamera(
		LocalPlayer player, CallbackInfoReturnable<ItemInHandRenderer.HandRenderSelection> info,
		@Local(name = "mainHandItem") ItemStack mainHandItem
	) {
		if (CameraItem.isCapableOfTakingPhotos(mainHandItem)) info.setReturnValue(ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY);
	}

}
