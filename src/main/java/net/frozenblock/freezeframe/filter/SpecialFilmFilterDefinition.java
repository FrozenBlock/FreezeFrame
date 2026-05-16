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

package net.frozenblock.freezeframe.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;

public record SpecialFilmFilterDefinition(
	Identifier id,
	Identifier ingredient,
	String operation,
	String tooltipKey,
	String shader,
	Map<String, List<ConfiguredUniform>> uniforms
) {
	public static final Codec<SpecialFilmFilterDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("id").forGetter(SpecialFilmFilterDefinition::id),
		Identifier.CODEC.fieldOf("ingredient").forGetter(SpecialFilmFilterDefinition::ingredient),
		Codec.STRING.fieldOf("operation").forGetter(SpecialFilmFilterDefinition::operation),
		Codec.STRING.fieldOf("tooltip_key").forGetter(SpecialFilmFilterDefinition::tooltipKey),
		Codec.STRING.fieldOf("shader").forGetter(SpecialFilmFilterDefinition::shader),
		uniformsCodec()
	).apply(instance, SpecialFilmFilterDefinition::new));
	public static final Codec<SpecialFilmFilterDefinition> FILE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("ingredient").forGetter(SpecialFilmFilterDefinition::ingredient),
		Codec.STRING.optionalFieldOf("operation", "custom").forGetter(SpecialFilmFilterDefinition::operation),
		Codec.STRING.fieldOf("tooltip_key").forGetter(SpecialFilmFilterDefinition::tooltipKey),
		Codec.STRING.fieldOf("shader").forGetter(SpecialFilmFilterDefinition::shader),
		uniformsCodec()
	).apply(instance, (ingredient, operation, tooltipKey, shader, uniforms) -> new SpecialFilmFilterDefinition(Identifier.withDefaultNamespace("missing"), ingredient, operation, tooltipKey, shader, uniforms)));

	public SpecialFilmFilterDefinition withId(Identifier id) {
		return new SpecialFilmFilterDefinition(id, this.ingredient, this.operation, this.tooltipKey, this.shader, this.uniforms);
	}

	private static RecordCodecBuilder<SpecialFilmFilterDefinition, Map<String, List<ConfiguredUniform>>> uniformsCodec() {
		return Codec.unboundedMap(Codec.STRING, ConfiguredUniform.CODEC.listOf())
			.optionalFieldOf("uniforms", Map.of())
			.forGetter(SpecialFilmFilterDefinition::uniforms);
	}

	public record ConfiguredUniform(String type, List<Float> values) {
		public static final Codec<ConfiguredUniform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(ConfiguredUniform::type),
			Codec.FLOAT.listOf().fieldOf("values").forGetter(ConfiguredUniform::values)
		).apply(instance, ConfiguredUniform::new));
	}
}
