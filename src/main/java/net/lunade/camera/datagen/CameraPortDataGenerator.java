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

package net.lunade.camera.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.frozenblock.lib.feature_flag.api.FeatureFlagApi;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.datagen.loot.CameraPortBlockLootProvider;
import net.lunade.camera.datagen.model.CameraPortModelProvider;
import net.lunade.camera.datagen.recipe.CameraPortRecipeProvider;
import net.lunade.camera.datagen.tag.CameraPortBlockTagProvider;
import net.lunade.camera.datagen.tag.CameraPortEntityTagProvider;
import net.lunade.camera.datagen.tag.CameraPortItemTagProvider;
import net.minecraft.core.RegistrySetBuilder;

public final class CameraPortDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FeatureFlagApi.rebuild();
		final FabricDataGenerator.Pack pack = dataGenerator.createPack();

		// ASSETS

		pack.addProvider(CameraPortModelProvider::new);

		// DATA

		pack.addProvider(CameraPortBlockLootProvider::new);
		pack.addProvider(CameraPortBlockTagProvider::new);
		pack.addProvider(CameraPortItemTagProvider::new);
		pack.addProvider(CameraPortEntityTagProvider::new);
		pack.addProvider(CameraPortRecipeProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
	}

	@Override
	public String getEffectiveModId() {
		return CameraPortConstants.MOD_ID;
	}
}
