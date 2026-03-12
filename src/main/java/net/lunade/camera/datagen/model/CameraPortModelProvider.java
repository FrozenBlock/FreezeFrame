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

package net.lunade.camera.datagen.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;

@Environment(EnvType.CLIENT)
public final class CameraPortModelProvider extends FabricModelProvider {

	public CameraPortModelProvider(FabricPackOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators generator) {
		generator.createHorizontallyRotatedBlock(CameraPortBlocks.PRINTER, TexturedModel.ORIENTABLE);
	}

	@Override
	public void generateItemModels(ItemModelGenerators generator) {
		generator.declareCustomModelItem(CameraPortItems.CAMERA.asItem());
		generator.declareCustomModelItem(CameraPortItems.DISC_CAMERA.asItem());

		generator.generateFlatItem(CameraPortItems.FILM.asItem(), ModelTemplates.FLAT_ITEM);
		generator.generateFlatItem(CameraPortItems.PHOTOGRAPH.asItem(), ModelTemplates.FLAT_ITEM);
	}
}
