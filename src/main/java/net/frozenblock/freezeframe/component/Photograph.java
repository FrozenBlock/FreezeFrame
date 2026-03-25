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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record Photograph(Identifier identifier, String photographer, String name, int generation) {
	public static final int ORIGINAL = 0;
	public static final int COPY = 1;
	public static final int COPY_OF_COPY = 2;
	public static final int MAX_COPY_GENERATION = COPY_OF_COPY;

	public static final Codec<Photograph> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
			Identifier.CODEC.fieldOf("identifier").forGetter(component -> component.identifier),
			Codec.STRING.fieldOf("author").forGetter(Photograph::photographer),
			Codec.STRING.optionalFieldOf("name", "").forGetter(Photograph::name),
			Codec.INT.optionalFieldOf("generation", ORIGINAL).forGetter(Photograph::generation)
		)
		.apply(instance, Photograph::new)
	);
	public static final StreamCodec<ByteBuf, Photograph> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC, Photograph::identifier,
		ByteBufCodecs.STRING_UTF8, Photograph::photographer,
		ByteBufCodecs.STRING_UTF8, Photograph::name,
		ByteBufCodecs.VAR_INT, Photograph::generation,
		Photograph::new
	);

	public Photograph(Identifier identifier, String photographer) {
		this(identifier, photographer, "", ORIGINAL);
	}

	public Photograph(Identifier identifier, String photographer, int generation) {
		this(identifier, photographer, "", generation);
	}

	public boolean isCopy() {
		return this.generation > ORIGINAL;
	}

	public boolean canCopy() {
		return this.generation < MAX_COPY_GENERATION;
	}

	public Photograph asCopy() {
		return new Photograph(this.identifier, this.photographer, this.name, Math.min(this.generation + 1, MAX_COPY_GENERATION));
	}

	public Photograph withName(String newName) {
		return new Photograph(this.identifier, this.photographer, newName, this.generation);
	}
}
