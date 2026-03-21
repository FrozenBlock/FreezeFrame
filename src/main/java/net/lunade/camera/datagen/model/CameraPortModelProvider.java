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
import net.lunade.camera.client.renderer.item.properties.conditional.CanTakePhoto;
import net.lunade.camera.client.renderer.item.properties.conditional.InMainHand;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public final class CameraPortModelProvider extends FabricModelProvider {

	public CameraPortModelProvider(FabricPackOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators generator) {
		generator.createHorizontallyRotatedBlock(
			CameraPortBlocks.DEVELOPING_TABLE,
			TexturedModel.createDefault(
				block -> new TextureMapping()
					.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_north"))
					.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_bottom"))
					.put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
					.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_north"))
					.put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_east"))
					.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_south"))
					.put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_west")),
				ModelTemplates.CUBE
			)
		);
	}

	@Override
	public void generateItemModels(ItemModelGenerators generator) {
		generateCamera(generator, CameraPortItems.CAMERA);
		generateCamera(generator, CameraPortItems.DISC_CAMERA);

		generator.generateFlatItem(CameraPortItems.FILM.asItem(), ModelTemplates.FLAT_ITEM);
		generator.generateFlatItem(CameraPortItems.PHOTOGRAPH.asItem(), ModelTemplates.FLAT_ITEM);
	}

	private static void generateCamera(ItemModelGenerators generator, Item camera) {
		final ItemModel.Unbaked inactiveModel = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(camera, "_inactive"));
		final ItemModel.Unbaked activeModel = ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(camera, "_active"));
		generator.generateBooleanDispatch(
			camera,
			new CanTakePhoto(),
			activeModel,
			ItemModelUtils.conditional(
				new InMainHand(),
				ItemModelUtils.conditional(
					new IsUsingItem(),
					activeModel,
					inactiveModel
				),
				inactiveModel
			)
		);
	}
}
