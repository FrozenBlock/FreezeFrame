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

package net.frozenblock.freezeframe.mixin.tracker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Inventory.class)
public class InventoryMixin {

	@Shadow
	@Final
	public Player player;

	@WrapOperation(
		method = "add(ILnet/minecraft/world/item/ItemStack;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;setCount(I)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$onItemAbsorbedFromCreativePlayer(ItemStack instance, int count, Operation<Void> original) {
		if (!this.player.level().isClientSide() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			if (count != 0) {
				FFConstants.log("onItemAbsorbedFromCreativePlayer - Inventory: Non-zero setCount called!", FFConstants.UNSTABLE_LOGGING);
			} else {
				FFConstants.log("onItemAbsorbedFromCreativePlayer - Inventory", FFConstants.UNSTABLE_LOGGING);
			}
			PhotographTracker.incrementOnItemStackSizeChange(this.player.level(), instance.copy(), -(instance.getCount() - count));
		}
		original.call(instance, count);
	}

	@WrapOperation(
		method = "add(ILnet/minecraft/world/item/ItemStack;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;setCount(I)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z",
				ordinal = 1
			)
		)
	)
	public void freezeFrame$onDamagedItemAbsorbedFromCreativePlayer(ItemStack instance, int count, Operation<Void> original) {
		if (!this.player.level().isClientSide() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			if (count != 0) {
				FFConstants.log("onDamagedItemAbsorbedFromCreativePlayer - Inventory: Non-zero setCount called!", FFConstants.UNSTABLE_LOGGING);
			} else {
				FFConstants.log("onDamagedItemAbsorbedFromCreativePlayer - Inventory", FFConstants.UNSTABLE_LOGGING);
			}
			PhotographTracker.incrementOnItemStackSizeChange(this.player.level(), instance.copy(), -(instance.getCount() - count));
		}
		original.call(instance, count);
	}
}
