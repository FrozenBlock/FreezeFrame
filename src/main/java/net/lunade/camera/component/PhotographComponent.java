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
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record PhotographComponent(Identifier identifier, String photographer, boolean isCopy) {
	public static final Codec<PhotographComponent> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
			Identifier.CODEC.fieldOf("identifier").forGetter(component -> component.identifier),
			Codec.STRING.fieldOf("author").forGetter(PhotographComponent::photographer),
			Codec.BOOL.optionalFieldOf("is_copy", false).forGetter(PhotographComponent::isCopy)
		)
		.apply(instance, PhotographComponent::new)
	);
	public static final StreamCodec<ByteBuf, PhotographComponent> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC, PhotographComponent::identifier,
		ByteBufCodecs.STRING_UTF8, PhotographComponent::photographer,
		ByteBufCodecs.BOOL, PhotographComponent::isCopy,
		PhotographComponent::new
	);

	public PhotographComponent(Identifier identifier, String photographer) {
		this(identifier, photographer, false);
	}

	public PhotographComponent asCopy() {
		return new PhotographComponent(this.identifier, this.photographer, true);
	}
}
