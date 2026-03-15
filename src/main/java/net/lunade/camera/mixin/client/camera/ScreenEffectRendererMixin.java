/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
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

package net.lunade.camera.mixin.client.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

	@Inject(method = "getViewBlockingState", at = @At("HEAD"))
	private static void cameraPort$setCameraEntityIfNeeded(
		Player player, CallbackInfoReturnable<BlockState> info,
		@Share("cameraPort$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		if (CameraScreenshotManager.isScreenshottingFromTripodCamera()) entityLocalRef.set(Minecraft.getInstance().getCameraEntity());
	}

	@ModifyExpressionValue(
		method = "renderScreenEffect",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/entity/player/Player;noPhysics:Z",
			opcode = Opcodes.GETFIELD
		)
	)
	private static boolean cameraPort$fixNoPhysicsCheck(boolean original) {
		if (CameraScreenshotManager.isScreenshottingFromTripodCamera()) return false;
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;getX()D"
		)
	)
	private static double cameraPort$fixPosX(
		double original,
		@Share("cameraPort$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getX();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;getEyeY()D"
		)
	)
	private static double cameraPort$fixPosY(
		double original,
		@Share("cameraPort$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getEyeY();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;getZ()D"
		)
	)
	private static double cameraPort$fixPosZ(
		double original,
		@Share("cameraPort$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getZ();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;getBbWidth()F"
		)
	)
	private static float cameraPort$fixBbWdith(
		float original,
		@Share("cameraPort$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return cameraEntity.getBbWidth();
		return original;
	}

	@ModifyExpressionValue(
		method = "getViewBlockingState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;getScale()F"
		)
	)
	private static float cameraPort$fixScale(
		float original,
		@Share("cameraPort$cameraEntity") LocalRef<Entity> entityLocalRef
	) {
		final Entity cameraEntity = entityLocalRef.get();
		if (cameraEntity != null) return (cameraEntity instanceof LivingEntity livingEntity) ? livingEntity.getScale() : 1F;
		return original;
	}

}
