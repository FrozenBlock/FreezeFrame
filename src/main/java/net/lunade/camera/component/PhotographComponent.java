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

public record PhotographComponent(Identifier identifier, String photographer, int generation) {
	public static final int ORIGINAL = 0;
	public static final int COPY = 1;
	public static final int COPY_OF_COPY = 2;
	public static final int MAX_COPY_GENERATION = COPY_OF_COPY;

	public static final Codec<PhotographComponent> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
			Identifier.CODEC.fieldOf("identifier").forGetter(component -> component.identifier),
			Codec.STRING.fieldOf("author").forGetter(PhotographComponent::photographer),
			Codec.INT.optionalFieldOf("generation", ORIGINAL).forGetter(PhotographComponent::generation)
		)
		.apply(instance, PhotographComponent::new)
	);
	public static final StreamCodec<ByteBuf, PhotographComponent> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC, PhotographComponent::identifier,
		ByteBufCodecs.STRING_UTF8, PhotographComponent::photographer,
		ByteBufCodecs.VAR_INT, PhotographComponent::generation,
		PhotographComponent::new
	);

	public PhotographComponent(Identifier identifier, String photographer) {
		this(identifier, photographer, ORIGINAL);
	}

	public boolean isCopy() {
		return this.generation > ORIGINAL;
	}

	public boolean canCopy() {
		return this.generation < MAX_COPY_GENERATION;
	}

	public PhotographComponent asCopy() {
		return new PhotographComponent(this.identifier, this.photographer, Math.min(this.generation + 1, MAX_COPY_GENERATION));
	}
}
