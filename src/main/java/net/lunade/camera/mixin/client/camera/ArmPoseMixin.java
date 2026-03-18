/*
 * Copyright 2025-2026 FrozenBlock
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

import java.util.ArrayList;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.model.object.camera.CameraPortArmPoses;
import net.minecraft.client.model.HumanoidModel;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HumanoidModel.ArmPose.class)
public class ArmPoseMixin {

	//CREDIT TO nyuppo/fabric-boat-example ON GITHUB

	@SuppressWarnings("ShadowTarget")
	@Final
	@Shadow
	@Mutable
	private static HumanoidModel.ArmPose[] $VALUES;

	@SuppressWarnings("InvokerTarget")
	@Invoker("<init>")
	private static HumanoidModel.ArmPose cameraPort$newArmPose(String internalName, int internalId, boolean twoHanded, boolean affectsOffhandPose) {
		throw new AssertionError("Mixin injection failed - Camera Port ArmPoseMixin.");
	}

	@Inject(
		method = "<clinit>",
		at = @At(value = "FIELD",
			opcode = Opcodes.PUTSTATIC,
			target = "Lnet/minecraft/client/model/HumanoidModel$ArmPose;$VALUES:[Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
			shift = At.Shift.AFTER
		)
	)
	private static void cameraPort$addCameraArmPose(CallbackInfo info) {
		final var armPoses = new ArrayList<>(Arrays.asList($VALUES));
		final var last = armPoses.get(armPoses.size() - 1);

		final var camera = cameraPort$newArmPose(
			"CAMERAPORTCAMERA",
			last.ordinal() + 1,
			true,
			true
		);
		CameraPortArmPoses.CAMERA = camera;
		armPoses.add(camera);

		$VALUES = armPoses.toArray(new HumanoidModel.ArmPose[0]);
	}
}
