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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.model.FreezeFrameArmPoses;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin {

	@WrapOperation(
		method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/layers/ItemInHandLayer;submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
		)
	)
	public <S extends ArmedEntityRenderState> void freezeFrame$renderHeldCameraRelativeToHead(
		PlayerItemInHandLayer instance,
		S state,
		ItemStackRenderState item,
		ItemStack itemStack,
		HumanoidArm arm,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		int lightCoords,
		Operation<Void> original
	) {
		final HumanoidModel.ArmPose armPose = arm == HumanoidArm.RIGHT ? state.rightArmPose : state.leftArmPose;
		if (arm == state.mainArm && (armPose == FreezeFrameArmPoses.CAMERA || armPose == FreezeFrameArmPoses.CAMERA_ONE_ARM)) {
			this.freezeFrame$renderHeldCamera((AvatarRenderState) state, item, arm, poseStack, submitNodeCollector, lightCoords);
			return;
		}
		original.call(instance, state, item, itemStack, arm, poseStack, submitNodeCollector, lightCoords);
	}

	@Unique
	private <S extends AvatarRenderState> void freezeFrame$renderHeldCamera(S state, ItemStackRenderState item, HumanoidArm arm, final PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
		poseStack.pushPose();
		final EntityModel model = PlayerItemInHandLayer.class.cast(this).getParentModel();
		model.root().translateAndRotate(poseStack);
		final ModelPart head = ((HeadedModel) model).getHead();
		if (!state.isUsingItem) {
			final float previousXRot = head.xRot;
			head.xRot = Mth.clamp(previousXRot, -Mth.TWO_PI, FreezeFrameArmPoses.HIGHEST_LOOK_ROT);
			head.translateAndRotate(poseStack);
			head.xRot = previousXRot;
		} else {
			head.translateAndRotate(poseStack);
		}
		CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
		final boolean isLeftHand = arm == HumanoidArm.LEFT;
		final float xOffset = state.isUsingItem ? 7.5F : 5F;
		poseStack.translate((isLeftHand ? -xOffset : xOffset) / 16F, (state.isUsingItem ? -0.125F : -0.6F), state.isUsingItem ? -0.6F : -1F);
		poseStack.scale(1.73913043478F, 1.73913043478F, 1.73913043478F);
		item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}

}
