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

package net.frozenblock.freezeframe.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FilmFilter(List<Layer> layers) {
	public static final int MAX_LAYERS = 8;
	public static final FilmFilter EMPTY = new FilmFilter(List.of());

	public static final Codec<FilmFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(Layer.CODEC.listOf().optionalFieldOf("layers", List.of()).forGetter(FilmFilter::layers))
		.apply(instance, FilmFilter::new));
	public static final StreamCodec<ByteBuf, FilmFilter> STREAM_CODEC = StreamCodec.composite(
		Layer.STREAM_CODEC.apply(ByteBufCodecs.list()), FilmFilter::layers,
		FilmFilter::new
	);

	public FilmFilter {
		layers = List.copyOf(layers);
	}

	public boolean isEmpty() {
		return this.layers.isEmpty();
	}

	public int size() {
		return this.layers.size();
	}

	public boolean canAddLayer() {
		return this.size() < MAX_LAYERS;
	}

	public boolean hasSpecial(String specialId) {
		for (Layer layer : this.layers) {
			if (layer.isSpecial() && layer.specialId().equals(specialId)) return true;
		}
		return false;
	}

	public FilmFilter addLayer(Layer layer) {
		if (!this.canAddLayer()) return this;
		final List<Layer> newLayers = new java.util.ArrayList<>(this.layers);
		newLayers.add(layer);
		return new FilmFilter(newLayers);
	}

	public record Layer(LayerType type, int color, String specialId, boolean exclusionTint) {
		private static final Codec<LayerType> LAYER_TYPE_CODEC = Codec.STRING.xmap(value -> LayerType.valueOf(value.toUpperCase(Locale.ROOT)), layerType -> layerType.name().toLowerCase(Locale.ROOT));
		private static final StreamCodec<ByteBuf, LayerType> LAYER_TYPE_STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(value -> LayerType.valueOf(value.toUpperCase(Locale.ROOT)), layerType -> layerType.name().toLowerCase(Locale.ROOT));

		public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LAYER_TYPE_CODEC.fieldOf("type").forGetter(Layer::type),
			Codec.INT.optionalFieldOf("color", 0).forGetter(Layer::color),
			Codec.STRING.optionalFieldOf("special_id", "").forGetter(Layer::specialId),
			Codec.BOOL.optionalFieldOf("exclusion_tint", false).forGetter(Layer::exclusionTint)
		).apply(instance, Layer::new));
		public static final StreamCodec<ByteBuf, Layer> STREAM_CODEC = StreamCodec.composite(
			LAYER_TYPE_STREAM_CODEC, Layer::type,
			ByteBufCodecs.VAR_INT, Layer::color,
			ByteBufCodecs.STRING_UTF8, Layer::specialId,
			ByteBufCodecs.BOOL, Layer::exclusionTint,
			Layer::new
		);

		public static Layer dye(int color, boolean exclusionTint) {
			return new Layer(LayerType.DYE, color, "", exclusionTint);
		}

		public static Layer special(String specialId) {
			return new Layer(LayerType.SPECIAL, 0, specialId, false);
		}

		public boolean isDye() {
			return this.type == LayerType.DYE;
		}

		public boolean isSpecial() {
			return this.type == LayerType.SPECIAL;
		}
	}

	public enum LayerType {
		DYE,
		SPECIAL
	}
}
