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

package net.frozenblock.freezeframe.client.renderer.entity.state.impl;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.lib.renderer.RenderStateDataKey;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.resources.Identifier;

@ClientOnly
public final class FFRenderStateDataKeys {
	public static final RenderStateDataKey<Identifier> PHOTOGRAPH_ID = RenderStateDataKey.create(FFConstants.id("photograph_id"));

	private FFRenderStateDataKeys() {}
}
