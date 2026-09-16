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
import net.frozenblock.freezeframe.item.crafting.FilmCapacityUpgradeRecipe;
import net.frozenblock.freezeframe.item.crafting.FilmFilterRecipe;
import net.frozenblock.lib.platform.api.registry.DeferredHolder;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class FFRecipeSerializers {
	private static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(
		Registries.RECIPE_SERIALIZER,
		FFConstants.MOD_ID
	);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FilmCapacityUpgradeRecipe>> FILM_CAPACITY_UPGRADE = REGISTER.register("film_capacity_upgrade",
		() -> FilmCapacityUpgradeRecipe.SERIALIZER
	);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FilmFilterRecipe>> FILM_FILTER = REGISTER.register("film_filter",
		() -> FilmFilterRecipe.SERIALIZER
	);

	static {
		REGISTER.register();
	}

	public static void init() {}

	private FFRecipeSerializers() {}
}
