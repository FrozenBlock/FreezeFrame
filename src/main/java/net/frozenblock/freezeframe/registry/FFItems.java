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

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.frozenblock.freezeframe.component.CameraContents;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.item.CameraItem;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.item.PhotographItem;
import net.frozenblock.freezeframe.references.FFBlockItemIds;
import net.frozenblock.freezeframe.references.FFItemIds;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.frozenblock.lib.item.api.component.BundleWeightOverride;
import net.frozenblock.lib.item.api.component.FrozenLibDataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class FFItems {
	public static final Item DEVELOPING_TABLE = Items.registerBlock(FFBlockItemIds.DEVELOPING_TABLE, FFBlocks.DEVELOPING_TABLE);

	public static final Item CAMERA = Items.registerItem(
		FFItemIds.CAMERA,
		CameraItem::new,
		new Item.Properties()
			.stacksTo(1)
			.spawnEgg(FFEntityTypes.CAMERA)
			.component(FFDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY)
			.component(FFDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomHelper.CAMERA_DEFAULTS)
	);
	public static final Item DISC_CAMERA = Items.registerItem(
		FFItemIds.DISC_CAMERA,
		CameraItem::new,
		new Item.Properties()
			.stacksTo(1)
			.spawnEgg(FFEntityTypes.DISC_CAMERA)
			.component(FFDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY)
			.component(FFDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomHelper.CAMERA_DEFAULTS)
	);
	public static final Item FILM = Items.registerItem(
		FFItemIds.FILM,
		FilmItem::new,
		new Item.Properties()
			.stacksTo(16)
			.component(FFDataComponents.FILM_CONTENTS, FilmContents.EMPTY)
			.component(FFDataComponents.FILM_FILTER, FilmFilter.EMPTY)
			.component(FFDataComponents.FILM_MAX_PHOTOGRAPHS, FilmContents.BASE_MAX_PHOTOGRAPHS)
			.component(FrozenLibDataComponents.BUNDLE_WEIGHT_OVERRIDE, new BundleWeightOverride(1, 16))
	);
	public static final Item PHOTOGRAPH = Items.registerItem(
		FFItemIds.PHOTOGRAPH,
		PhotographItem::new,
		new Item.Properties()
			.stacksTo(16)
	);

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(
			output -> output.insertAfter(Items.LOOM, DEVELOPING_TABLE)
		);

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(
			entries -> {
				entries.insertAfter(Items.SPYGLASS, CAMERA);
				entries.insertAfter(CAMERA, FILM);
			}
		);
	}
}
