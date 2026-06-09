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

package net.frozenblock.freezeframe.mixin.client.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.screenshot.FFScreenshotUtil;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@ModifyExpressionValue(
		method = {"render", "renderLevel"},
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/renderer/GameRenderer;mainRenderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;",
			opcode = Opcodes.GETFIELD
		)
	)
	public RenderTarget freezeFrame$useCorrectRenderTarget(RenderTarget original) {
		if (!FFScreenshotUtil.screenshotting()) return original;
		final RenderTarget screenshotTarget = FFScreenshotUtil.getRenderTarget();
		return screenshotTarget != null ? screenshotTarget : original;
	}

	@ModifyReturnValue(method = "mainRenderTarget", at = @At("RETURN"))
	public RenderTarget freezeFrame$getMainRenderTarget(RenderTarget original) {
		if (!FFScreenshotUtil.screenshotting()) return original;
		final RenderTarget screenshotTarget = FFScreenshotUtil.getRenderTarget();
		return screenshotTarget != null ? screenshotTarget : original;
	}
}
