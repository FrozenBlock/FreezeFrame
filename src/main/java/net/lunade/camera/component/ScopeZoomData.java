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

package net.lunade.camera.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ScopeZoomData(UUID lastUserUUID, float zoom) {
	public static final Codec<ScopeZoomData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		UUIDUtil.CODEC.fieldOf("last_user_uuid").forGetter(ScopeZoomData::lastUserUUID),
		Codec.FLOAT.fieldOf("zoom").forGetter(ScopeZoomData::zoom)
	).apply(instance, ScopeZoomData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ScopeZoomData> STREAM_CODEC = StreamCodec.composite(
		UUIDUtil.STREAM_CODEC, ScopeZoomData::lastUserUUID,
		ByteBufCodecs.FLOAT, ScopeZoomData::zoom,
		ScopeZoomData::new
	);
}
