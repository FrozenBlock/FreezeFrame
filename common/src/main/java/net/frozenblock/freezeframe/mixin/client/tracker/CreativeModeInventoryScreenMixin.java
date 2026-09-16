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
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.frozenblock.freezeframe.networking.packet.SetCreativeModeCarriedItemPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.lib.networking.api.ClientNetworkingHelper;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
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

@ClientOnly
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

	public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Unique
	private static void freezeFrame$sendCreativeModeCarriedItemPacket(Player player, AbstractContainerMenu menu) {
		if (player == null || !FFConfig.PHOTOGRAPH_TRACKER.get()) return;

		final ItemStack attachedCarried = FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.getAttachedOrElse(player, ItemStack.EMPTY);
		final ItemStack carried = menu.getCarried();
		if (ItemStack.isSameItemSameComponents(attachedCarried, carried) && (carried.count() == attachedCarried.count())) return;

		FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.set(player, carried.copy());
		ClientNetworkingHelper.sendToServer(new SetCreativeModeCarriedItemPacket(carried.copy()));
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
		if (!original.getItem().isEmpty() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(ChangeItemStackSizePacket.itemStackDeleted(original.getItem().copy()));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onTrashAll - CreativeModeInventoryScreen", true);
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
		if (!this.menu.getCarried().isEmpty() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(ChangeItemStackSizePacket.itemStackDeleted(this.menu.getCarried().copy()));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onTrash - CreativeModeInventoryScreen", true);
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
		if (!original.getItem(buttonNum).isEmpty() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(ChangeItemStackSizePacket.itemStackDeleted(original.getItem(buttonNum).copy()));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onReplacedByNumpadFromPicker - CreativeModeInventoryScreen", true);
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
		if (count > 0 && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(new ChangeItemStackSizePacket(instance.copy(), count));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onClone - CreativeModeInventoryScreen", true);
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
		if (delta > 0 && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(new ChangeItemStackSizePacket(instance.copy(), delta));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onSameItemMaxedFromPicker - CreativeModeInventoryScreen", true);
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
		if (amount > 0 && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(new ChangeItemStackSizePacket(instance.copy(), amount));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onSameItemGrownFromPicker - CreativeModeInventoryScreen", true);
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
		if (amount > 0 && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(new ChangeItemStackSizePacket(instance.copy(), -amount));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onSameItemShrankFromPicker - CreativeModeInventoryScreen", true);
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
		if (count > 0 && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(new ChangeItemStackSizePacket(instance.copy(), count));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onCopiedFromPicker - CreativeModeInventoryScreen", true);
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
		if (!this.menu.getCarried().isEmpty() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(ChangeItemStackSizePacket.itemStackDeleted(this.menu.getCarried().copy()));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onDeletedFromPicker - CreativeModeInventoryScreen", true);
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
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(new ChangeItemStackSizePacket(instance.copy(), -amount));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onShrankFromPicker - CreativeModeInventoryScreen", true);
		}
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
		if (FFConfig.PHOTOGRAPH_TRACKER.get()) {
			final ItemStack originalInSlot = instance.getItem(slot);
			if (!originalInSlot.isEmpty()) {
				ClientNetworkingHelper.sendToServer(ChangeItemStackSizePacket.itemStackDeleted(originalInSlot.copy()));
				if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onDeletedFromHotbarLoad - CreativeModeInventoryScreen", true);
			}
		}

		original.call(instance, slot, itemStack);
	}
}
