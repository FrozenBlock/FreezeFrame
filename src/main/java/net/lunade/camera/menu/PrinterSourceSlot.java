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

package net.lunade.camera.menu;

import net.lunade.camera.component.FilmContents;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PrinterSourceSlot extends Slot {

	public PrinterSourceSlot(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return isValidAsSource(stack);
	}

	public static boolean isValidAsSource(ItemStack stack) {
		return isValidFilmForPrinting(stack) || isValidPhotographForCopying(stack);
	}

	public static boolean isValidFilmForPrinting(ItemStack stack) {
		return stack.is(CameraPortItems.FILM) && !stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY).isEmpty();
	}

	public static boolean isValidPhotographForCopying(ItemStack stack) {
		return stack.is(CameraPortItems.PHOTOGRAPH) && stack.has(CameraPortDataComponents.PHOTOGRAPH) && !stack.get(CameraPortDataComponents.PHOTOGRAPH).isCopy();
	}
}
