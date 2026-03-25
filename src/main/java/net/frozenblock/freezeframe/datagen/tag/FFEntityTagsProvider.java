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

package net.frozenblock.freezeframe.datagen.tag;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.frozenblock.freezeframe.registry.FFEntityTypes;
import net.frozenblock.freezeframe.tag.CameraPortEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagEntry;

public final class FFEntityTagsProvider extends FabricTagsProvider.EntityTypeTagsProvider {

	public FFEntityTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void addTags(HolderLookup.Provider arg) {
		this.valueLookupBuilder(CameraPortEntityTypeTags.CAMERAS)
			.add(FFEntityTypes.CAMERA)
			.add(FFEntityTypes.DISC_CAMERA);

		this.getOrCreateRawBuilder(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
			.add(TagEntry.optionalTag(CameraPortEntityTypeTags.CAMERAS.location()));
	}
}
