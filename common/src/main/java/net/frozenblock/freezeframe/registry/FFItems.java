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
import net.frozenblock.freezeframe.component.CameraContents;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.item.CameraItem;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.item.PhotographItem;
import net.frozenblock.freezeframe.references.FFBlockItemIds;
import net.frozenblock.freezeframe.references.FFItemIds;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.frozenblock.lib.item.api.component.BundleWeightOverride;
import net.frozenblock.lib.item.api.component.FrozenLibDataComponents;
import net.frozenblock.lib.platform.api.registry.DeferredItem;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class FFItems {
	private static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(FFConstants.MOD_ID);

	public static final DeferredItem<BlockItem> DEVELOPING_TABLE = REGISTER.registerSimpleBlockItem(FFBlockItemIds.DEVELOPING_TABLE, FFBlocks.DEVELOPING_TABLE);

	public static final DeferredItem<CameraItem> CAMERA = REGISTER.registerItem(FFItemIds.CAMERA,
		CameraItem::new,
		() -> new Item.Properties()
			.stacksTo(1)
			.spawnEgg(FFEntityTypes.CAMERA.get())
			.component(FFDataComponents.CAMERA_CONTENTS.get(), CameraContents.EMPTY)
			.component(FFDataComponents.SCOPE_ZOOM_CONFIG.get(), ScopeZoomHelper.CAMERA_DEFAULTS)
	);
	public static final DeferredItem<CameraItem> DISC_CAMERA = REGISTER.registerItem(FFItemIds.DISC_CAMERA,
		CameraItem::new,
		() -> new Item.Properties()
			.stacksTo(1)
			.spawnEgg(FFEntityTypes.DISC_CAMERA.get())
			.component(FFDataComponents.CAMERA_CONTENTS.get(), CameraContents.EMPTY)
			.component(FFDataComponents.SCOPE_ZOOM_CONFIG.get(), ScopeZoomHelper.CAMERA_DEFAULTS)
	);
	public static final DeferredItem<FilmItem> FILM = REGISTER.registerItem(FFItemIds.FILM,
		FilmItem::new,
		() -> new Item.Properties()
			.stacksTo(16)
			.component(FFDataComponents.FILM_CONTENTS.get(), FilmContents.EMPTY)
			.component(FFDataComponents.FILM_FILTER.get(), FilmFilter.EMPTY)
			.component(FFDataComponents.FILM_MAX_PHOTOGRAPHS.get(), FilmContents.BASE_MAX_PHOTOGRAPHS)
			.component(FrozenLibDataComponents.BUNDLE_WEIGHT_OVERRIDE.get(), new BundleWeightOverride(1, 16))
	);
	public static final DeferredItem<PhotographItem> PHOTOGRAPH = REGISTER.registerItem(FFItemIds.PHOTOGRAPH,
		PhotographItem::new,
		() -> new Item.Properties().stacksTo(16)
	);

	static {
		REGISTER.register();
	}

	public static void init() {}

	private FFItems() {}
}
