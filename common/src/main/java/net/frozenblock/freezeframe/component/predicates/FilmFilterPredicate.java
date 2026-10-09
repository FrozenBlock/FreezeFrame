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

package net.frozenblock.freezeframe.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.advancements.predicates.CollectionContentsPredicate;
import net.minecraft.advancements.predicates.CollectionPredicate;
import net.minecraft.advancements.predicates.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;

public record FilmFilterPredicate(Optional<CollectionPredicate<FilmFilter.Layer, LayerPredicate>> layers) implements SingleComponentItemPredicate<FilmFilter> {
	public static final Codec<FilmFilterPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		CollectionPredicate.codec(LayerPredicate.CODEC).optionalFieldOf("layers").forGetter(FilmFilterPredicate::layers)
	).apply(instance, FilmFilterPredicate::new));

	@Override
	public DataComponentType<FilmFilter> componentType() {
		return FFDataComponents.FILM_FILTER.get();
	}

	public static FilmFilterPredicate containsOnly(LayerPredicate layerPredicate) {
		return new FilmFilterPredicate(
			Optional.of(
				new CollectionPredicate<>(
					Optional.of(
						new CollectionContentsPredicate.Multiple<>(List.of(layerPredicate))
					),
					Optional.empty(),
					Optional.empty()
				)
			)
		);
	}

	@Override
	public boolean matches(FilmFilter filmFilter) {
		return this.layers.isEmpty() || this.layers.get().test(filmFilter.layers());
	}

	public record LayerPredicate(FilmFilter.Layer layer) implements Predicate<FilmFilter.Layer> {
		public static final Codec<LayerPredicate> CODEC = FilmFilter.Layer.CODEC.xmap(LayerPredicate::new, LayerPredicate::layer);

		@Override
		public boolean test(FilmFilter.Layer layer) {
			return layer.equals(this.layer);
		}
	}
}
