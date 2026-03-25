/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
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
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;

public record ScopeZoomConfig(
	float minZoom,
	float maxZoom,
	float zoomIncrement,
	float defaultZoom,
	boolean offhandEnabled,
	Optional<Holder<SoundEvent>> zoomInSound,
	Optional<Holder<SoundEvent>> zoomOutSound
) {
	public static final Codec<ScopeZoomConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.FLOAT.fieldOf("min_zoom").forGetter(ScopeZoomConfig::minZoom),
		Codec.FLOAT.fieldOf("max_zoom").forGetter(ScopeZoomConfig::maxZoom),
		Codec.FLOAT.fieldOf("zoom_increment").forGetter(ScopeZoomConfig::zoomIncrement),
		Codec.FLOAT.fieldOf("default_zoom").forGetter(ScopeZoomConfig::defaultZoom),
		Codec.BOOL.fieldOf("offhand_enabled").forGetter(ScopeZoomConfig::offhandEnabled),
		SoundEvent.CODEC.optionalFieldOf("zoom_in_sound").forGetter(ScopeZoomConfig::zoomInSound),
		SoundEvent.CODEC.optionalFieldOf("zoom_out_sound").forGetter(ScopeZoomConfig::zoomOutSound)
	).apply(instance, ScopeZoomConfig::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ScopeZoomConfig> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, ScopeZoomConfig::minZoom,
		ByteBufCodecs.FLOAT, ScopeZoomConfig::maxZoom,
		ByteBufCodecs.FLOAT, ScopeZoomConfig::zoomIncrement,
		ByteBufCodecs.FLOAT, ScopeZoomConfig::defaultZoom,
		ByteBufCodecs.BOOL, ScopeZoomConfig::offhandEnabled,
		SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), ScopeZoomConfig::zoomInSound,
		SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), ScopeZoomConfig::zoomOutSound,
		ScopeZoomConfig::new
	);
	public static final ScopeZoomConfig EMPTY = new ScopeZoomConfig(1F, 1F, 0F, 1F, false, Optional.empty(), Optional.empty());
}
