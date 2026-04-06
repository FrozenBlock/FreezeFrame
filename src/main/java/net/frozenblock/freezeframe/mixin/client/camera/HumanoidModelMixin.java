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
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.model.FreezeFrameArmPoses;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {

	@Shadow
	@Final
	public ModelPart head;

	@Shadow
	@Final
	public ModelPart rightArm;

	@Shadow
	@Final
	public ModelPart leftArm;

	@WrapWithCondition(
		method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V",
			ordinal = 0
		)
	)
	public boolean freezeFrame$modifyRightArmBobbing(
		ModelPart modelPart, float ageInTicks, float scale,
		@Local(name = "rightArmPose") HumanoidModel.ArmPose rightArmPose,
		@Local(name = "leftArmPose") HumanoidModel.ArmPose leftArmPose
	) {
		return rightArmPose != FreezeFrameArmPoses.CAMERA && rightArmPose != FreezeFrameArmPoses.CAMERA_ONE_ARM && leftArmPose != FreezeFrameArmPoses.CAMERA;
	}

	@WrapWithCondition(
		method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V",
			ordinal = 1
		)
	)
	public boolean freezeFrame$modifyLeftArmBobbing(
		ModelPart modelPart, float ageInTicks, float scale,
		@Local(name = "rightArmPose") HumanoidModel.ArmPose rightArmPose,
		@Local(name = "leftArmPose") HumanoidModel.ArmPose leftArmPose
	) {
		return leftArmPose != FreezeFrameArmPoses.CAMERA && leftArmPose != FreezeFrameArmPoses.CAMERA_ONE_ARM && rightArmPose != FreezeFrameArmPoses.CAMERA;
	}

	@Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$poseRightArm(T state, CallbackInfo info) {
		final boolean oneArm = state.rightArmPose == FreezeFrameArmPoses.CAMERA_ONE_ARM;
		if (state.rightArmPose != FreezeFrameArmPoses.CAMERA && !oneArm) return;
		this.freezeFrame$poseArmForCamera(false, state.isUsingItem, state.isCrouching);
		if (!oneArm) this.freezeFrame$poseArmForCamera(true, state.isUsingItem, state.isCrouching);
		info.cancel();
	}

	@Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$poseLeftArm(T state, CallbackInfo info) {
		final boolean oneArm = state.leftArmPose == FreezeFrameArmPoses.CAMERA_ONE_ARM;
		if (state.leftArmPose != FreezeFrameArmPoses.CAMERA && !oneArm) return;
		this.freezeFrame$poseArmForCamera(true, state.isUsingItem, state.isCrouching);
		if (!oneArm) this.freezeFrame$poseArmForCamera(false, state.isUsingItem, state.isCrouching);
		info.cancel();
	}

	@Unique
	private void freezeFrame$poseArmForCamera(boolean isLeftArm, boolean using, boolean crouching) {
		final ModelPart arm = isLeftArm ? this.leftArm : this.rightArm;
		arm.yRot = (isLeftArm ? 0.3F : -0.3F) + (this.head.yRot);

		final float clampedHeadXRot = using ? this.head.xRot : Mth.clamp(this.head.xRot, -Mth.TWO_PI, FreezeFrameArmPoses.HIGHEST_LOOK_ROT);
		final float zRotScale = clampedHeadXRot < 0 && using ? 0.3F : 0.15F;
		arm.zRot = clampedHeadXRot * (isLeftArm ? zRotScale : -zRotScale);
		arm.xRot = -Mth.HALF_PI + clampedHeadXRot + (using ? (crouching ? -0.75F : -0.45F) : (crouching ? -0.2F : 0.1F));
	}
}
