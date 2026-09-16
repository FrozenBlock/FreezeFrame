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

package net.frozenblock.freezeframe.registry;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.block.DevelopingTableBlock;
import net.frozenblock.freezeframe.references.FFBlockItemIds;
import net.frozenblock.lib.platform.api.registry.DeferredBlock;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public final class FFBlocks {
	private static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(FFConstants.MOD_ID);

	public static final DeferredBlock<DevelopingTableBlock> DEVELOPING_TABLE = REGISTER.registerBlock(FFBlockItemIds.DEVELOPING_TABLE,
		DevelopingTableBlock::new,
		() -> BlockBehaviour.Properties.of()
			.mapColor(MapColor.WOOD)
			.instrument(NoteBlockInstrument.BASS)
			.strength(2.5F)
			.sound(SoundType.WOOD)
			.ignitedByLava()
	);

	static {
		REGISTER.register();
	}

	public static void init() {}

	private FFBlocks() {}
}
