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

import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.frozenblock.lib.item.api.component.BundleWeightOverride;
import net.frozenblock.lib.item.api.component.FrozenLibDataComponents;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.CameraContents;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.item.CameraItem;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.item.PhotographItem;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class FFItems {
	public static final CameraItem CAMERA = register(
		"camera",
		CameraItem::new,
		new Item.Properties()
			.stacksTo(1)
			.spawnEgg(FFEntityTypes.CAMERA)
			.component(FFDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY)
			.component(FFDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomHelper.CAMERA_DEFAULTS)
	);
	public static final CameraItem DISC_CAMERA = register(
		"disc_camera",
		CameraItem::new,
		new Item.Properties()
			.stacksTo(1)
			.spawnEgg(FFEntityTypes.DISC_CAMERA)
			.component(FFDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY)
			.component(FFDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomHelper.CAMERA_DEFAULTS)
	);
	public static final FilmItem FILM = register(
		"film",
		FilmItem::new,
		new Item.Properties()
			.stacksTo(16)
			.component(FFDataComponents.FILM_CONTENTS, FilmContents.EMPTY)
			.component(FFDataComponents.FILM_MAX_PHOTOGRAPHS, FilmContents.BASE_MAX_PHOTOGRAPHS)
			.component(FrozenLibDataComponents.BUNDLE_WEIGHT_OVERRIDE, new BundleWeightOverride(1, 16))
	);
	public static final PhotographItem PHOTOGRAPH = register(
		"photograph",
		PhotographItem::new,
		new Item.Properties()
			.stacksTo(16)
	);

	// public static final PortfolioItem PORTFOLIO = new PortfolioItem(new Item.Properties().stacksTo(1).component(WRITABLE_PORTFOLIO_CONTENT, WritablePortfolioContent.EMPTY));

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(
			entries -> {
				entries.insertAfter(Items.LODESTONE, CAMERA);
				entries.insertAfter(CAMERA, DISC_CAMERA);
				entries.insertAfter(DISC_CAMERA, FILM);
				entries.insertAfter(FILM, PHOTOGRAPH);
			}
		);
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
		return (T) Items.registerItem(ResourceKey.create(Registries.ITEM, FFConstants.id(name)), function, properties);
	}
}
