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

package net.frozenblock.freezeframe.data.recipe;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.frozenblock.freezeframe.recipe.FilmCapacityUpgradeRecipe;
import net.frozenblock.freezeframe.recipe.FilmFilterUpgradeRecipe;
import net.frozenblock.freezeframe.registry.FFBlocks;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFRegistries;
import net.frozenblock.lib.recipe.api.RecipeExportNamespaceFix;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class FFRecipeProvider extends FabricRecipeProvider {

	public FFRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		return new RecipeProvider(registries, output) {
			@Override
			public void buildRecipes() {
				RecipeExportNamespaceFix.setCurrentGeneratingModId(FFConstants.MOD_ID);
				final HolderLookup.RegistryLookup<Item> items = this.registries.lookupOrThrow(Registries.ITEM);

				this.shapeless(RecipeCategory.TOOLS, FFItems.FILM)
					.requires(Items.PAPER, 3)
					.requires(Items.COPPER_INGOT)
					.unlockedBy(RecipeProvider.getHasName(FFItems.CAMERA), this.has(FFItems.CAMERA))
					.unlockedBy(RecipeProvider.getHasName(Items.PAPER), this.has(Items.PAPER))
					.save(this.output);

				this.shaped(RecipeCategory.TOOLS, FFItems.CAMERA)
					.define('#', Ingredient.of(items.getOrThrow(ItemTags.PLANKS)))
					.define('A', Ingredient.of(Items.AMETHYST_SHARD))
					.define('G', Ingredient.of(Items.GOLD_INGOT))
					.define('R', Ingredient.of(Items.REDSTONE))
					.pattern("##G")
					.pattern("R A")
					.pattern("##G")
					.unlockedBy(RecipeProvider.getHasName(Items.AMETHYST_SHARD), this.has(Items.AMETHYST_SHARD))
					.save(this.output);

				this.shaped(RecipeCategory.TOOLS, FFBlocks.DEVELOPING_TABLE)
					.define('#', Ingredient.of(items.getOrThrow(ItemTags.PLANKS)))
					.define('S', Ingredient.of(Items.STONE))
					.define('R', Ingredient.of(Items.RED_DYE))
					.define('B', Ingredient.of(Items.BLUE_DYE))
					.define('Y', Ingredient.of(Items.YELLOW_DYE))
					.pattern("SSR")
					.pattern("##B")
					.pattern("##Y")
					.unlockedBy(RecipeProvider.getHasName(Items.STONE), this.has(Items.STONE))
					.save(this.output);

				SpecialRecipeBuilder.special(() -> new FilmCapacityUpgradeRecipe(
					Ingredient.of(FFItems.FILM),
					Ingredient.of(Items.PAPER),
					new ItemStackTemplate(FFItems.FILM)
				))
					.unlockedBy(RecipeProvider.getHasName(FFItems.FILM), this.has(FFItems.FILM))
					.save(this.output, "film_capacity_upgrade");

				try {
					createFilmFilterRecipe(items, this.output, this, Optional.empty(), "dye");

					this.registries.lookupOrThrow(FFRegistries.SPECIAL_FILM_FILTER).listElements().forEach(specialFilmFilter -> {
						try {
							createFilmFilterRecipe(items, this.output, this, Optional.of(specialFilmFilter), specialFilmFilter.key().identifier().getPath());
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					});
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}

				RecipeExportNamespaceFix.clearCurrentGeneratingModId();
			}
		};
	}

	public static void createFilmFilterRecipe(
		HolderLookup.RegistryLookup<Item> itemRegistry,
		RecipeOutput output,
		RecipeProvider provider,
		Optional<Holder<SpecialFilmFilter>> specialFilmFilter,
		String recipeSuffix
	) throws IllegalAccessException {
		if (StringUtil.isNullOrEmpty(recipeSuffix)) throw new IllegalAccessException("recipeSuffix cannot be empty!");

		SpecialRecipeBuilder.special(() -> new FilmFilterUpgradeRecipe(
				Ingredient.of(FFItems.FILM),
				Ingredient.of(Items.AMETHYST_SHARD),
				Ingredient.of(itemRegistry.getOrThrow(ItemTags.DYES)),
				specialFilmFilter,
				new ItemStackTemplate(FFItems.FILM)
			))
			.unlockedBy(RecipeProvider.getHasName(FFItems.FILM), provider.has(FFItems.FILM))
			.save(output, "film_filter_upgrade_" + recipeSuffix);
	}

	@Override
	public String getName() {
		return "Freeze Frame Recipes";
	}
}
