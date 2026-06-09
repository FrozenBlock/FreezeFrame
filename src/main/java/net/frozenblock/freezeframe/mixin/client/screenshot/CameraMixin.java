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

package net.frozenblock.freezeframe.mixin.client.screenshot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.screenshot.FFScreenshotUtil;
import net.minecraft.client.Camera;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public class CameraMixin {

	@ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
	public boolean freezeFrame$ignoreDetachedIfScreenshotting(boolean original) {
		return original && !FFScreenshotUtil.screenshotting();
	}

	@ModifyReturnValue(method = "attributeProbe", at = @At("RETURN"))
	public EnvironmentAttributeProbe freezeFrame$useTripodAttributeProbe(EnvironmentAttributeProbe original) {
		if (FFScreenshotUtil.screenshottingAndTripod()) {
			final EnvironmentAttributeProbe probe = FFScreenshotUtil.environmentAttributeProbe();
			if (probe != null) return probe;
		}
		return original;
	}
}
