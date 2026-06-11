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

package net.frozenblock.freezeframe;

import net.fabricmc.api.ModInitializer;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.networking.FFNetworking;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.freezeframe.registry.FFBlocks;
import net.frozenblock.freezeframe.registry.FFContainerComponentManipulators;
import net.frozenblock.freezeframe.registry.FFDataComponentPredicates;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFEntityTypes;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFMenuTypes;
import net.frozenblock.freezeframe.registry.FFRecipeSerializers;
import net.frozenblock.freezeframe.registry.FFRegistries;
import net.frozenblock.freezeframe.registry.FFSlotDisplays;
import net.frozenblock.freezeframe.registry.FFSounds;
import net.frozenblock.freezeframe.registry.FFStats;

public class FreezeFrame implements ModInitializer {

	@Override
	public void onInitialize() {
		FFRegistries.init();
		FFEntityTypes.init();
		FFBlocks.init();
		FFItems.init();
		FFAttachmentTypes.init();
		FFContainerComponentManipulators.init();
		FFDataComponentPredicates.init();
		FFSounds.init();
		FFDataComponents.init();
		FFRecipeSerializers.init();
		FFSlotDisplays.bootstrap();
		FFMenuTypes.init();
		FFStats.init();

		FFNetworking.init();

		FFConfig.CONFIG.load(true);
	}
}
