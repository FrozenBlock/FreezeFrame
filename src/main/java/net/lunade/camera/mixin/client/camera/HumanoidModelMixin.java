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
import net.lunade.camera.client.model.object.camera.CameraPortArmPoses;
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
			target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V"
		)
	)
	public boolean cameraPort$modifyLeftArmBobbing(
		ModelPart modelPart, float ageInTicks, float scale,
		@Local(name = "rightArmPose") HumanoidModel.ArmPose rightArmPose,
		@Local(name = "leftArmPose") HumanoidModel.ArmPose leftArmPose
	) {
		return rightArmPose != CameraPortArmPoses.CAMERA && leftArmPose != CameraPortArmPoses.CAMERA;
	}

	@Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
	private void cameraPort$poseRightArm(T state, CallbackInfo info) {
		if (state.rightArmPose != CameraPortArmPoses.CAMERA) return;
		this.cameraPort$poseArmsForCamera(state.isUsingItem);
		info.cancel();
	}

	@Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
	private void cameraPort$poseLeftArm(T state, CallbackInfo info) {
		if (state.leftArmPose != CameraPortArmPoses.CAMERA) return;
		this.cameraPort$poseArmsForCamera(true);
		info.cancel();
	}

	@Unique
	private void cameraPort$poseArmsForCamera(boolean using) {
		this.rightArm.yRot = -0.3F + (this.head.yRot);
		this.leftArm.yRot = 0.3F + (this.head.yRot);

		final float zRotScale = this.head.xRot < 0 && using ? 0.3F : 0.15F;
		this.rightArm.zRot = this.head.xRot * -zRotScale;
		this.leftArm.zRot = this.head.xRot * zRotScale;
		this.rightArm.xRot = -Mth.HALF_PI + this.head.xRot + (using ? -0.25F : 0.1F);
		this.leftArm.xRot = -Mth.HALF_PI + this.head.xRot + (using ? -0.25F : 0.1F);
	}
}
