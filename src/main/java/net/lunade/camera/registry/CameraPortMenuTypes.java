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

import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.menu.DevelopingTableMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class CameraPortMenuTypes {
	public static final MenuType<DevelopingTableMenu> DEVELOPING_TABLE = new MenuType<>(DevelopingTableMenu::new, FeatureFlags.DEFAULT_FLAGS);

	public static void register() {
		Registry.register(BuiltInRegistries.MENU, CameraPortConstants.id("developing_table"), DEVELOPING_TABLE);
	}
}
