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

package net.lunade.camera.mixin.client.photograph;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm);

	@Inject(
		method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
			ordinal = 0,
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	private void cameraPort$renderArmWithItem(
		AbstractClientPlayer player,
		float frameInterp,
		float xRot,
		InteractionHand hand,
		float attackValue,
		ItemStack stack,
		float inverseArmHeight,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int lightCoords,
		CallbackInfo info,
		@Local(name = "arm") HumanoidArm arm
	) {
		if (!stack.is(CameraPortItems.PHOTOGRAPH)) return;

		final Photograph photograph = stack.get(CameraPortDataComponents.PHOTOGRAPH);
		if (photograph == null) return;

		this.cameraPort$submitPhotographInHand(poseStack, collector, lightCoords, inverseArmHeight, attackValue, arm, photograph.identifier());
		info.cancel();
	}

	@Unique
	private void cameraPort$submitPhotographInHand(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int lightCoords,
		float inverseArmHeight,
		float attackValue,
		HumanoidArm arm,
		Identifier photographId
	) {
		poseStack.pushPose();

		final float invert = arm == HumanoidArm.RIGHT ? 1F : -1F;
		poseStack.translate(invert * 0.125F, -0.125F, 0F);
		if (!this.minecraft.player.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 10F));
			this.renderPlayerArm(poseStack, collector, lightCoords, inverseArmHeight, attackValue, arm);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate(invert * 0.51F, -0.08F + inverseArmHeight * -1.2F, -0.75F);
		final float sqrtAttackValue = Mth.sqrt(attackValue);
		final float xSwing = Mth.sin(sqrtAttackValue * Mth.PI);
		final float xSwingPosition = -0.5F * xSwing;
		final float ySwingPosition = 0.4F * Mth.sin(sqrtAttackValue * (Mth.PI * 2F));
		final float zSwingPosition = -0.3F * Mth.sin(attackValue * Mth.PI);
		poseStack.translate(invert * xSwingPosition, ySwingPosition - 0.3F * xSwing, zSwingPosition);
		poseStack.mulPose(Axis.XP.rotationDegrees(xSwing * -45F));
		poseStack.mulPose(Axis.YP.rotationDegrees(invert * xSwing * -30F));
		this.cameraPort$renderPhotograph(poseStack, collector, lightCoords, photographId);
		poseStack.popPose();

		poseStack.popPose();
	}

	@Unique
	private void cameraPort$renderPhotograph(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, Identifier photographId) {
		poseStack.mulPose(Axis.YP.rotationDegrees(180F));
		poseStack.scale(0.38F, 0.38F, 0.38F);
		PhotographRenderer.submit(poseStack, collector, photographId, lightCoords, PhotographRenderer.FrameType.FRAME_FULL);
	}
}
