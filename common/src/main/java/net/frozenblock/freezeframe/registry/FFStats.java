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
import net.frozenblock.lib.platform.api.registry.DeferredHolder;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public final class FFStats {
	private static final DeferredRegister<Identifier> REGISTER = DeferredRegister.create(
		Registries.CUSTOM_STAT,
		FFConstants.MOD_ID
	);

	public static final DeferredHolder<Identifier, Identifier> INTERACT_WITH_DEVELOPING_TABLE = REGISTER.register("interact_with_developing_table",
		() -> FFConstants.id("interact_with_developing_table"),
		id -> Stats.CUSTOM.get(id, StatFormatter.DEFAULT)
	);

	static {
		REGISTER.register();
	}

	public static void init() {}

	private FFStats() {}
}
