/*
 * Copyright 2026 FrozenBlock
 * This file is part of Freeze Frame.
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

package net.frozenblock.freezeframe.mixin.client.photograph;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

	@WrapOperation(
		method = "submitArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
		)
	)
	public void freezeFrame$renderPhotograph(
		ItemStackRenderState instance, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, Operation<Void> original,
		@Local(argsOnly = true) ItemStack itemStack
	) {
		renderPhotograph: {
			if (!FFConfig.THIRD_PERSON_PHOTOGRAPH_ITEM.get() || !itemStack.is(FFItems.PHOTOGRAPH)) break renderPhotograph;

			final Photograph photograph = itemStack.get(FFDataComponents.PHOTOGRAPH);
			if (photograph == null) break renderPhotograph;

			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(180F));
			poseStack.translate(0F, 0.188975F, -0.0625F);
			poseStack.scale(0.38F, 0.38F, 0.38F);
			PhotographRenderer.submit(poseStack, submitNodeCollector, photograph.identifier(), lightCoords, PhotographRenderer.FrameType.FRAME, PhotographRenderer.FrameType.FRAME_BACK);
			poseStack.popPose();
			return;
		}

		original.call(instance, poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
	}

}
