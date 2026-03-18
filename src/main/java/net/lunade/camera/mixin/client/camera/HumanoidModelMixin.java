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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.model.CameraPortArmPoses;
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
	public boolean cameraPort$modifyRightArmBobbing(
		ModelPart modelPart, float ageInTicks, float scale,
		@Local(name = "rightArmPose") HumanoidModel.ArmPose rightArmPose,
		@Local(name = "leftArmPose") HumanoidModel.ArmPose leftArmPose
	) {
		return rightArmPose != CameraPortArmPoses.CAMERA && rightArmPose != CameraPortArmPoses.CAMERA_ONE_ARM && leftArmPose != CameraPortArmPoses.CAMERA;
	}

	@WrapWithCondition(
		method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V",
			ordinal = 1
		)
	)
	public boolean cameraPort$modifyLeftArmBobbing(
		ModelPart modelPart, float ageInTicks, float scale,
		@Local(name = "rightArmPose") HumanoidModel.ArmPose rightArmPose,
		@Local(name = "leftArmPose") HumanoidModel.ArmPose leftArmPose
	) {
		return leftArmPose != CameraPortArmPoses.CAMERA && leftArmPose != CameraPortArmPoses.CAMERA_ONE_ARM && rightArmPose != CameraPortArmPoses.CAMERA;
	}

	@Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
	private void cameraPort$poseRightArm(T state, CallbackInfo info) {
		final boolean oneArm = state.rightArmPose == CameraPortArmPoses.CAMERA_ONE_ARM;
		if (state.rightArmPose != CameraPortArmPoses.CAMERA && !oneArm) return;
		this.cameraPort$poseRightArmForCamera(state.isUsingItem);
		if (!oneArm) this.cameraPort$poseLeftArmForCamera(state.isUsingItem);
		info.cancel();
	}

	@Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
	private void cameraPort$poseLeftArm(T state, CallbackInfo info) {
		final boolean oneArm = state.leftArmPose == CameraPortArmPoses.CAMERA_ONE_ARM;
		if (state.leftArmPose != CameraPortArmPoses.CAMERA && !oneArm) return;
		this.cameraPort$poseLeftArmForCamera(state.isUsingItem);
		if (!oneArm) this.cameraPort$poseRightArmForCamera(state.isUsingItem);
		info.cancel();
	}

	@Unique
	private void cameraPort$poseLeftArmForCamera(boolean using) {
		this.leftArm.yRot = 0.3F + (this.head.yRot);

		final float clampedHeadXRot = using ? this.head.xRot : Mth.clamp(this.head.xRot, -Mth.TWO_PI, CameraPortArmPoses.HIGHEST_LOOK_ROT);
		final float zRotScale = clampedHeadXRot < 0 && using ? 0.3F : 0.15F;
		this.leftArm.zRot = clampedHeadXRot * zRotScale;
		this.leftArm.xRot = -Mth.HALF_PI + clampedHeadXRot + (using ? -0.45F : 0.1F);
	}

	@Unique
	private void cameraPort$poseRightArmForCamera(boolean using) {
		this.rightArm.yRot = -0.3F + (this.head.yRot);

		final float clampedHeadXRot = using ? this.head.xRot : Mth.clamp(this.head.xRot, -Mth.TWO_PI, CameraPortArmPoses.HIGHEST_LOOK_ROT);
		final float zRotScale = clampedHeadXRot < 0 && using ? 0.3F : 0.15F;
		this.rightArm.zRot = clampedHeadXRot * -zRotScale;
		this.rightArm.xRot = -Mth.HALF_PI + clampedHeadXRot + (using ? -0.45F : 0.1F);
	}
}
