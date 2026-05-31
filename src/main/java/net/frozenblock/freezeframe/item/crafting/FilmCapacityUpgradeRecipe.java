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

package net.frozenblock.freezeframe.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.level.Level;

public class FilmCapacityUpgradeRecipe extends CustomRecipe {
	public static final MapCodec<FilmCapacityUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		Ingredient.CODEC.fieldOf("film").forGetter(recipe -> recipe.film),
		Ingredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.film),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
	).apply(i, FilmCapacityUpgradeRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmCapacityUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.film,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.material,
		ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
		FilmCapacityUpgradeRecipe::new
	);
	public static final RecipeSerializer<FilmCapacityUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
	private final ShapedRecipePattern pattern;
	private final Ingredient film;
	private final Ingredient material;
	private final ItemStackTemplate result;

	public FilmCapacityUpgradeRecipe(Ingredient film, Ingredient material, ItemStackTemplate result) {
		this.film = film;
		this.material = material;
		this.result = result;
		this.pattern = ShapedRecipePattern.of(Map.of('#', material, 'I', film), "###", "#I#", "###");
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (!this.pattern.matches(input)) return false;

		final ItemStack film = this.findFilm(input);
		return !film.isEmpty() && FilmItem.getMaxPhotographs(film) < FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		final ItemStack sourceFilm = this.findFilm(input);
		if (sourceFilm.isEmpty()) return ItemStack.EMPTY;

		final int currentMax = FilmItem.getMaxPhotographs(sourceFilm);
		if (currentMax >= FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS) return ItemStack.EMPTY;

		final int upgradedMax = Math.min(currentMax + FilmContents.CAPACITY_INCREASE_PER_UPGRADE, FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS);
		final ItemStack upgradedFilm = TransmuteRecipe.createWithOriginalComponents(this.result, sourceFilm);
		upgradedFilm.set(FFDataComponents.FILM_MAX_PHOTOGRAPHS, upgradedMax);
		FilmItem.refreshStackingState(upgradedFilm);
		return upgradedFilm;
	}

	private ItemStack findFilm(CraftingInput input) {
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.film.test(itemStack)) return itemStack;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return FFRecipeSerializers.FILM_CAPACITY_UPGRADE;
	}
}
