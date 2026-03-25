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

package net.lunade.camera;

import net.fabricmc.api.ModInitializer;
import net.lunade.camera.config.CameraPortConfig;
import net.lunade.camera.networking.CameraPortNetworking;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortContainerComponentManipulators;
import net.lunade.camera.registry.CameraPortDataComponentPredicates;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortEntityTypes;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortMenuTypes;
import net.lunade.camera.registry.CameraPortRecipeSerializers;
import net.lunade.camera.registry.CameraPortSounds;

public class CameraPortMain implements ModInitializer {

	@Override
	public void onInitialize() {
		CameraPortEntityTypes.init();
		CameraPortBlocks.register();
		CameraPortItems.init();
		CameraPortContainerComponentManipulators.init();
		CameraPortDataComponentPredicates.init();
		CameraPortSounds.init();
		CameraPortDataComponents.init();
		CameraPortRecipeSerializers.init();
		CameraPortMenuTypes.register();

		CameraPortNetworking.init();

		CameraPortConfig.CONFIG.load(true);
	}
}
