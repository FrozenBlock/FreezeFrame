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

package net.frozenblock.freezeframe.datafix.freezeframe;

import com.mojang.datafixers.schemas.Schema;
import net.fabricmc.frozenblock.datafixer.api.FabricDataFixerBuilder;
import net.fabricmc.frozenblock.datafixer.api.FabricDataFixes;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.datafix.freezeframe.fixes.PhotographFieldRenameFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public final class FFDataFixer {
	public static final int DATA_VERSION = 1;

	public static void applyDataFixes() {
		FFConstants.log("Applying DataFixes for Freeze Frame with Data Version " + DATA_VERSION, true);
		final FabricDataFixerBuilder builder = new FabricDataFixerBuilder(DATA_VERSION);
		builder.addSchema(0, FabricDataFixes.BASE_SCHEMA);

		// Unfortunately this does literally nothing because....
		// I completely forgot we needed to register a datafixer first so it won't assume it has nothing to fix later.
		// But I'm keeping this for the sake of good practice anyway.
		final Schema schemaV1 = builder.addSchema(1, NamespacedSchema::new);
		builder.addFixer(
			new PhotographFieldRenameFix(
				"Rename Photograph author field to photographer",
				"author",
				"photographer",
				schemaV1
			)
		);
		builder.addFixer(
			new PhotographFieldRenameFix(
				"Rename Photograph identifier field to id",
				"identifier",
				"id",
				schemaV1
			)
		);

		FabricDataFixes.buildAndRegisterFixer(FFConstants.MOD_ID, builder);
		FFConstants.log("DataFixes for Freeze Frame have been applied", true);
	}

	private FFDataFixer() {}
}
