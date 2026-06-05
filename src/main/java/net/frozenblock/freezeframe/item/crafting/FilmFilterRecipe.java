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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.item.crafting.display.FilmDyeFilterSlotDisplay;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class FilmFilterRecipe extends CustomRecipe {
	public static final MapCodec<FilmFilterRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("film").forGetter(recipe -> recipe.film),
		Ingredient.CODEC.optionalFieldOf("exclusion_tint_material").forGetter(recipe -> recipe.exclusionTintMaterial),
		Ingredient.CODEC.optionalFieldOf("dye").forGetter(recipe -> recipe.dye),
		SpecialFilterAndIngredient.CODEC.optionalFieldOf("special_film_filter_and_ingredient").forGetter(recipe -> recipe.specialFilter),
		CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.craftingBookInfo),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
	).apply(instance, FilmFilterRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmFilterRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.film,
		ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), recipe -> recipe.exclusionTintMaterial,
		ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), recipe -> recipe.dye,
		ByteBufCodecs.optional(SpecialFilterAndIngredient.STREAM_CODEC), recipe -> recipe.specialFilter,
		CraftingBookInfo.STREAM_CODEC, recipe -> recipe.craftingBookInfo,
		ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
		FilmFilterRecipe::new
	);
	public static final RecipeSerializer<FilmFilterRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
	private final Ingredient film;
	private final Optional<Ingredient> exclusionTintMaterial;
	private final Optional<Ingredient> dye;
	private final Optional<SpecialFilterAndIngredient> specialFilter;
	private final CraftingBookInfo craftingBookInfo;
	private final ItemStackTemplate result;
	@Nullable
	private PlacementInfo placementInfo;

	public FilmFilterRecipe(
		Ingredient film,
		Optional<Ingredient> exclusionTintMaterial,
		Optional<Ingredient> dye,
		Optional<SpecialFilterAndIngredient> specialFilter,
		CraftingBookInfo craftingBookInfo,
		ItemStackTemplate result
	) {
		if (specialFilter.isPresent() && (exclusionTintMaterial.isPresent() || dye.isPresent())) {
			throw new IllegalArgumentException("Dye and exclusion tint ingredients cannot be present in a special film filter recipe!");
		}

		if (specialFilter.isEmpty() && dye.isEmpty()) {
			throw new IllegalArgumentException("Film filter recipes require at least one dye or special film filter ingredient!");
		}

		this.film = film;
		this.exclusionTintMaterial = exclusionTintMaterial;
		this.dye = dye;
		this.specialFilter = specialFilter;
		this.craftingBookInfo = craftingBookInfo;
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
	public boolean isSpecial() {
		return !FFConfig.FILM_FILTER_RECIPES.get();
	}

	@Override
	public boolean showNotification() {
		return this.specialFilter.isPresent() && FFConfig.FILM_FILTER_RECIPES.get();
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
		if (this.specialFilter.isPresent()) return PlacementInfo.create(List.of(this.film, this.specialFilter.get().ingredient()));
		if (this.exclusionTintMaterial.isPresent()) return PlacementInfo.create(List.of(this.film, this.dye.orElseThrow(), this.exclusionTintMaterial.orElseThrow()));
		return PlacementInfo.create(List.of(this.film, this.dye.orElseThrow()));
	}

	@Override
	public final PlacementInfo placementInfo() {
		if (this.placementInfo == null) this.placementInfo = this.createPlacementInfo();
		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		if (!FFConfig.FILM_FILTER_RECIPES.get()) return super.display();

		final SlotDisplay film = this.film.display();
		final SlotDisplay craftingStation = new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE);

		if (this.specialFilter.isPresent()) {
			return List.of(
				new ShapelessCraftingRecipeDisplay(
					List.of(film, this.specialFilter.get().ingredient().display()),
					new SlotDisplay.ItemStackSlotDisplay(
						ItemStackTemplate.fromNonEmptyStack(
							this.result.apply(
								DataComponentPatch.builder().set(
									FFDataComponents.FILM_FILTER,
									FilmFilter.specialDemo(this.specialFilter.get().specialFilmFilter())
								).build()
							)
						)
					),
					craftingStation
				)
			);
		}

		final SlotDisplay dye = this.dye.orElseThrow().display();
		final SlotDisplay result = new SlotDisplay.ItemStackSlotDisplay(this.result);
		return List.of(
			this.exclusionTintMaterial.isPresent()
				? new ShapelessCraftingRecipeDisplay(
					List.of(film, dye, this.exclusionTintMaterial.orElseThrow().display()),
					new FilmDyeFilterSlotDisplay(dye, result, true),
					craftingStation
				)
				: new ShapelessCraftingRecipeDisplay(
					List.of(film, dye),
					new FilmDyeFilterSlotDisplay(dye, result, false),
					craftingStation
				)
		);
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return FFRecipeSerializers.FILM_FILTER;
	}

	private ItemStack assembleInternal(CraftingInput input) {
		if (!FFConfig.FILM_FILTER_RECIPES.get()) return ItemStack.EMPTY;

		final ItemStack targetStack = this.findFilm(input);
		if (targetStack == null || targetStack.isEmpty()) return ItemStack.EMPTY;

		FilmFilter filter = FilmItem.getFilter(targetStack);
		if (!filter.canAddLayer()) return ItemStack.EMPTY;

		int redTotal = 0;
		int greenTotal = 0;
		int blueTotal = 0;
		int colorCount = 0;
		for (DyeColor dyeColor : this.findDyes(input)) {
			final int color = dyeColor.getTextureDiffuseColor();
			redTotal += ARGB.red(color);
			greenTotal += ARGB.green(color);
			blueTotal += ARGB.blue(color);
			colorCount++;
		}

		final int exclusionTintCount = this.countExclusionTintMaterials(input);

		final ItemStack specialFilterMaterial = this.findSpecialFilterMaterial(input);
		if (this.specialFilter.isPresent() && specialFilterMaterial.isEmpty()) return ItemStack.EMPTY;

		if (targetStack.isEmpty()) return ItemStack.EMPTY;
		if (this.hasNonMatchingItems(input)) return ItemStack.EMPTY;
		if (this.specialFilter.isEmpty() && colorCount == 0) return ItemStack.EMPTY;
		if (exclusionTintCount > 1) return ItemStack.EMPTY;

		if (this.specialFilter.isPresent()) {
			if (filter.hasSpecialOfType(this.specialFilter.get().specialFilmFilter())) return ItemStack.EMPTY;
			filter = filter.addLayer(FilmFilter.Layer.special(this.specialFilter.get().specialFilmFilter()));
		} else {
			final int red = redTotal / colorCount;
			final int green = greenTotal / colorCount;
			final int blue = blueTotal / colorCount;
			final int rgb  = ARGB.color(0, red, green, blue);
			filter = filter.addLayer(FilmFilter.Layer.dye(rgb, exclusionTintCount == 1));
		}

		final ItemStack output = TransmuteRecipe.createWithOriginalComponents(this.result, targetStack);
		output.set(FFDataComponents.FILM_FILTER, filter);
		FilmItem.refreshStackingState(output);
		return output;
	}

	private ItemStack findFilm(CraftingInput input) {
		ItemStack film = ItemStack.EMPTY;
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.film.test(itemStack)) {
				if (film.isEmpty()) {
					film = itemStack;
				} else {
					return ItemStack.EMPTY;
				}
			}
		}
		return film;
	}

	private int countExclusionTintMaterials(CraftingInput input) {
		if (this.exclusionTintMaterial.isEmpty()) return 0;

		int count = 0;
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.exclusionTintMaterial.orElseThrow().test(itemStack)) count += 1;
		}
		return count;
	}

	private List<DyeColor> findDyes(CraftingInput input) {
		if (this.dye.isEmpty()) return List.of();

		final List<DyeColor> dyes = new ArrayList<>();
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.dye.orElseThrow().test(itemStack))dyes.add(itemStack.getOrDefault(DataComponents.DYE, DyeColor.WHITE));
		}
		return dyes;
	}

	private ItemStack findSpecialFilterMaterial(CraftingInput input) {
		if (this.specialFilter.isEmpty()) return ItemStack.EMPTY;

		ItemStack specialFilterMaterial = ItemStack.EMPTY;
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.specialFilter.get().ingredient().test(itemStack)) {
				if (specialFilterMaterial.isEmpty()) {
					specialFilterMaterial = itemStack;
				} else {
					return ItemStack.EMPTY;
				}
			}
		}
		return specialFilterMaterial;
	}

	private boolean hasNonMatchingItems(CraftingInput input) {
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (itemStack.isEmpty()) continue;
			if (this.film.test(itemStack)) continue;
			if (!this.specialFilter.map(specialFilter -> specialFilter.ingredient().test(itemStack)).orElse(true)) return true;
			if (!this.dye.map(ingredient -> ingredient.test(itemStack)).orElse(true)
				&& !this.exclusionTintMaterial.map(ingredient -> ingredient.test(itemStack)).orElse(false)
			) {
				return true;
			}
		}
		return false;
	}

	public record SpecialFilterAndIngredient(Holder<SpecialFilmFilter> specialFilmFilter, Ingredient ingredient) {
		public static final Codec<SpecialFilterAndIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SpecialFilmFilter.REGISTRY_CODEC.fieldOf("special_film_filter").forGetter(SpecialFilterAndIngredient::specialFilmFilter),
			Ingredient.CODEC.fieldOf("ingredient").forGetter(SpecialFilterAndIngredient::ingredient)
		).apply(instance, SpecialFilterAndIngredient::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, SpecialFilterAndIngredient> STREAM_CODEC = StreamCodec.composite(
			SpecialFilmFilter.STREAM_CODEC, SpecialFilterAndIngredient::specialFilmFilter,
			Ingredient.CONTENTS_STREAM_CODEC, SpecialFilterAndIngredient::ingredient,
			SpecialFilterAndIngredient::new
		);
	}
}
