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
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.networking.packet.SelectFilmPhotographPacket;
import net.frozenblock.freezeframe.registry.FFItems;
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
public class FilmMouseActions implements ItemSlotMouseAction {
	private final Minecraft minecraft;
	private final ScrollWheelHandler scrollWheelHandler;

	public FilmMouseActions(final Minecraft minecraft) {
		this.minecraft = minecraft;
		this.scrollWheelHandler = new ScrollWheelHandler();
	}

	@Override
	public boolean matches(Slot slot) {
		return slot.getItem().is(FFItems.FILM);
	}

	@Override
	public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack stack) {
		int amountOfShownPhotographs = FilmItem.getNumberOfPhotographs(stack);
		if (amountOfShownPhotographs == 0) return false;

		final Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		final int wheel = wheelXY.y == 0 ? wheelXY.x : -wheelXY.y;
		if (wheel != 0) {
			final int selectedPhotograph = FilmItem.getSelectedPhotographIndex(stack);
			final int updatedSelectedPhotograph = ScrollWheelHandler.getNextScrollWheelSelection(wheel, selectedPhotograph, amountOfShownPhotographs);
			if (selectedPhotograph != updatedSelectedPhotograph) this.toggledSelectedFilmPhotograph(stack, slotIndex, updatedSelectedPhotograph);
		}
		return true;
	}

	@Override
	public void onStopHovering(final Slot hoveredSlot) {
		this.unselectedFilmItem(hoveredSlot.getItem(), hoveredSlot.index);
	}

	@Override
	public void onSlotClicked(final Slot slot, final ContainerInput containerInput) {
		if (containerInput == ContainerInput.QUICK_MOVE || containerInput == ContainerInput.SWAP) this.unselectedFilmItem(slot.getItem(), slot.index);
	}

	private void toggledSelectedFilmPhotograph(ItemStack filmItem, int slotIndex, int selectedPhotograph) {
		final ClientPacketListener connection = this.minecraft.getConnection();
		if (connection == null || selectedPhotograph >= FilmItem.getNumberOfPhotographs(filmItem)) return;

		FilmItem.toggleSelectedPhotograph(filmItem, selectedPhotograph);
		connection.send(new ServerboundCustomPayloadPacket(
			new SelectFilmPhotographPacket(slotIndex, selectedPhotograph)
		));
	}

	public void unselectedFilmItem(ItemStack filmItem, int slotIndex) {
		this.toggledSelectedFilmPhotograph(filmItem, slotIndex, -1);
	}
}
