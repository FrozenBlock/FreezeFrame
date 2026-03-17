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

package net.lunade.camera.datagen.tag;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.lunade.camera.registry.CameraPortEntityTypes;
import net.lunade.camera.tag.CameraPortEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagEntry;

public final class CameraPortEntityTagProvider extends FabricTagsProvider.EntityTypeTagsProvider {

	public CameraPortEntityTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		this.valueLookupBuilder(CameraPortEntityTypeTags.CAMERAS)
			.add(CameraPortEntityTypes.CAMERA)
			.add(CameraPortEntityTypes.DISC_CAMERA);

		this.getOrCreateRawBuilder(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
			.add(TagEntry.optionalTag(CameraPortEntityTypeTags.CAMERAS.location()));
	}
}
