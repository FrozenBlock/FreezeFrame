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

package net.frozenblock.freezeframe.menu;

import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DevelopingTableSourceSlot extends Slot {
	private static final int MAX_SOURCE_STACK_SIZE = 1;

	public DevelopingTableSourceSlot(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return isValidAsSource(stack);
	}

	@Override
	public int getMaxStackSize() {
		return MAX_SOURCE_STACK_SIZE;
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return MAX_SOURCE_STACK_SIZE;
	}

	public static boolean isValidAsSource(ItemStack stack) {
		return isValidFilmForPrinting(stack) || isValidPhotographForCopying(stack);
	}

	public static boolean isValidFilmForPrinting(ItemStack stack) {
		return stack.is(FFItems.FILM);
	}

	public static boolean isValidPhotographForCopying(ItemStack stack) {
		return stack.is(FFItems.PHOTOGRAPH)
			&& stack.has(FFDataComponents.PHOTOGRAPH)
			&& stack.get(FFDataComponents.PHOTOGRAPH).canCopy();
	}
}
