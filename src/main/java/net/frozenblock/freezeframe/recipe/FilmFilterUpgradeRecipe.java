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

import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.filter.SpecialFilmFilterDefinition;
import net.frozenblock.freezeframe.filter.SpecialFilmFilterRegistry;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FilmFilterUpgradeRecipe extends CustomRecipe {

	public FilmFilterUpgradeRecipe() {
		super();
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return this.assembleInternal(input) != ItemStack.EMPTY;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return this.assembleInternal(input);
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return FFRecipeSerializers.FILM_FILTER_UPGRADE;
	}

	private ItemStack assembleInternal(CraftingInput input) {
		ItemStack film = ItemStack.EMPTY;
		FilmFilter filter = FilmFilter.EMPTY;
		SpecialFilmFilterDefinition specialDefinition = null;
		int dyeRed = 0;
		int dyeGreen = 0;
		int dyeBlue = 0;
		int dyeCount = 0;
		int amethystCount = 0;

		for (int i = 0; i < input.size(); i++) {
			final ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (stack.is(FFItems.FILM)) {
				if (!film.isEmpty()) return ItemStack.EMPTY;
				film = stack;
				filter = FilmItem.getFilter(stack);
				continue;
			}

			if (stack.getItem() instanceof DyeItem) {
				final DyeColor color = FilmItem.getDyeColor(stack);
				if (color == null) return ItemStack.EMPTY;
				final int rgb = color.getTextureDiffuseColor();
				dyeRed += (rgb >> 16) & 0xFF;
				dyeGreen += (rgb >> 8) & 0xFF;
				dyeBlue += rgb & 0xFF;
				dyeCount++;
				continue;
			}

			if (stack.is(Items.AMETHYST_SHARD)) {
				amethystCount++;
				continue;
			}

			final SpecialFilmFilterDefinition definition = SpecialFilmFilterRegistry.getByIngredient(stack.getItem());
			if (definition == null) return ItemStack.EMPTY;
			if (specialDefinition != null) return ItemStack.EMPTY;
			specialDefinition = definition;
		}

		if (film.isEmpty()) return ItemStack.EMPTY;
		if (specialDefinition != null && (dyeCount > 0 || amethystCount > 0)) return ItemStack.EMPTY;
		if (specialDefinition == null && dyeCount == 0) return ItemStack.EMPTY;
		if (amethystCount > 1) return ItemStack.EMPTY;
		if (!filter.canAddLayer()) return ItemStack.EMPTY;

		if (specialDefinition != null) {
			final String specialId = specialDefinition.id().toString();
			if (filter.hasSpecial(specialId)) return ItemStack.EMPTY;
			filter = filter.addLayer(FilmFilter.Layer.special(specialId));
		} else {
			final int color = ((dyeRed / dyeCount) << 16) | ((dyeGreen / dyeCount) << 8) | (dyeBlue / dyeCount);
			filter = filter.addLayer(FilmFilter.Layer.dye(color, amethystCount == 1));
		}

		final ItemStack output = film.copyWithCount(1);
		output.set(FFDataComponents.FILM_FILTER, filter);
		FilmItem.refreshStackingState(output);
		return output;
	}
}
