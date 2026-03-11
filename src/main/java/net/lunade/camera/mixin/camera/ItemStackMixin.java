package net.lunade.camera.mixin.camera;

import com.mojang.serialization.DataResult;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@Shadow
	private static DataResult<?> validateContainedItemSizes(Iterable<? extends ItemInstance> items) {
		throw new AssertionError("Mixin injection failed - Camera Port ItemStackMixin.");
	}

	@Inject(method = "validateComponents", at = @At("TAIL"), cancellable = true)
	private static void cameraPort$validateCameraContentsComponent(DataComponentMap components, CallbackInfoReturnable<DataResult<?>> info) {
		final CameraContents cameraContents = components.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (cameraContents == null) return;

		final DataResult<?> result = validateContainedItemSizes(cameraContents.items());
		if (result.isError()) info.setReturnValue(result);
	}

}
