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
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.frozenblock.freezeframe.networking.packet.DeleteItemStackPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
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

	@Shadow
	private static void dropOrPlaceInInventory(Player player, ItemStack carried) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@ModifyExpressionValue(
		method = "removed",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
		)
	)
	public boolean freezeFrame$onRemovedWithEmptyCarried(
		boolean original,
		@Local(argsOnly = true) Player player
	) {
		if (original && player != null) {
			final ItemStack carriedAttachment = player.getAttachedOrElse(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM, ItemStack.EMPTY);
			if (!carriedAttachment.isEmpty()) {
				PhotographTracker.incrementOnItemStackDeletion(player.level(), carriedAttachment.copy());
				dropOrPlaceInInventory(player, carriedAttachment);
			}
			player.removeAttached(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM);
		}
		return original;
	}

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
		if (this.quickcraftType == QUICKCRAFT_TYPE_CLONE) {
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
					ClientPlayNetworking.send(new DeleteItemStackPacket(source.copy()));
				} else {
					PhotographTracker.incrementOnItemStackDeletion(player.level(), source.copy());
				}
				FFConstants.log("onDragCloneQuickCraft (Adjust Against Carried) - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
				adjustedForFirst.set(true);
			}
		}
		original.call(instance, itemStack);
	}

	@WrapOperation(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;",
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
	public ItemStack freezeframe$onClone(ItemStack instance, int count, Operation<ItemStack> original) {
		final ItemStack copyWithCount = original.call(instance, count);
		if (count > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), count));
			FFConstants.log("onClone - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
		}
		return copyWithCount;
	}
}
