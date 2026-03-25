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

package net.frozenblock.freezeframe.datagen.recipe;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.frozenblock.lib.recipe.api.RecipeExportNamespaceFix;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.recipe.FilmCapacityUpgradeRecipe;
import net.frozenblock.freezeframe.registry.FFItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Contract;

public final class FFRecipeProvider extends FabricRecipeProvider {

	public FFRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Contract("_, _ -> new")
	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput exporter) {
		return new RecipeProvider(registries, exporter) {
			@Override
			public void buildRecipes() {
				RecipeExportNamespaceFix.setCurrentGeneratingModId(FFConstants.MOD_ID);

				this.shapeless(RecipeCategory.TOOLS, FFItems.FILM)
					.requires(Items.PAPER, 3)
					.requires(Items.COPPER_INGOT)
					.unlockedBy("has_camera", this.has(FFItems.CAMERA))
					.save(this.output);

				this.shaped(RecipeCategory.TOOLS, FFItems.CAMERA)
					.define('S', Ingredient.of(Items.STICK))
					.define('#', Ingredient.of(this.registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.PLANKS)))
					.define('A', Ingredient.of(Items.AMETHYST_SHARD))
					.pattern("S#S")
					.pattern("#A#")
					.pattern("S#S")
					.unlockedBy(RecipeProvider.getHasName(Items.AMETHYST_SHARD), this.has(Items.AMETHYST_SHARD))
					.save(exporter);

				this.shaped(RecipeCategory.TOOLS, FFItems.DISC_CAMERA)
					.define('#', Ingredient.of(FFItems.CAMERA))
					.define('X', Ingredient.of(this.registries.lookupOrThrow(Registries.ITEM).getOrThrow(ConventionalItemTags.MUSIC_DISCS)))
					.pattern("#")
					.pattern("X")
					.unlockedBy(RecipeProvider.getHasName(FFItems.DISC_CAMERA), this.has(FFItems.DISC_CAMERA))
					.save(exporter);

				SpecialRecipeBuilder.special(FilmCapacityUpgradeRecipe::new)
					.unlockedBy("has_film", this.has(FFItems.FILM))
					.save(this.output, "film_capacity_upgrade");

				RecipeExportNamespaceFix.clearCurrentGeneratingModId();
			}
		};
	}

	@Override
	public String getName() {
		return "Freeze Frame Recipes";
	}
}
