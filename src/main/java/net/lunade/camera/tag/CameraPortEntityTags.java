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

package net.lunade.camera.tag;


import net.lunade.camera.CameraPortConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class CameraPortEntityTags {
	public static final TagKey<EntityType<?>> CAMERAS = bind("cameras");

	private CameraPortEntityTags() {
		throw new UnsupportedOperationException("CameraPortEntityTags contains only static declarations.");
	}

	private static TagKey<EntityType<?>> bind(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, CameraPortConstants.id(path));
	}
}
