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
