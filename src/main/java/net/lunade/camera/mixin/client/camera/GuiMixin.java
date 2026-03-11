package net.lunade.camera.mixin.client.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.camera.CameraScreenshotManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void cameraPort$removeOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
		if (CameraScreenshotManager.isUsingSelfRenderingCamera()) info.cancel();
	}

}
