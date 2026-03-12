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
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.predicates.CameraPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class CameraPortDataComponentPredicates {
	public static final DataComponentPredicate.Type<CameraPredicate> CAMERA_CONTENTS = register("camera_contents", CameraPredicate.CODEC);

	public static void init() {
	}

	private static <T extends DataComponentPredicate> DataComponentPredicate.Type<T> register(String id, Codec<T> codec) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, CameraPortConstants.id(id), new DataComponentPredicate.ConcreteType<>(codec));
	}
}
