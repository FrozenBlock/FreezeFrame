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
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

	@WrapOperation(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V",
			ordinal = 1
		)
	)
	public void freezeFrame$onItemEntityDespawned(ItemEntity instance, Operation<Void> original) {
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			PhotographTracker.incrementOnItemStackDeletion(instance.level(), instance.getItem());
			FFConstants.log("onItemEntityDespawned - ItemEntity", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance);
	}

	@WrapOperation(
		method = "hurtServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
		)
	)
	public void freezeFrame$onItemEntityDestroyed(ItemEntity instance, Operation<Void> original) {
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			PhotographTracker.incrementOnItemStackDeletion(instance.level(), instance.getItem(), false);
			FFConstants.log("onItemEntityDestroyed - ItemEntity", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance);
	}
}
