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

public class FFSpecialFilmFilters {
	public static final ResourceKey<SpecialFilmFilter> BLOOM = createKey("bloom");
	public static final ResourceKey<SpecialFilmFilter> CHROMATIC_ABERRATION = createKey("chromatic_aberration");
	public static final ResourceKey<SpecialFilmFilter> CRUNCHY = createKey("crunchy");
	public static final ResourceKey<SpecialFilmFilter> DESATURATE = createKey("desaturate");
	public static final ResourceKey<SpecialFilmFilter> GILDED = createKey("gilded");
	public static final ResourceKey<SpecialFilmFilter> HIGH_CONTRAST = createKey("high_contrast");
	public static final ResourceKey<SpecialFilmFilter> INVERT = createKey("invert");
	public static final ResourceKey<SpecialFilmFilter> MONOCHROME = createKey("monochrome");
	public static final ResourceKey<SpecialFilmFilter> SAPPED = createKey("sapped");
	public static final ResourceKey<SpecialFilmFilter> SPIDER = createKey("spider");
	public static final ResourceKey<SpecialFilmFilter> TEMPERATURE_DOWN = createKey("temperature_down");
	public static final ResourceKey<SpecialFilmFilter> TEMPERATURE_UP = createKey("temperature_up");
	public static final ResourceKey<SpecialFilmFilter> TRIPLE_VISION = createKey("triple_vision");
	public static final ResourceKey<SpecialFilmFilter> WARDING = createKey("warding");

	public static void bootstrap(BootstrapContext<SpecialFilmFilter> context) {
		register(context, BLOOM, SpecialFilmFilter.Operation.BLOOM, "post/bloom");
		register(context, CHROMATIC_ABERRATION, SpecialFilmFilter.Operation.CHROMATIC_ABERRATION, "post/chromatic_aberration");
		register(context, CRUNCHY, SpecialFilmFilter.Operation.CRUNCHY, "post/crunchy");
		register(context, DESATURATE, SpecialFilmFilter.Operation.NONE, "post/desaturate");
		register(context, GILDED, SpecialFilmFilter.Operation.NONE, "post/gilded");
		register(context, HIGH_CONTRAST, SpecialFilmFilter.Operation.HIGH_CONTRAST, "post/contrast");
		register(context, INVERT, SpecialFilmFilter.Operation.NONE, "post/invert");
		register(context, MONOCHROME, SpecialFilmFilter.Operation.NONE, "post/monochrome");
		register(context, SAPPED, SpecialFilmFilter.Operation.SAPPED, "post/tint_shift");
		register(context, SPIDER, SpecialFilmFilter.Operation.NONE, "post/spider_simple");
		register(context, TEMPERATURE_DOWN, SpecialFilmFilter.Operation.TEMPERATURE_DOWN, "post/temperature");
		register(context, TEMPERATURE_UP, SpecialFilmFilter.Operation.TEMPERATURE_UP, "post/temperature");
		register(context, TRIPLE_VISION, SpecialFilmFilter.Operation.TRIPLE_VISION, "post/triple_vision");
		register(context, WARDING, SpecialFilmFilter.Operation.WARDING, "post/tint_shift");
	}

	private static void register(
		BootstrapContext<SpecialFilmFilter> context,
		ResourceKey<SpecialFilmFilter> key,
		SpecialFilmFilter.Operation operation,
		String shader
	) {
		register(context, key, operation, FFConstants.id(shader));
	}

	public static void register(
		BootstrapContext<SpecialFilmFilter> context,
		ResourceKey<SpecialFilmFilter> key,
		SpecialFilmFilter.Operation operation,
		Identifier shader
	) {
		context.register(key, new SpecialFilmFilter(operation, shader));
	}

	private static ResourceKey<SpecialFilmFilter> createKey(String name) {
		return createKey(FFConstants.id(name));
	}

	public static ResourceKey<SpecialFilmFilter> createKey(Identifier id) {
		return ResourceKey.create(FFRegistries.SPECIAL_FILM_FILTER, id);
	}
}
