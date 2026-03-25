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

package net.frozenblock.freezeframe.recipe;

import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FilmCapacityUpgradeRecipe extends CustomRecipe {

	public FilmCapacityUpgradeRecipe() {
		super();
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.width() != 3 || input.height() != 3) return false;

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				final ItemStack stack = input.getItem(x + (y * 3));
				if (x == 1 && y == 1) {
					if (!stack.is(FFItems.FILM)) return false;
					if (FilmItem.getMaxPhotographs(stack) >= FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS) return false;
					continue;
				}

				if (!stack.is(Items.PAPER)) return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		final ItemStack sourceFilm = input.getItem(4);
		if (!sourceFilm.is(FFItems.FILM)) return ItemStack.EMPTY;

		final int currentMax = FilmItem.getMaxPhotographs(sourceFilm);
		if (currentMax >= FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS) return ItemStack.EMPTY;

		final int upgradedMax = Math.min(currentMax + FilmContents.CAPACITY_INCREASE_PER_UPGRADE, FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS);
		final ItemStack upgradedFilm = sourceFilm.copyWithCount(1);
		upgradedFilm.set(FFDataComponents.FILM_MAX_PHOTOGRAPHS, upgradedMax);
		FilmItem.refreshStackingState(upgradedFilm);
		return upgradedFilm;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return FFRecipeSerializers.FILM_CAPACITY_UPGRADE;
	}
}
