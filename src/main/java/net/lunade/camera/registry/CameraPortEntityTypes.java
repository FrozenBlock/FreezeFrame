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

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.entity.DiscCamera;
import net.lunade.camera.entity.TripodCamera;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CameraPortEntityTypes {
	public static final EntityType<TripodCamera> CAMERA = register(
		"camera",
		EntityType.Builder.of(TripodCamera::new, MobCategory.MISC)
			.sized(0.6F, 1.75F)
			.eyeHeight(1.619999999999999F)
			.clientTrackingRange(10)
	);

	public static final EntityType<DiscCamera> DISC_CAMERA = register(
		"disc_camera",
		EntityType.Builder.of(DiscCamera::new, MobCategory.MISC)
			.sized(0.55F, 0.9F)
			.eyeHeight(0.81F)
			.clientTrackingRange(10)
	);

	public static void init() {
	}

	static {
		FabricDefaultAttributeRegistry.register(CAMERA, TripodCamera.createTripodCameraAttributes());
		FabricDefaultAttributeRegistry.register(DISC_CAMERA, DiscCamera.createTripodCameraAttributes());
	}

	private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> builder) {
		final ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, CameraPortConstants.id(id));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}
}
