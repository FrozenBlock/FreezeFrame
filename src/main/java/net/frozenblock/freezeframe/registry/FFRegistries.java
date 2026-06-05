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

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class FFRegistries {
	public static final ResourceKey<Registry<SpecialFilmFilter>> SPECIAL_FILM_FILTER = ResourceKey.createRegistryKey(FFConstants.id("special_film_filter"));

	public static void init() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			DynamicRegistries.registerSynced(SPECIAL_FILM_FILTER, SpecialFilmFilter.Client.CODEC);
		} else {
			DynamicRegistries.registerSynced(SPECIAL_FILM_FILTER, SpecialFilmFilter.CODEC);
		}
	}
}
