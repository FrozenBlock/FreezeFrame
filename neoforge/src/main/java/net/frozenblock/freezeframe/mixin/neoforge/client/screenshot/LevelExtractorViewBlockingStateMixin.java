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

package net.frozenblock.freezeframe.mixin.neoforge.client.screenshot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.frozenblock.freezeframe.client.screenshot.FFScreenshotUtil;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ClientOnly
@Mixin(LevelExtractor.class)
public class LevelExtractorViewBlockingStateMixin {

	@Inject(method = "getViewBlockingStateAndPos", at = @At("HEAD"))
	private static void freezeFrame$setCameraEntityIfNeeded(
		LocalPlayer player, Frustum frustum, CallbackInfoReturnable<BlockState> info,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		if (FFScreenshotUtil.screenshottingAndTripod()) entityLocalRef.set(Minecraft.getInstance().getCameraEntity());
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/player/LocalPlayer;noPhysics:Z",
			opcode = Opcodes.GETFIELD
		)
	)
	private static boolean freezeFrame$fixNoPhysicsCheck(boolean original) {
		if (FFScreenshotUtil.screenshottingAndTripod()) return false;
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getEyePosition()Lnet/minecraft/world/phys/Vec3;"
		)
	)
	private static Vec3 freezeFrame$fixEyePosition(
		Vec3 original,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getEyePosition();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getX()D"
		)
	)
	private static double freezeFrame$fixPosX(
		double original,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getX();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getEyeY()D"
		)
	)
	private static double freezeFrame$fixPosY(
		double original,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getEyeY();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D"
		)
	)
	private static double freezeFrame$fixPosZ(
		double original,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getZ();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getBbWidth()F"
		)
	)
	private static float freezeFrame$fixBbWidth(
		float original,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getBbWidth();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingStateAndPos",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;getScale()F"
		)
	)
	private static float freezeFrame$fixScale(
		float original,
		@Share("freezeFrame$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return (cameraEntity instanceof LivingEntity livingEntity) ? livingEntity.getScale() : 1F;
		return original;
	}
}
