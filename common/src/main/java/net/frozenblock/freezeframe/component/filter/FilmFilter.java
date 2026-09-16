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

package net.frozenblock.freezeframe.component.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;

public record FilmFilter(List<Layer> layers) {
	public static final int MAX_LAYERS = 8;
	public static final FilmFilter EMPTY = new FilmFilter(List.of());
	public static final Codec<FilmFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Layer.CODEC.listOf().optionalFieldOf("layers", List.of()).forGetter(FilmFilter::layers)
	).apply(instance, FilmFilter::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmFilter> STREAM_CODEC = StreamCodec.composite(
		Layer.STREAM_CODEC.apply(ByteBufCodecs.list()), FilmFilter::layers,
		FilmFilter::new
	);

	public boolean isEmpty() {
		return this.layers.isEmpty();
	}

	public int size() {
		return this.layers.size();
	}

	public boolean canAddLayer() {
		return this.size() < MAX_LAYERS;
	}

	public boolean hasSpecialOfType(Holder<SpecialFilmFilter> specialFilmFilter) {
		for (Layer layer : this.layers) {
			if (!layer.isSpecial()) continue;
			if (layer.specialFilmFilter.isEmpty()) continue;
			if (layer.specialFilmFilter.get().is(specialFilmFilter)) return true;
		}
		return false;
	}

	public FilmFilter addLayer(Layer layer) {
		if (!this.canAddLayer()) return this;
		final List<Layer> newLayers = new java.util.ArrayList<>(this.layers);
		newLayers.add(layer);
		return new FilmFilter(newLayers);
	}

	public static FilmFilter dyeDemo(DyeColor dyeColor, boolean exclusionTint) {
		return new FilmFilter(List.of(Layer.dye(dyeColor.getTextureDiffuseColor(), exclusionTint)));
	}

	public static FilmFilter specialDemo(Holder<SpecialFilmFilter> specialFilmFilter) {
		return new FilmFilter(List.of(Layer.special(specialFilmFilter)));
	}

	public record Layer(LayerType type, int color, Optional<Holder<SpecialFilmFilter>> specialFilmFilter, boolean exclusionTint) {
		public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LayerType.CODEC.fieldOf("type").forGetter(Layer::type),
			Codec.INT.optionalFieldOf("color", 0).forGetter(Layer::color),
			SpecialFilmFilter.REGISTRY_CODEC.optionalFieldOf("special_film_filter").forGetter(Layer::specialFilmFilter),
			Codec.BOOL.optionalFieldOf("exclusion_tint", false).forGetter(Layer::exclusionTint)
		).apply(instance, Layer::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, Layer> STREAM_CODEC = StreamCodec.composite(
			LayerType.STREAM_CODEC, Layer::type,
			ByteBufCodecs.VAR_INT, Layer::color,
			ByteBufCodecs.optional(SpecialFilmFilter.STREAM_CODEC), Layer::specialFilmFilter,
			ByteBufCodecs.BOOL, Layer::exclusionTint,
			Layer::new
		);

		public static Layer dye(int color, boolean exclusionTint) {
			return new Layer(LayerType.DYE, color, Optional.empty(), exclusionTint);
		}

		public static Layer special(Holder<SpecialFilmFilter> specialFilmFilter) {
			return new Layer(LayerType.SPECIAL, 0, Optional.of(specialFilmFilter), false);
		}

		public boolean isDye() {
			return this.type == LayerType.DYE;
		}

		public boolean isSpecial() {
			return this.type == LayerType.SPECIAL;
		}
	}

	public enum LayerType implements StringRepresentable {
		DYE("dye"),
		SPECIAL("special");
		public static final Codec<LayerType> CODEC = StringRepresentable.fromEnum(LayerType::values);
		public static final StreamCodec<ByteBuf, LayerType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
			string -> CODEC.parse(JavaOps.INSTANCE, string).result().orElseThrow(),
			LayerType::getSerializedName
		);
		private final String name;

		LayerType(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
