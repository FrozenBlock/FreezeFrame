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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
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

public class FilmFilterUpgradeRecipe extends CustomRecipe {
	public static final MapCodec<FilmFilterUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("film").forGetter(recipe -> recipe.film),
		Ingredient.CODEC.fieldOf("exclusion_tint_material").forGetter(recipe -> recipe.exclusionTintMaterial),
		Ingredient.CODEC.fieldOf("dye").forGetter(recipe -> recipe.dye),
		SpecialFilmFilter.REGISTRY_CODEC.optionalFieldOf("special_filter_material").forGetter(recipe -> recipe.specialFilter),
		Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
		ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
	).apply(instance, FilmFilterUpgradeRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmFilterUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.film,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.exclusionTintMaterial,
		Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.dye,
		ByteBufCodecs.optional(SpecialFilmFilter.STREAM_CODEC), recipe -> recipe.specialFilter,
		ByteBufCodecs.STRING_UTF8, recipe -> recipe.group,
		ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
		FilmFilterUpgradeRecipe::new
	);
	public static final RecipeSerializer<FilmFilterUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
	private final Ingredient film;
	private final Ingredient exclusionTintMaterial;
	private final Ingredient dye;
	private final Optional<Holder<SpecialFilmFilter>> specialFilter;
	private final String group;
	private final ItemStackTemplate result;
	@Nullable
	private PlacementInfo placementInfo;

	public FilmFilterUpgradeRecipe(
		Ingredient film,
		Ingredient exclusionTintMaterial,
		Ingredient dye,
		Optional<Holder<SpecialFilmFilter>> specialFilter,
		String group,
		ItemStackTemplate result
	) {
		this.film = film;
		this.exclusionTintMaterial = exclusionTintMaterial;
		this.dye = dye;
		this.specialFilter = specialFilter;
		this.group = group;
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
	public String group() {
		return this.group;
	}

	protected PlacementInfo createPlacementInfo() {
		if (this.specialFilter.isPresent()) return PlacementInfo.create(List.of(this.film, this.specialFilter.get().value().ingredient()));
		return PlacementInfo.create(List.of(this.film, this.dye, this.exclusionTintMaterial));
	}

	@Override
	public final PlacementInfo placementInfo() {
		if (this.placementInfo == null) this.placementInfo = this.createPlacementInfo();
		return this.placementInfo;
	}

	@Override
	public List<RecipeDisplay> display() {
		return List.of(
			new ShapelessCraftingRecipeDisplay(
				List.of(this.film.display(), this.dye.display(), this.exclusionTintMaterial.display()),
				new SlotDisplay.ItemStackSlotDisplay(this.result),
				new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
			)
		);
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return FFRecipeSerializers.FILM_FILTER_UPGRADE;
	}

	private ItemStack assembleInternal(CraftingInput input) {
		final ItemStack targetStack = this.findFilm(input);
		if (targetStack == null || targetStack.isEmpty()) return ItemStack.EMPTY;

		FilmFilter filter = FilmItem.getFilter(targetStack);

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
		if (this.specialFilter.isPresent() && (colorCount > 0 || exclusionTintCount > 0)) return ItemStack.EMPTY;
		if (this.specialFilter.isEmpty() && colorCount == 0) return ItemStack.EMPTY;
		if (exclusionTintCount > 1) return ItemStack.EMPTY;
		if (!filter.canAddLayer()) return ItemStack.EMPTY;

		if (this.specialFilter.isPresent()) {
			if (filter.hasSpecialOfType(this.specialFilter.get())) return ItemStack.EMPTY;
			filter = filter.addLayer(FilmFilter.Layer.special(this.specialFilter.get()));
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
		int count = 0;
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.exclusionTintMaterial.test(itemStack)) count += 1;
		}
		return count;
	}

	private List<DyeColor> findDyes(CraftingInput input) {
		final List<DyeColor> dyes = new ArrayList<>();
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.dye.test(itemStack))dyes.add(itemStack.getOrDefault(DataComponents.DYE, DyeColor.WHITE));
		}
		return dyes;
	}

	private ItemStack findSpecialFilterMaterial(CraftingInput input) {
		if (this.specialFilter.isEmpty()) return ItemStack.EMPTY;

		ItemStack specialFilterMaterial = ItemStack.EMPTY;
		for (int i = 0; i < input.size(); i++) {
			final ItemStack itemStack = input.getItem(i);
			if (this.specialFilter.get().value().ingredient().test(itemStack)) {
				if (specialFilterMaterial.isEmpty()) {
					specialFilterMaterial = itemStack;
				} else {
					return ItemStack.EMPTY;
				}
			}
		}
		return specialFilterMaterial;
	}
}
