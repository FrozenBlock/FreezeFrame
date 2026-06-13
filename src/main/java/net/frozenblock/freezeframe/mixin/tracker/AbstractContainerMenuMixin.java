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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

	@Shadow
	@Final
	public static int QUICKCRAFT_TYPE_CLONE;

	@Shadow
	private int quickcraftType;

	@WrapOperation(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/Slot;setByPlayer(Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getQuickCraftPlaceCount(IILnet/minecraft/world/item/ItemStack;)I"
			)
		)
	)
	public void freezeFrame$onDragCloneQuickCraft(
		Slot instance, ItemStack itemStack, Operation<Void> original,
		@Local(argsOnly = true) Player player,
		@Local(name = "source") ItemStack source,
		@Share("freezeFrame$adjustedForFirst") LocalBooleanRef adjustedForFirst
	) {
		if (this.quickcraftType == QUICKCRAFT_TYPE_CLONE && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			final int increaseCount = itemStack.getCount() - instance.getItem().getCount();
			if (increaseCount > 0) {
				if (player.level().isClientSide()) {
					ClientPlayNetworking.send(new ChangeItemStackSizePacket(itemStack.copy(), increaseCount));
				} else {
					PhotographTracker.incrementOnItemStackSizeChange(player.level(), itemStack.copy(), increaseCount);
				}
				FFConstants.log("onDragCloneQuickCraft - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
			}

			if (!adjustedForFirst.get()) {
				if (player.level().isClientSide()) {
					ClientPlayNetworking.send(ChangeItemStackSizePacket.itemStackDeleted(source.copy()));
				} else {
					PhotographTracker.incrementOnItemStackDeletion(player.level(), source.copy());
				}
				FFConstants.log("onDragCloneQuickCraft (Adjust Against Carried) - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
				adjustedForFirst.set(true);
			}
		}
		original.call(instance, itemStack);
	}

	@ModifyExpressionValue(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/Slot;safeClone(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "FIELD",
				target = "Lnet/minecraft/world/inventory/ContainerInput;CLONE:Lnet/minecraft/world/inventory/ContainerInput;",
				opcode = Opcodes.GETSTATIC,
				ordinal = 0
			)
		)
	)
	public ItemStack freezeframe$onSafeClone(ItemStack original) {
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientPlayNetworking.send(ChangeItemStackSizePacket.itemStackCloned(original.copy()));
			FFConstants.log("onSafeClone - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
		}
		return original;
	}
}
