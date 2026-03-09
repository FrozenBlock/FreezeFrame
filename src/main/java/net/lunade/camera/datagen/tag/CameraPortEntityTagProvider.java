package net.lunade.camera.datagen.tag;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.lunade.camera.registry.CameraPortEntityTypes;
import net.lunade.camera.tag.CameraPortEntityTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagEntry;

public final class CameraPortEntityTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {

	public CameraPortEntityTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		this.valueLookupBuilder(CameraPortEntityTags.CAMERAS)
			.add(CameraPortEntityTypes.CAMERA)
			.add(CameraPortEntityTypes.DISC_CAMERA);

		this.getOrCreateRawBuilder(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
			.add(TagEntry.optionalTag(CameraPortEntityTags.CAMERAS.location()));
	}
}
