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

import java.util.function.UnaryOperator;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.WritablePortfolioContent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class CameraPortDataComponents {
	public static final DataComponentType<PhotographComponent> PHOTOGRAPH = register(
		"photograph",
		builder -> builder.persistent(PhotographComponent.CODEC).networkSynchronized(PhotographComponent.STREAM_CODEC)
	);
	public static final DataComponentType<CameraContents> CAMERA_CONTENTS = register(
		"camera_contents",
		builder -> builder.persistent(CameraContents.CODEC).networkSynchronized(CameraContents.STREAM_CODEC)
	);
	public static final DataComponentType<FilmContents> FILM_CONTENTS = register(
		"film_contents",
		builder -> builder.persistent(FilmContents.CODEC).networkSynchronized(FilmContents.STREAM_CODEC)
	);
	public static final DataComponentType<WritablePortfolioContent> WRITABLE_PORTFOLIO_CONTENT = register(
		"writable_portfolio_content",
		builder -> builder.persistent(WritablePortfolioContent.CODEC).networkSynchronized(WritablePortfolioContent.STREAM_CODEC)
	);

	public static void init() {
	}

	private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, CameraPortConstants.id(id), unaryOperator.apply(DataComponentType.builder()).build());
	}
}
