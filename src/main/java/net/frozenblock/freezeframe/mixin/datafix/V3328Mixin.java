/*
 * Copyright 2025-2026 FrozenBlock
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

package net.frozenblock.freezeframe.mixin.datafix;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V3328;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(V3328.class)
public class V3328Mixin {

	@Inject(method = "registerEntities", at = @At("RETURN"))
	public void freezeFrame$registerEntities(
		Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> info,
		@Local(name = "map") Map<String, Supplier<TypeTemplate>> map
	) {
		schema.register(
			map,
			FFConstants.string("camera"),
			(string) -> DSL.optionalFields("CameraContents", References.DATA_COMPONENTS.in(schema))
		);
		schema.register(
			map,
			FFConstants.string("disc_camera"),
			(string) -> DSL.optionalFields("CameraContents", References.DATA_COMPONENTS.in(schema))
		);
	}
}
