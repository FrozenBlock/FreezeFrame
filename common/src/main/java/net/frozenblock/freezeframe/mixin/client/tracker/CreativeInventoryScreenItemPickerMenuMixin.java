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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.frozenblock.freezeframe.networking.packet.SetCreativeModeCarriedItemPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.lib.networking.api.ClientNetworkingHelper;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public abstract class CreativeInventoryScreenItemPickerMenuMixin extends AbstractContainerMenu {

	protected CreativeInventoryScreenItemPickerMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
		super(menuType, containerId);
	}

	@Inject(method = "setCarried", at = @At("RETURN"))
	public void freezeFrame$onSetCarried(ItemStack carried, CallbackInfo info) {
		if (!FFConfig.PHOTOGRAPH_TRACKER.get()) return;

		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.set(minecraft.player, carried.copy());
		ClientNetworkingHelper.sendToServer(new SetCreativeModeCarriedItemPacket(carried.copy()));
	}

	@WrapOperation(
		method = "quickMoveStack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/Slot;setByPlayer(Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 0
		)
	)
	public void freezeFrame$onDeleteFromQuickMove(Slot instance, ItemStack itemStack, Operation<Void> original) {
		if (!instance.getItem().isEmpty() && FFConfig.PHOTOGRAPH_TRACKER.get()) {
			ClientNetworkingHelper.sendToServer(ChangeItemStackSizePacket.itemStackDeleted(instance.getItem().copy()));
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("onDeleteFromQuickMove - CreativeModeInventoryScreen$ItemPickerMenu", true);
		}
		original.call(instance, itemStack);
	}
}
