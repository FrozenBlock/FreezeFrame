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

import com.llamalad7.mixinextras.sugar.Local;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {

	@Shadow
	public abstract ItemStack getItem();

	@Inject(
		method = "interact",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setItem(Lnet/minecraft/world/item/ItemStack;)V"
		)
	)
	public void freezeFrame$incrementOnCreativeInteract(
		Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> info,
		@Local(name = "itemStack") ItemStack itemStack
	) {
		if (player.hasInfiniteMaterials() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			PhotographTracker.incrementOnItemStackSizeChange(player.level(), itemStack.copyWithCount(1), 1);
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("incrementOnCreativeInteract - ItemFrame", true);
		}
	}

	@Inject(
		method = "dropItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/decoration/ItemFrame;removeFramedMap(Lnet/minecraft/world/item/ItemStack;)V"
		),
		slice = @Slice(
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/entity/decoration/ItemFrame;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$incrementOnDropItemWithoutSpawning(
		ServerLevel level, Entity causedBy, boolean withFrame, CallbackInfo info,
		@Local(name = "itemStack") ItemStack itemStack
	) {
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			PhotographTracker.incrementOnItemStackDeletion(level, itemStack.copy());
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("incrementOnDropItemWithoutSpawning - ItemFrame", true);
		}
	}

	@Inject(method = "kill", at = @At("HEAD"))
	public void freezeFrame$incrementOnKill(ServerLevel level, CallbackInfo info) {
		final ItemStack itemStack = this.getItem();
		if (!itemStack.isEmpty() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			PhotographTracker.incrementOnItemStackDeletion(level, itemStack.copy());
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("incrementOnKill - ItemFrame", true);
		}
	}
}
