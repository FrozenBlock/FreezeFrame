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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.screenshot.CameraScreenshotManager;
import net.frozenblock.freezeframe.config.FFConfig;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(MobEffectFogEnvironment.class)
public class MobEffectFogEnvironmentMixin {

	@ModifyReturnValue(method = "isApplicable", at = @At("RETURN"))
	public boolean freezeFrame$removeFog(boolean original) {
		return !(CameraScreenshotManager.screenshotData().screenshotting() && FFConfig.CAMERA_IGNORES_EFFECT_FOG.get()) && original;
	}
}
