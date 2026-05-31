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
import java.util.List;
import java.util.Map;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class FilmCapacityUpgradeRecipe extends CustomRecipe {
	public static final MapCodec<FilmCapacityUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("film").forGetter(recipe -> recipe.film),
		Ingredient.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
		CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.craftingBookInfo),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
	).apply(instance, FilmCapacityUpgradeRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmCapacityUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.film,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.material,
		CraftingBookInfo.STREAM_CODEC, recipe -> recipe.craftingBookInfo,
		ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
		FilmCapacityUpgradeRecipe::new
	);
	public static final RecipeSerializer<FilmCapacityUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
	private final ShapedRecipePattern pattern;
	private final Ingredient film;
	private final Ingredient material;
	private final CraftingBookInfo craftingBookInfo;
	private final ItemStackTemplate result;
	@Nullable
	private PlacementInfo placementInfo;

	public FilmCapacityUpgradeRecipe(Ingredient film, Ingredient material, CraftingBookInfo craftingBookInfo, ItemStackTemplate result) {
		this.film = film;
		this.material = material;
		this.result = result;
		this.craftingBookInfo = craftingBookInfo;
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

	@Override
	public boolean isSpecial() {
		return false;
	}

	@Override
	public String group() {
		return this.craftingBookInfo.group();
	}

	@Override
	public CraftingBookCategory category() {
		return this.craftingBookInfo.category();
	}

	protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.createFromOptionals(this.pattern.ingredients());
	}

	@Override
	public final PlacementInfo placementInfo() {
		if (this.placementInfo == null) this.placementInfo = this.createPlacementInfo();
		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new ShapedCraftingRecipeDisplay(
				this.pattern.width(),
				this.pattern.height(),
				this.pattern.ingredients().stream().map(ingredient -> ingredient.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
			)
		);
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
