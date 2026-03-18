package net.lunade.camera.client.model.object.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;

@Environment(EnvType.CLIENT)
public class CameraPortArmPoses {
	public static HumanoidModel.ArmPose CAMERA;

	static {
		HumanoidModel.ArmPose.values();
	}
}
