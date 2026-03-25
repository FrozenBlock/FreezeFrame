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

package net.frozenblock.freezeframe.mixin.camera;

import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Shadow
	public abstract InteractionHand getUsedItemHand();

	@Shadow
	public abstract void stopUsingItem();

	@Inject(
		method = "updatingUsingItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
		),
		cancellable = true
	)
	private void freezeFrame$stopUsingItemWhenHoldingCamera(CallbackInfo info) {
		if (!(LivingEntity.class.cast(this) instanceof Player player)) return;
		final InteractionHand usedItemHand = this.getUsedItemHand();
		if (usedItemHand == InteractionHand.MAIN_HAND) return;
		if (ScopeItemHelper.isPlayerHoldingPhotoTakingCamera(player)) {
			this.stopUsingItem();
			info.cancel();
		}
	}

}
