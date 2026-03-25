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
import java.util.List;
import net.frozenblock.freezeframe.component.impl.PortfolioContent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record WritablePortfolioContent(List<ItemStack> pages) implements PortfolioContent<ItemStack, WritablePortfolioContent> {
	public static final WritablePortfolioContent EMPTY = new WritablePortfolioContent(List.of());
	public static final int MAX_PAGES = 64;
	private static final Codec<ItemStack> PAGE_CODEC = ItemStack.CODEC;
	public static final Codec<List<ItemStack>> PAGES_CODEC = PAGE_CODEC.sizeLimitedListOf(MAX_PAGES);
	public static final Codec<WritablePortfolioContent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(WritablePortfolioContent::pages))
			.apply(instance, WritablePortfolioContent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, WritablePortfolioContent> STREAM_CODEC = ItemStack.STREAM_CODEC
		.apply(ByteBufCodecs.list(MAX_PAGES))
		.map(WritablePortfolioContent::new, WritablePortfolioContent::pages);

	@Override
	public WritablePortfolioContent withReplacedPages(List<ItemStack> newPages) {
		return new WritablePortfolioContent(newPages);
	}
}
