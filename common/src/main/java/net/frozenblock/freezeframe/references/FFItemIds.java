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

package net.frozenblock.freezeframe.references;

import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class FFItemIds {
	public static final ResourceKey<Item> CAMERA = create("camera");
	public static final ResourceKey<Item> DISC_CAMERA = create("disc_camera");
	public static final ResourceKey<Item> FILM = create("film");
	public static final ResourceKey<Item> PHOTOGRAPH = create("photograph");

	private static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, FFConstants.id(name));
	}
}
