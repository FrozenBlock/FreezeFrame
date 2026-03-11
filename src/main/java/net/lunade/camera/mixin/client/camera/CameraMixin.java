package net.lunade.camera.mixin.client.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public class CameraMixin {

	@ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
	public boolean cameraPort$isDetached(boolean original) {
		return original && !CameraScreenshotManager.isPossessingCamera();
	}

}
