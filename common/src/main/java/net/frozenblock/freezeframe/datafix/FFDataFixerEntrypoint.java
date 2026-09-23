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

package net.frozenblock.freezeframe.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import net.fabricmc.frozenblock.datafixer.api.DataFixerEntrypoint;
import net.fabricmc.frozenblock.datafixer.api.SchemaRegistry;
import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.util.datafix.fixes.References;

public final class FFDataFixerEntrypoint implements DataFixerEntrypoint {

	@Override
	public void onRegisterBlockEntities(SchemaRegistry registry, Schema schema) {}

	@Override
	public void onRegisterEntities(SchemaRegistry registry, Schema schema) {
		registry.register(FFConstants.id("camera"), () -> DSL.optionalFields("CameraContents", References.DATA_COMPONENTS.in(schema)));
		registry.register(FFConstants.id("disc_camera"), () -> DSL.optionalFields("CameraContents", References.DATA_COMPONENTS.in(schema)));
	}
}
