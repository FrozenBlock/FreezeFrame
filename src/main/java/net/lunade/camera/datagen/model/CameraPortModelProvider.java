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
