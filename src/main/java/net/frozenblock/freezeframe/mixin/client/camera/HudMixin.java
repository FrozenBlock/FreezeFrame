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
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Hud.class)
public class HudMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private static final Identifier FREEZE_FRAME$CAMERA_SCOPE = FFConstants.id("textures/misc/camera_scope.png");

	@WrapWithCondition(
		method = "extractRenderState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Hud;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
		)
	)
	public boolean freezeFrame$removeCrosshair(
		Hud instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
		@Share("freezeFrame$isPlayerScopingInFirstPerson") LocalBooleanRef isPlayerScopingInFirstPerson
	) {
		isPlayerScopingInFirstPerson.set(this.minecraft.player != null && this.minecraft.player.isScoping() && this.minecraft.options.getCameraType().isFirstPerson());
		return !FFConfig.SCOPE_HIDES_CROSSHAIR.get() || !isPlayerScopingInFirstPerson.get();
	}

	@WrapWithCondition(
		method = "extractRenderState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/Hud;extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"
		)
	)
	public boolean freezeFrame$removeHotbar(
		Hud instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
		@Share("freezeFrame$isPlayerScopingInFirstPerson") LocalBooleanRef isPlayerScopingInFirstPerson
	) {
		return !FFConfig.SCOPE_HIDES_HOTBAR.get() || !isPlayerScopingInFirstPerson.get();
	}

	@ModifyExpressionValue(
		method = "extractSpyglassOverlay",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/gui/Hud;SPYGLASS_SCOPE_LOCATION:Lnet/minecraft/resources/Identifier;",
			opcode = Opcodes.GETSTATIC
		)
	)
	private Identifier freezeFrame$useCameraOverlay(Identifier original) {
		if (this.minecraft.player == null || !ScopeItemHelper.isCameraItem(this.minecraft.player.getUseItem())) return original;
		return FREEZE_FRAME$CAMERA_SCOPE;
	}
}
