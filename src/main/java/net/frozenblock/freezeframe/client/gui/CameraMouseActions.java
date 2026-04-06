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

package net.frozenblock.freezeframe.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.item.CameraItem;
import net.frozenblock.freezeframe.networking.packet.SelectCameraFilmPacket;
import net.frozenblock.freezeframe.tag.FFItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class CameraMouseActions implements ItemSlotMouseAction {
	private final Minecraft minecraft;
	private final ScrollWheelHandler scrollWheelHandler;

	public CameraMouseActions(final Minecraft minecraft) {
		this.minecraft = minecraft;
		this.scrollWheelHandler = new ScrollWheelHandler();
	}

	@Override
	public boolean matches(Slot slot) {
		return slot.getItem().is(FFItemTags.CAMERAS);
	}

	@Override
	public boolean onMouseScrolled(final double scrollX, final double scrollY, final int slotIndex, final ItemStack itemStack) {
		int amountOfShownItems = CameraItem.getNumberOfItemsToShow(itemStack);
		if (amountOfShownItems == 0) return false;

		final Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		final int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
		if (wheel != 0) {
			final int selectedItem = CameraItem.getSelectedItemIndex(itemStack);
			final int updatedSelectedItem = ScrollWheelHandler.getNextScrollWheelSelection(wheel, selectedItem, amountOfShownItems);
			if (selectedItem != updatedSelectedItem) this.toggleSelectedCameraItem(itemStack, slotIndex, updatedSelectedItem);
		}

		return true;
	}

	@Override
	public void onStopHovering(Slot hoveredSlot) {
		this.unselectedCameraItem(hoveredSlot.getItem(), hoveredSlot.index);
	}

	@Override
	public void onSlotClicked(Slot slot, ContainerInput containerInput) {
		if (containerInput == ContainerInput.QUICK_MOVE || containerInput == ContainerInput.SWAP) this.unselectedCameraItem(slot.getItem(), slot.index);
	}

	private void toggleSelectedCameraItem(ItemStack cameraItem, int slotIndex, int selectedItem) {
		final ClientPacketListener connection = this.minecraft.getConnection();
		if (connection == null || selectedItem >= CameraItem.getNumberOfItemsToShow(cameraItem)) return;

		CameraItem.toggleSelectedItem(cameraItem, selectedItem);
		connection.send(new ServerboundCustomPayloadPacket(
			new SelectCameraFilmPacket(slotIndex, selectedItem)
		));
	}

	public void unselectedCameraItem(ItemStack cameraItem, int slotIndex) {
		this.toggleSelectedCameraItem(cameraItem, slotIndex, -1);
	}
}
