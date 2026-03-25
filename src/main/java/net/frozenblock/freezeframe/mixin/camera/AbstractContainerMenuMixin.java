/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
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

package net.frozenblock.freezeframe.mixin.camera;

import net.frozenblock.freezeframe.item.CameraItem;
import net.frozenblock.freezeframe.menu.impl.ContainerMenuCameraInterface;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements ContainerMenuCameraInterface {

	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Unique
	@Override
	public void freezeFrame$setSelectedCameraFilmIndex(int slotIndex, int selectedFilmIndex) {
		if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
		final ItemStack stack = this.slots.get(slotIndex).getItem();
		CameraItem.toggleSelectedItem(stack, selectedFilmIndex);
	}
}
