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

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.entity.DiscCamera;
import net.frozenblock.freezeframe.entity.TripodCamera;
import net.frozenblock.freezeframe.references.FFEntityTypeIds;
import net.frozenblock.lib.entity.api.attribute.DefaultAttributeRegistry;
import net.frozenblock.lib.platform.api.registry.DeferredEntityType;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.world.entity.MobCategory;

public final class FFEntityTypes {
	private static final DeferredRegister.Entities REGISTER = DeferredRegister.createEntities(FFConstants.MOD_ID);

	public static final DeferredEntityType<TripodCamera> CAMERA = REGISTER.register(FFEntityTypeIds.CAMERA,
		TripodCamera::new,
		MobCategory.MISC,
		builder -> builder
			.sized(0.6F, TripodCamera.MAX_HEIGHT)
			.eyeHeight(TripodCamera.MAX_EYE_EIGHT)
			.clientTrackingRange(10),
		type -> {
			DefaultAttributeRegistry.register(type, TripodCamera.createTripodCameraAttributes());
		}
	);
	public static final DeferredEntityType<DiscCamera> DISC_CAMERA = REGISTER.register(FFEntityTypeIds.DISC_CAMERA,
		DiscCamera::new,
		MobCategory.MISC,
		builder -> builder
			.sized(0.55F, 0.9F)
			.eyeHeight(0.81F)
			.clientTrackingRange(10),
		type -> {
			DefaultAttributeRegistry.register(type, DiscCamera.createTripodCameraAttributes());
		}
	);

	public static void init() {}

	static {
		REGISTER.register();
	}

	private FFEntityTypes() {}
}
