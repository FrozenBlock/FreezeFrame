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

package net.lunade.camera.mixin.client.photograph;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.client.renderer.entity.state.impl.CameraPortRenderStateDataKeys;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.registry.CameraPortDataComponents;
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

@Environment(EnvType.CLIENT)
@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin<T extends ItemFrame> {

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ItemFrame;Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;F)V",
		at = @At("TAIL")
	)
	public void cameraPort$addPhotoToRenderState(T itemFrame, ItemFrameRenderState renderState, float partialTicks, CallbackInfo info) {
		final Photograph photograph = itemFrame.getItem().get(CameraPortDataComponents.PHOTOGRAPH);
		renderState.setData(
			CameraPortRenderStateDataKeys.PHOTOGRAPH_ID,
			photograph == null ? null : photograph.identifier()
		);
	}

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
		@Share("cameraPort$photographId") LocalRef<Identifier> photographIdRef
	) {
		final Identifier photoId = renderState.getData(CameraPortRenderStateDataKeys.PHOTOGRAPH_ID);
		if (photoId == null) return original;

		photographIdRef.set(photoId);
		return original % 4 * 2;
	}

	@WrapOperation(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemFrameRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
		)
	)
	public void cameraPort$submit(
		ItemStackRenderState instance, PoseStack poseStack, SubmitNodeCollector collector, int lightVal, int overlay, int outlineColor, Operation<Void> original,
		@Share("cameraPort$photographId") LocalRef<Identifier> photographIdRef
	) {
		final Identifier photographId = photographIdRef.get();
		if (photographId != null) {
			// 0.625F
			poseStack.scale(1.25F, 1.25F, 1.25F);
			poseStack.translate(0F, 0F, 0.03125F);
			PhotographRenderer.submit(poseStack, collector, photographId, lightVal, PhotographRenderer.FrameType.NONE);
			return;
		}
		original.call(instance, poseStack, collector, lightVal, overlay, outlineColor);
	}
}
