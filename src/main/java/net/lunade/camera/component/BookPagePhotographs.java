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
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record BookPagePhotographs(List<PagePhotograph> photographs) {
	public static final BookPagePhotographs EMPTY = new BookPagePhotographs(List.of());
	public static final int MAX_PAGES = 100;
	public static final Codec<PagePhotograph> PAGE_PHOTOGRAPH_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Codec.intRange(0, MAX_PAGES - 1).fieldOf("page").forGetter(PagePhotograph::pageIndex),
			ItemStack.CODEC.fieldOf("photograph").forGetter(PagePhotograph::photograph)
		).apply(instance, PagePhotograph::new)
	);
	public static final Codec<List<PagePhotograph>> PAGES_CODEC = PAGE_PHOTOGRAPH_CODEC.sizeLimitedListOf(MAX_PAGES);
	public static final Codec<BookPagePhotographs> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(BookPagePhotographs::photographs))
			.apply(instance, BookPagePhotographs::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, PagePhotograph> PAGE_PHOTOGRAPH_STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		PagePhotograph::pageIndex,
		ItemStack.STREAM_CODEC,
		PagePhotograph::photograph,
		PagePhotograph::new
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, BookPagePhotographs> STREAM_CODEC = PAGE_PHOTOGRAPH_STREAM_CODEC
		.apply(ByteBufCodecs.list(MAX_PAGES))
		.map(BookPagePhotographs::new, BookPagePhotographs::photographs);

	public static record PagePhotograph(int pageIndex, ItemStack photograph) {
	}
}
