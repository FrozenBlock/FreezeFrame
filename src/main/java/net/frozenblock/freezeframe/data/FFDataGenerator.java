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

package net.frozenblock.freezeframe.data;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.data.loot.FFBlockLootProvider;
import net.frozenblock.freezeframe.data.model.FFModelProvider;
import net.frozenblock.freezeframe.data.recipe.FFRecipeProvider;
import net.frozenblock.freezeframe.data.tag.FFBlockTagsProvider;
import net.frozenblock.freezeframe.data.tag.FFEntityTagsProvider;
import net.frozenblock.freezeframe.data.tag.FFItemTagsProvider;
import net.frozenblock.freezeframe.registry.FFRegistries;
import net.frozenblock.freezeframe.registry.FFSpecialFilmFilters;
import net.frozenblock.lib.feature_flag.api.FeatureFlagApi;
import net.minecraft.core.RegistrySetBuilder;

public final class FFDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FeatureFlagApi.rebuild();
		final FabricDataGenerator.Pack pack = dataGenerator.createPack();

		// ASSETS
		pack.addProvider(FFModelProvider::new);

		// DATA
		pack.addProvider(FFBlockLootProvider::new);
		pack.addProvider(FFBlockTagsProvider::new);
		pack.addProvider(FFItemTagsProvider::new);
		pack.addProvider(FFEntityTagsProvider::new);
		pack.addProvider(FFRegistryProvider::new);
		pack.addProvider(FFRecipeProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			registryBuilder.add(FFRegistries.SPECIAL_FILM_FILTER, context -> FFSpecialFilmFilters.bootstrap(context));
		}
	}

	@Override
	public String getEffectiveModId() {
		return FFConstants.MOD_ID;
	}
}
