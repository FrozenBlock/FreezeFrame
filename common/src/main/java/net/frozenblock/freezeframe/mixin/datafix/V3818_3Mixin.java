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

package net.frozenblock.freezeframe.mixin.datafix;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V3818_3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(V3818_3.class)
public class V3818_3Mixin {

	@ModifyReturnValue(method = "components", at = @At("RETURN"))
	private static SequencedMap<String, Supplier<TypeTemplate>> freezeFrame$injectCameraContentsComponent(
		SequencedMap<String, Supplier<TypeTemplate>> original,
		Schema schema
	) {
		original.put(FFConstants.string("camera_contents"), () -> DSL.list(References.ITEM_STACK.in(schema)));
		return original;
	}
}
