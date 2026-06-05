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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import org.joml.Vector4f;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@Environment(EnvType.CLIENT)
	public static void bootstrap(BootstrapContext<SpecialFilmFilter> context) {
		register(context, BLOOM, Map.of("BloomConfig", List.of(new UniformValue.FloatUniform(0.55F))), "post/bloom");
		register(context, CHROMATIC_ABERRATION, Map.of("OffsetConfig", List.of(new UniformValue.FloatUniform(1F))), "post/chromatic_aberration");
		register(context, CRUNCHY, Map.of("CrunchConfig", List.of(new UniformValue.FloatUniform(2F), new UniformValue.FloatUniform(6F))), "post/crunchy");
		register(context, DESATURATE, Map.of(), "post/desaturate");
		register(context, GILDED, Map.of(), "post/gilded");
		register(context, HIGH_CONTRAST, Map.of("ContrastConfig", List.of(new UniformValue.FloatUniform(1.35F), new UniformValue.FloatUniform(0F))), "post/contrast");
		register(context, INVERT, Map.of(), "post/invert");
		register(context, MONOCHROME, Map.of(), "post/monochrome");
		register(context, SAPPED, tintShiftUniforms(0xEC7214, 0.68F, 1.18F, 0.88F), "post/tint_shift");
		register(context, SPIDER, Map.of(), "post/spider_simple");
		register(context, TEMPERATURE_DOWN, temperatureUniforms(-0.12F, 0.04F, 0.2F), "post/temperature");
		register(context, TEMPERATURE_UP, temperatureUniforms(0.18F, 0.02F, -0.12F), "post/temperature");
		register(context, TRIPLE_VISION, Map.of("OffsetConfig", List.of(new UniformValue.FloatUniform(2F))), "post/triple_vision");
		register(context, WARDING, tintShiftUniforms(0x29DFEB, 0.52F, 1.15F, 0.72F), "post/tint_shift");
	}

	@Environment(EnvType.CLIENT)
	public static Map<String, List<UniformValue>> temperatureUniforms(float red, float green, float blue) {
		return Map.of("TemperatureConfig", List.of(new UniformValue.Vec4Uniform(new Vector4f(red, green, blue, 0F))));
	}

	@Environment(EnvType.CLIENT)
	public static Map<String, List<UniformValue>> tintShiftUniforms(int color, float tintAmount, float contrastAmount, float saturationAmount) {
		final float red = ARGB.red(color) / 255F;
		final float green = ARGB.green(color) / 255F;
		final float blue = ARGB.blue(color) / 255F;
		return Map.of("TintShiftConfig", List.of(
			new UniformValue.Vec4Uniform(new Vector4f(red, green, blue, 1F)),
			new UniformValue.Vec4Uniform(new Vector4f(tintAmount, contrastAmount, saturationAmount, 0F))
		));
	}

	@Environment(EnvType.CLIENT)
	private static void register(
		BootstrapContext<SpecialFilmFilter> context,
		ResourceKey<SpecialFilmFilter> key,
		Map<String, List<UniformValue>> uniforms,
		String shader
	) {
		register(context, key, uniforms, FFConstants.id(shader));
	}

	@Environment(EnvType.CLIENT)
	public static void register(
		BootstrapContext<SpecialFilmFilter> context,
		ResourceKey<SpecialFilmFilter> key,
		Map<String, List<UniformValue>> uniforms,
		Identifier shader
	) {
		context.register(key, new SpecialFilmFilter(shader, Optional.of(uniforms)));
	}

	private static ResourceKey<SpecialFilmFilter> createKey(String name) {
		return createKey(FFConstants.id(name));
	}

	public static ResourceKey<SpecialFilmFilter> createKey(Identifier id) {
		return ResourceKey.create(FFRegistries.SPECIAL_FILM_FILTER, id);
	}
}
