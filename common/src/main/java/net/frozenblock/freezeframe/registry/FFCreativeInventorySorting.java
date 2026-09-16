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

import net.frozenblock.lib.item.api.creative.CreativeModeTabSorter;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

public final class FFCreativeInventorySorting {

	/**
	 * Called separately on Fabric and NeoForge
	 */
	public static void setup() {
		CreativeModeTabSorter.insertAfter(Items.LOOM, FFItems.DEVELOPING_TABLE.get(), CreativeModeTabs.FUNCTIONAL_BLOCKS);

		CreativeModeTabSorter.insertAfter(Items.SPYGLASS, FFItems.CAMERA.get(), CreativeModeTabs.TOOLS_AND_UTILITIES);
		CreativeModeTabSorter.insertAfter(FFItems.CAMERA.get(), FFItems.FILM.get(), CreativeModeTabs.TOOLS_AND_UTILITIES);
	}

	private FFCreativeInventorySorting() {}
}
