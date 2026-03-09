package net.lunade.camera.mixin.photograph;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.client.renderer.entity.state.impl.ItemFrameRenderStateInterface;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin<T extends ItemFrame> {

	@ModifyExpressionValue(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;rotation:I",
			ordinal = 1,
			opcode = Opcodes.GETFIELD
		)
	)
	public int cameraPort$fixRotationAndCapturePhotographComponent(
		int original,
		@Local(argsOnly = true) ItemFrameRenderState renderState,
		@Share("cameraPort$photoLocation") LocalRef<Identifier> photoLocationRef
	) {
		if (!(renderState instanceof ItemFrameRenderStateInterface renderStateInterface)) return original;

		final Identifier photographLocation = renderStateInterface.cameraPort$getPhotographLocation();
		if (photographLocation == null) return original;

		photoLocationRef.set(photographLocation);
		return original % 4 * 2;
	}

	@WrapOperation(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
		)
	)
	public void cameraPort$render(
		ItemStackRenderState instance, PoseStack poseStack, SubmitNodeCollector collector, int submitNodeCollector, int lightCoords, int overlayCoords, Operation<Void> original,
		@Share("cameraPort$photoLocation") LocalRef<Identifier> photoLocationRef
	) {
		final Identifier photographLocation = photoLocationRef.get();
		if (photographLocation != null) {
			// 0.625F
			poseStack.scale(1.25F, 1.25F, 1.25F);
			poseStack.translate(0F, 0F, 0.03125F);
			PhotographRenderer.submit(poseStack, collector, photographLocation, lightCoords, false);
		} else {
			original.call(instance, poseStack, collector, submitNodeCollector, lightCoords, overlayCoords);
		}
	}

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
		at = @At("TAIL")
	)
	public void cameraPort$addPhotoToRenderState(T itemFrame, ItemFrameRenderState renderState, float partialTicks, CallbackInfo info) {
		if (!(renderState instanceof ItemFrameRenderStateInterface renderStateInterface)) return;

		final PhotographComponent photographComponent = itemFrame.getItem().get(CameraPortItems.PHOTO_COMPONENT);
		if (photographComponent != null) {
			renderStateInterface.cameraPort$addPhotographLocation(photographComponent.identifier());
		} else {
			renderStateInterface.cameraPort$addPhotographLocation(null);
		}
	}
}
