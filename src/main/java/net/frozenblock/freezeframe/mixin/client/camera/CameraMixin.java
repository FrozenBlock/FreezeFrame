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
import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.frozenblock.freezeframe.util.client.CameraScreenshotManager;
import net.frozenblock.freezeframe.util.client.ScopeZoomManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public class CameraMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Nullable
	private Entity entity;

	@ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
	public boolean freezeFrame$isDetached(boolean original) {
		return original && !CameraScreenshotManager.isScreenshotting();
	}

	@ModifyReturnValue(
		method = "calculateFov",
		at = @At(
			value = "RETURN",
			ordinal = 0
		)
	)
	private float freezeFrame$applyFovToPhotograph(float original) {
		if (this.minecraft.player == null || this.entity != minecraft.player) return original;
		if (!CameraScreenshotManager.isScreenshottingFromHandheldCamera() && !ScopeItemHelper.isPlayerUsingCamera(this.minecraft.player)) return original;

		final float cameraFovModifier = ScopeZoomHelper.toFovModifier(ScopeZoomManager.getZoom());
		return original * cameraFovModifier;
	}

}
