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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity {

	protected ArmorStandMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@ModifyExpressionValue(
		method = "swapItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"
		)
	)
	public ItemStack freezeFrame$incrementOnCreativeInteract(
		ItemStack original,
		Player player, EquipmentSlot slot, ItemStack playerItemStack, InteractionHand hand
	) {
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			PhotographTracker.incrementOnItemStackSizeChange(player.level(), original.copy(), original.count());
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("incrementOnCreativeInteract - ArmorStand", true);
		}
		return original;
	}

	@Inject(method = "kill", at = @At("HEAD"))
	public void freezeFrame$incrementOnKill(ServerLevel level, CallbackInfo info) {
		if (!FFConfig.PHOTOGRAPH_TRACKER.get()) return;

		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			final ItemStack itemStack = this.equipment.set(slot, ItemStack.EMPTY);
			PhotographTracker.incrementOnItemStackDeletion(level, itemStack.copy());
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("incrementOnKill - ArmorStand", true);
		}
	}
}
