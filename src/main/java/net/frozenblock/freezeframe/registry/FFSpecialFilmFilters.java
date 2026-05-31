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

package net.frozenblock.freezeframe.registry;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class FFSpecialFilmFilters {

	public static void bootstrap(BootstrapContext<SpecialFilmFilter> context) {
		register(context, "bloom", Ingredient.of(Items.SLIME_BALL), SpecialFilmFilter.Operation.BLOOM, "post/bloom");
		register(context, "chromatic_aberration", Ingredient.of(Items.DIAMOND), SpecialFilmFilter.Operation.CHROMATIC_ABERRATION, "post/chromatic_aberration");
		register(context, "crunchy", Ingredient.of(Items.CREEPER_HEAD), SpecialFilmFilter.Operation.CRUNCHY, "post/crunchy");
		register(context, "desaturate", Ingredient.of(Items.ZOMBIE_HEAD), SpecialFilmFilter.Operation.NONE, "post/desaturate");
		register(context, "gilded", Ingredient.of(Items.PIGLIN_HEAD), SpecialFilmFilter.Operation.NONE, "post/gilded");
		register(context, "high_contrast", Ingredient.of(Items.HONEY_BOTTLE), SpecialFilmFilter.Operation.HIGH_CONTRAST, "post/contrast");
		register(context, "invert", Ingredient.of(Items.ENDER_PEARL), SpecialFilmFilter.Operation.NONE, "post/invert");
		register(context, "monochrome", Ingredient.of(Items.SKELETON_SKULL), SpecialFilmFilter.Operation.NONE, "post/monochrome");
		register(context, "sapped", Ingredient.of(Items.RESIN_CLUMP), SpecialFilmFilter.Operation.SAPPED, "post/tint_shift");
		register(context, "spider", Ingredient.of(Items.SPIDER_EYE), SpecialFilmFilter.Operation.NONE, "post/spider_simple");
		register(context, "temperature_down", Ingredient.of(Items.SNOWBALL), SpecialFilmFilter.Operation.TEMPERATURE_DOWN, "post/temperature");
		register(context, "temperature_up", Ingredient.of(Items.MAGMA_CREAM), SpecialFilmFilter.Operation.TEMPERATURE_UP, "post/temperature");
		register(context, "triple_vision", Ingredient.of(Items.WITHER_SKELETON_SKULL), SpecialFilmFilter.Operation.TRIPLE_VISION, "post/triple_vision");
		register(context, "warding", Ingredient.of(Items.ECHO_SHARD), SpecialFilmFilter.Operation.WARDING, "post/tint_shift");
	}

	private static void register(
		BootstrapContext<SpecialFilmFilter> context,
		String name,
		Ingredient ingredient,
		SpecialFilmFilter.Operation operation,
		String shader
	) {
		register(context, FFConstants.id(name), ingredient, operation, FFConstants.id(shader));
	}

	public static void register(
		BootstrapContext<SpecialFilmFilter> context,
		Identifier id,
		Ingredient ingredient,
		SpecialFilmFilter.Operation operation,
		Identifier shader
	) {
		context.register(ResourceKey.create(FFRegistries.SPECIAL_FILM_FILTER, id), new SpecialFilmFilter(ingredient, operation, shader));
	}
}
