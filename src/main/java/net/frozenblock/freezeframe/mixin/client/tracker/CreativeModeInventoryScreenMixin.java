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

package net.frozenblock.freezeframe.mixin.client.tracker;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.frozenblock.freezeframe.networking.packet.DeleteItemStackPacket;
import net.frozenblock.freezeframe.networking.packet.SetCreativeModeCarriedItemPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

	public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Unique
	private static void freezeFrame$sendCreativeModeCarriedItemPacket(Player player, AbstractContainerMenu menu) {
		if (player == null) return;

		final ItemStack attachedCarried = player.getAttachedOrElse(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM, ItemStack.EMPTY);
		final ItemStack carried = menu.getCarried();
		if (ItemStack.isSameItemSameComponents(attachedCarried, carried) && (carried.count() == attachedCarried.count())) return;

		player.setAttached(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM, carried.copy());
		ClientPlayNetworking.send(new SetCreativeModeCarriedItemPacket(carried.copy()));
	}

	@Inject(method = "containerTick", at = @At("HEAD"))
	public void freezeFrame$onContainerTick(CallbackInfo info) {
		freezeFrame$sendCreativeModeCarriedItemPacket(this.minecraft.player, this.menu);
	}

	@Inject(method = "slotClicked", at = @At("RETURN"))
	public void freezeFrame$onSlotClickedEnd(CallbackInfo info) {
		freezeFrame$sendCreativeModeCarriedItemPacket(this.minecraft.player, this.menu);
	}

	@Inject(method = "keyPressed", at = @At("RETURN"))
	public void freezeFrame$onKeyPressedEnd(KeyEvent event, CallbackInfoReturnable<Boolean> info) {
		freezeFrame$sendCreativeModeCarriedItemPacket(this.minecraft.player, this.menu);
	}

	@Inject(method = "keyReleased", at = @At("RETURN"))
	public void freezeFrame$onKeyReleasedEnd(KeyEvent event, CallbackInfoReturnable<Boolean> info) {
		freezeFrame$sendCreativeModeCarriedItemPacket(this.minecraft.player, this.menu);
	}

	@Inject(method = "mouseClicked", at = @At("RETURN"))
	public void freezeFrame$onMouseClickedEnd(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> info) {
		freezeFrame$sendCreativeModeCarriedItemPacket(this.minecraft.player, this.menu);
	}

	@Inject(method = "mouseReleased", at = @At("RETURN"))
	public void freezeFrame$onMouseReleasedEnd(MouseButtonEvent event, CallbackInfoReturnable<Boolean> info) {
		freezeFrame$sendCreativeModeCarriedItemPacket(this.minecraft.player, this.menu);
	}

	@ModifyExpressionValue(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/InventoryMenu;getSlot(I)Lnet/minecraft/world/inventory/Slot;",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "FIELD",
				target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;",
				opcode = Opcodes.GETFIELD,
				ordinal = 0
			)
		)
	)
	public Slot freezeFrame$onTrashAll(Slot original) {
		if (!original.getItem().isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(original.getItem().copy()));
			FFConstants.log("onTrashAll - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		return original;
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;setCarried(Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "FIELD",
				target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;",
				opcode = Opcodes.GETFIELD,
				ordinal = 1
			)
		)
	)
	public void freezeFrame$onTrash(CreativeModeInventoryScreen.ItemPickerMenu instance, ItemStack carried, Operation<Void> original) {
		if (!this.menu.getCarried().isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(this.menu.getCarried().copy()));
			FFConstants.log("onTrash - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, carried);
	}

	@ModifyExpressionValue(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "FIELD",
				target = "Lnet/minecraft/world/inventory/ContainerInput;SWAP:Lnet/minecraft/world/inventory/ContainerInput;",
				opcode = Opcodes.GETSTATIC,
				ordinal = 0
			)
		)
	)
	public Inventory freezeFrame$onReplacedByNumpadFromPicker(
		Inventory original,
		@Local(argsOnly = true, ordinal = 1) int buttonNum
	) {
		if (!original.getItem(buttonNum).isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(original.getItem(buttonNum).copy()));
			FFConstants.log("onReplacedByNumpadFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		return original;
	}

	@WrapOperation(
		method = "slotClicked",
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
		if (count > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), count));
			FFConstants.log("onClone - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		return original.call(instance, count);
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;setCount(I)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$onSameItemMaxedFromPicker(ItemStack instance, int count, Operation<Void> original) {
		final int delta = count - instance.getCount();
		if (delta > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), delta));
			FFConstants.log("onSameItemMaxedFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, count);
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;grow(I)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$onSameItemGrownFromPicker(ItemStack instance, int amount, Operation<Void> original) {
		if (amount > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), amount));
			FFConstants.log("onSameItemGrownFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, amount);
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$onSameItemShrankFromPicker(ItemStack instance, int amount, Operation<Void> original) {
		if (amount > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), -amount));
			FFConstants.log("onSameItemShrankFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, amount);
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
				ordinal = 0
			)
		)
	)
	public ItemStack freezeFrame$onCopiedFromPicker(ItemStack instance, int count, Operation<ItemStack> original) {
		if (count > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), count));
			FFConstants.log("onCopiedFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		return original.call(instance, count);
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;setCarried(Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$onDeletedFromPicker(CreativeModeInventoryScreen.ItemPickerMenu instance, ItemStack carried, Operation<Void> original) {
		if (!this.menu.getCarried().isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(this.menu.getCarried().copy()));
			FFConstants.log("onDeletedFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, carried);
	}

	@WrapOperation(
		method = "slotClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
			ordinal = 1
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;getCarried()Lnet/minecraft/world/item/ItemStack;",
				ordinal = 0
			)
		)
	)
	public void freezeFrame$onShrankFromPicker(ItemStack instance, int amount, Operation<Void> original) {
		ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), -amount));
		FFConstants.log("onShrankFromPicker - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		original.call(instance, amount);
	}

	@WrapOperation(
		method = "handleHotbarLoadOrSave",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Inventory;setItem(ILnet/minecraft/world/item/ItemStack;)V"
		)
	)
	private static void freezeFrame$onDeletedFromHotbarLoad(Inventory instance, int slot, ItemStack itemStack, Operation<Void> original) {
		final ItemStack originalInSlot = instance.getItem(slot);
		if (!originalInSlot.isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(originalInSlot.copy()));
			FFConstants.log("onDeletedFromHotbarLoad - CreativeModeInventoryScreen", FFConstants.UNSTABLE_LOGGING);
		}

		original.call(instance, slot, itemStack);
	}
}
