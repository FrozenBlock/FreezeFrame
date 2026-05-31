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

package net.frozenblock.freezeframe.item.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.frozenblock.freezeframe.registry.FFRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.ChatFormatting;

public record SpecialFilmFilter(Ingredient ingredient, Operation operation, Identifier shader) {
	public static final Codec<SpecialFilmFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Ingredient.CODEC.fieldOf("ingredient").forGetter(SpecialFilmFilter::ingredient),
		Operation.CODEC.optionalFieldOf("operation", Operation.CUSTOM).forGetter(SpecialFilmFilter::operation),
		Identifier.CODEC.fieldOf("shader").forGetter(SpecialFilmFilter::shader)
	).apply(instance, SpecialFilmFilter::new));
	public static final Codec<Holder<SpecialFilmFilter>> REGISTRY_CODEC = RegistryFixedCodec.create(FFRegistries.SPECIAL_FILM_FILTER);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SpecialFilmFilter>> STREAM_CODEC = ByteBufCodecs.holderRegistry(FFRegistries.SPECIAL_FILM_FILTER);

	public static Component tooltipComponent(Holder<SpecialFilmFilter> specialFilmFilter) {
		final String path = specialFilmFilter.unwrapKey().map(key -> key.identifier().getPath()).orElse("unknown");
		return Component.translatable("item.freezeframe.film.filter.effect" + path).withStyle(ChatFormatting.GRAY);
	}

	public enum Operation implements StringRepresentable {
		BLOOM("bloom"),
		CHROMATIC_ABERRATION("chromatic_aberration"),
		CRUNCHY("crunchy"),
		DESATURATE("desaturate"),
		GILDED("gilded"),
		HIGH_CONTRAST("high_contrast"),
		INVERT("invert"),
		MONOCHROME("monochrome"),
		TEMPERATURE_UP("temperature_up"),
		TEMPERATURE_DOWN("temperature_down"),
		TRIPLE_VISION("triple_vision"),
		SAPPED("sapped"),
		SPIDER("spider"),
		WARDING("warding"),
		CUSTOM("custom"),
		NONE("none");
		public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);
		private final String name;

		Operation(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
