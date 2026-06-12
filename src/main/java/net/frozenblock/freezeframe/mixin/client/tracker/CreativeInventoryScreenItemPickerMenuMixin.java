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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.networking.packet.DeleteItemStackPacket;
import net.frozenblock.freezeframe.networking.packet.SetCreativeModeCarriedItemPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
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

@Environment(EnvType.CLIENT)
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public abstract class CreativeInventoryScreenItemPickerMenuMixin extends AbstractContainerMenu {

	protected CreativeInventoryScreenItemPickerMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
		super(menuType, containerId);
	}

	@Inject(method = "setCarried", at = @At("RETURN"))
	public void freezeFrame$onSetCarried(ItemStack carried, CallbackInfo info) {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) minecraft.player.setAttached(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM, carried.copy());
		ClientPlayNetworking.send(new SetCreativeModeCarriedItemPacket(carried.copy()));
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
		if (!instance.getItem().isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(instance.getItem().copy()));
			FFConstants.log("onDeleteFromQuickMove - CreativeModeInventoryScreen$ItemPickerMenu", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, itemStack);
	}
}
