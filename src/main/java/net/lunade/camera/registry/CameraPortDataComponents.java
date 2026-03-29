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

package net.lunade.camera.registry;

import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.lunade.camera.component.BookPagePhotographs;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.component.ScopeZoomConfig;
import net.lunade.camera.component.WritablePortfolioContent;
import net.lunade.camera.util.ScopeZoomHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Items;

public class CameraPortDataComponents {
	public static final DataComponentType<Photograph> PHOTOGRAPH = register(
		"photograph",
		builder -> builder.persistent(Photograph.CODEC).networkSynchronized(Photograph.STREAM_CODEC)
	);
	public static final DataComponentType<CameraContents> CAMERA_CONTENTS = register(
		"camera_contents",
		builder -> builder.persistent(CameraContents.CODEC).networkSynchronized(CameraContents.STREAM_CODEC)
	);
	public static final DataComponentType<FilmContents> FILM_CONTENTS = register(
		"film_contents",
		builder -> builder.persistent(FilmContents.CODEC).networkSynchronized(FilmContents.STREAM_CODEC)
	);
	public static final DataComponentType<Integer> FILM_MAX_PHOTOGRAPHS = register(
		"film_max_photographs",
		builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<WritablePortfolioContent> WRITABLE_PORTFOLIO_CONTENT = register(
		"writable_portfolio_content",
		builder -> builder.persistent(WritablePortfolioContent.CODEC).networkSynchronized(WritablePortfolioContent.STREAM_CODEC)
	);
	public static final DataComponentType<BookPagePhotographs> BOOK_PAGE_PHOTOGRAPHS = register(
		"book_page_photographs",
		builder -> builder.persistent(BookPagePhotographs.CODEC).networkSynchronized(BookPagePhotographs.STREAM_CODEC)
	);
	public static final DataComponentType<Float> SCOPE_ZOOM = register(
		"scope_zoom",
		builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
	);
	public static final DataComponentType<ScopeZoomConfig> SCOPE_ZOOM_CONFIG = register(
		"scope_zoom_config",
		builder -> builder.persistent(ScopeZoomConfig.CODEC).networkSynchronized(ScopeZoomConfig.STREAM_CODEC)
	);

	public static void init() {
		DefaultItemComponentEvents.MODIFY.register(modifyContext -> {
			modifyContext.modify(Items.SPYGLASS, componentBuilder -> {
				componentBuilder.set(SCOPE_ZOOM_CONFIG, ScopeZoomHelper.SPYGLASS_DEFAULTS);
			});
		});
	}

	private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, CameraPortConstants.id(id), unaryOperator.apply(DataComponentType.builder()).build());
	}
}
