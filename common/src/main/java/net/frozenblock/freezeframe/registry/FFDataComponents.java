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

package net.frozenblock.freezeframe.registry;

import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.BookPagePhotographs;
import net.frozenblock.freezeframe.component.CameraContents;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.component.ScopeZoomConfig;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.frozenblock.lib.platform.api.registry.DeferredDataComponentType;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Items;

public final class FFDataComponents {
	private static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(FFConstants.MOD_ID);

	public static final DeferredDataComponentType<Photograph> PHOTOGRAPH = REGISTER.registerComponent("photograph",
		builder -> builder.persistent(Photograph.CODEC).networkSynchronized(Photograph.STREAM_CODEC)
	);
	public static final DeferredDataComponentType<CameraContents> CAMERA_CONTENTS = REGISTER.registerComponent("camera_contents",
		builder -> builder.persistent(CameraContents.CODEC).networkSynchronized(CameraContents.STREAM_CODEC)
	);
	public static final DeferredDataComponentType<FilmContents> FILM_CONTENTS = REGISTER.registerComponent("film_contents",
		builder -> builder.persistent(FilmContents.CODEC).networkSynchronized(FilmContents.STREAM_CODEC)
	);
	public static final DeferredDataComponentType<FilmFilter> FILM_FILTER = REGISTER.registerComponent("film_filter",
		builder -> builder.persistent(FilmFilter.CODEC).networkSynchronized(FilmFilter.STREAM_CODEC)
	);
	public static final DeferredDataComponentType<Integer> FILM_MAX_PHOTOGRAPHS = REGISTER.registerComponent("film_max_photographs",
		builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DeferredDataComponentType<BookPagePhotographs> BOOK_PAGE_PHOTOGRAPHS = REGISTER.registerComponent("book_page_photographs",
		builder -> builder.persistent(BookPagePhotographs.CODEC).networkSynchronized(BookPagePhotographs.STREAM_CODEC)
	);
	public static final DeferredDataComponentType<Float> SCOPE_ZOOM = REGISTER.registerComponent("scope_zoom",
		builder -> builder.persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
	);
	public static final DeferredDataComponentType<ScopeZoomConfig> SCOPE_ZOOM_CONFIG = REGISTER.registerComponent("scope_zoom_config",
		builder -> builder.persistent(ScopeZoomConfig.CODEC).networkSynchronized(ScopeZoomConfig.STREAM_CODEC)
	);

	static {
		REGISTER.register();
	}

	public static void init() {
		DefaultItemComponentEvents.MODIFY.register(modifyContext -> {
			modifyContext.modify(Items.SPYGLASS, componentBuilder -> {
				componentBuilder.set(SCOPE_ZOOM_CONFIG, ScopeZoomHelper.SPYGLASS_DEFAULTS);
			});
		});
	}

	private FFDataComponents() {}
}
