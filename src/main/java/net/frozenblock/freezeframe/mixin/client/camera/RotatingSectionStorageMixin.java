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

package net.frozenblock.freezeframe.mixin.client.camera;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.screenshot.FFScreenshotUtil;
import net.minecraft.client.RotatingSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(RotatingSectionStorage.class)
public class RotatingSectionStorageMixin {

	@WrapWithCondition(
		method = "repositionCenter",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/RotatingSectionStorage$Value;setSectionNode(J)V"
		)
	)
	public boolean freezeFrame$cancelRecompileChunksInFeed(RotatingSectionStorage.Value instance, long sectionNode) {
		return !FFScreenshotUtil.screenshottingAndTripod();
	}
}
