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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.frozenblock.freezeframe.client.screenshot.FFScreenshotUtil;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private boolean freezeFrame$wasScreenshotting;

	@Shadow
	private boolean shouldResetSkyRenderer;

	/**
	 * This fixes an issue on NeoForge that caused the sky to not render while screenshotting.
	 */
	@Inject(method = "extract", at = @At("HEAD"))
	public void freezeFrame$resetSkyRendererWhileScreenshotting(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo info) {
		if (FFScreenshotUtil.screenshotting() && !this.freezeFrame$wasScreenshotting) {
			this.shouldResetSkyRenderer = true;
			this.freezeFrame$wasScreenshotting = true;
		} else if (this.freezeFrame$wasScreenshotting) {
			this.shouldResetSkyRenderer = true;
			this.freezeFrame$wasScreenshotting = false;
		}
	}

	@ModifyExpressionValue(
		method = "extractVisibleEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;",
			ordinal = 3
		)
	)
	public Entity freezeFrame$trickIntoRenderingPlayer(
		Entity original,
		@Local(name = "entity") Entity entity
	) {
		if (this.minecraft.player == null) return original;
		if (FFScreenshotUtil.notScreenshottingOrIsTripod()) return original;

		return entity == this.minecraft.player ? entity : original;
	}

	@WrapWithCondition(
		method = "extract",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;emitGizmos(Lnet/minecraft/client/renderer/culling/Frustum;DDDF)V"
		)
	)
	public boolean freezeFrame$ignoreGizmosWhileScreenshotting(DebugRenderer instance, Frustum frustum, double camX, double camY, double camZ, float partialTicks) {
		return !FFScreenshotUtil.screenshotting();
	}
}
