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

package net.frozenblock.freezeframe.references;

import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class FFBlockItemIds {
	public static final BlockItemId DEVELOPING_TABLE = create("developing_table");

	private static BlockItemId create(String name) {
		final Identifier id = FFConstants.id(name);
		return BlockItemId.create(id, id);
	}

	private static BlockItemId create(String blockName, String itemName) {
		return BlockItemId.create(FFConstants.id(blockName), FFConstants.id(itemName));
	}
}
