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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.filter.SpecialFilmFilterDefinition;
import net.frozenblock.freezeframe.filter.SpecialFilmFilterRegistry;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.level.Level;

public class FilmFilterUpgradeRecipe extends CustomRecipe {
	public static final MapCodec<FilmFilterUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Ingredient.CODEC.fieldOf("target").forGetter(recipe -> recipe.target),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
	).apply(i, FilmFilterUpgradeRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmFilterUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.target,
		ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
		FilmFilterUpgradeRecipe::new
	);
	public static final RecipeSerializer<FilmFilterUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
	private final Ingredient target;
	private final ItemStackTemplate result;

	public FilmFilterUpgradeRecipe(Ingredient target, ItemStackTemplate result) {
		this.target = target;
		this.result = result;
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
		ItemStack targetStack = ItemStack.EMPTY;
		FilmFilter filter = FilmFilter.EMPTY;
		SpecialFilmFilterDefinition specialDefinition = null;
		int redTotal = 0;
		int greenTotal = 0;
		int blueTotal = 0;
		int colorCount = 0;
		int amethystCount = 0;

		for (int i = 0; i < input.size(); i++) {
			final ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;

			if (this.target.test(stack)) {
				if (!targetStack.isEmpty()) return ItemStack.EMPTY;
				targetStack = stack;
				filter = FilmItem.getFilter(stack);
				continue;
			}

			if (stack.getItem() instanceof DyeItem) {
				final DyeColor dye = FilmItem.getDyeColor(stack);
				if (dye == null) return ItemStack.EMPTY;

				final int color = dye.getTextureDiffuseColor();
				final int red = ARGB.red(color);
				final int green = ARGB.green(color);
				final int blue = ARGB.blue(color);
				redTotal += red;
				greenTotal += green;
				blueTotal += blue;
				colorCount++;
				continue;
			}

			if (stack.is(Items.AMETHYST_SHARD)) {
				amethystCount++;
				continue;
			}

			final SpecialFilmFilterDefinition definition = SpecialFilmFilterRegistry.getByIngredient(stack.getItem());
			if (definition == null) return ItemStack.EMPTY;
			// TODO: what? it's always null
			if (specialDefinition != null) return ItemStack.EMPTY;
			specialDefinition = definition;
		}

		if (targetStack.isEmpty()) return ItemStack.EMPTY;
		if (specialDefinition != null && (colorCount > 0 || amethystCount > 0)) return ItemStack.EMPTY;
		if (specialDefinition == null && colorCount == 0) return ItemStack.EMPTY;
		if (amethystCount > 1) return ItemStack.EMPTY;
		if (!filter.canAddLayer()) return ItemStack.EMPTY;

		if (specialDefinition != null) {
			final Identifier specialId = specialDefinition.id();
			if (filter.hasSpecial(specialId)) return ItemStack.EMPTY;
			filter = filter.addLayer(FilmFilter.Layer.special(specialId));
		} else {
			final int red = redTotal / colorCount;
			final int green = greenTotal / colorCount;
			final int blue = blueTotal / colorCount;
			final int rgb  = ARGB.color(0, red, green, blue);
			filter = filter.addLayer(FilmFilter.Layer.dye(rgb, amethystCount == 1));
		}

		final ItemStack output = TransmuteRecipe.createWithOriginalComponents(this.result, targetStack);
		output.set(FFDataComponents.FILM_FILTER, filter);
		FilmItem.refreshStackingState(output);
		return output;
	}
}
