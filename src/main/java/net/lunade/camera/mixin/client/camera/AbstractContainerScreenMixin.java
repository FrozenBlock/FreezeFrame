package net.lunade.camera.mixin.client.camera;

import net.lunade.camera.client.gui.CameraMouseActions;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

	@Shadow
	protected abstract void addItemSlotMouseAction(ItemSlotMouseAction itemSlotMouseAction);

	@Inject(method = "init", at = @At("TAIL"))
	public void cameraPort$addCameraMouseActions(CallbackInfo info) {
		this.addItemSlotMouseAction(new CameraMouseActions(AbstractContainerScreen.class.cast(this).minecraft));
	}
}
