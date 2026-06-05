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

package net.frozenblock.freezeframe.item.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.registry.FFRegistries;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.ChatFormatting;

public record SpecialFilmFilter(Identifier shader, Optional uniforms) {
	public static final Codec<SpecialFilmFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("shader").forGetter(SpecialFilmFilter::shader)
	).apply(instance, SpecialFilmFilter::new));
	public static final Codec<Holder<SpecialFilmFilter>> REGISTRY_CODEC = RegistryFixedCodec.create(FFRegistries.SPECIAL_FILM_FILTER);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SpecialFilmFilter>> STREAM_CODEC = ByteBufCodecs.holderRegistry(FFRegistries.SPECIAL_FILM_FILTER);

	public static Component tooltipComponent(Holder<SpecialFilmFilter> specialFilmFilter) {
		final String path = specialFilmFilter.unwrapKey().map(key -> key.identifier().getPath()).orElse("unknown");
		return Component.translatable("item.freezeframe.film.filter.effect." + path).withStyle(ChatFormatting.GRAY);
	}

	public SpecialFilmFilter(Identifier shader) {
		this(shader, Optional.empty());
	}

	@Environment(EnvType.CLIENT)
	public static class Client {
		public static final Codec<SpecialFilmFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("shader").forGetter(SpecialFilmFilter::shader),
			PostChainConfig.Pass.UNIFORM_BLOCKS_CODEC.optionalFieldOf("shader_uniforms")
				.forGetter(specialFilmFilter -> ((Optional<Map<String, List<UniformValue>>>) specialFilmFilter.uniforms()))
		).apply(instance, SpecialFilmFilter::new));
	}
}
