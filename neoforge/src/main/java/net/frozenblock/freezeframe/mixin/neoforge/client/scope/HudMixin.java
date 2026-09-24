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

package net.frozenblock.freezeframe.mixin.neoforge.client.scope;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(Hud.class)
public abstract class HudMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Nullable
	protected abstract Player getCameraPlayer();

	@Unique
	private static final Identifier FREEZE_FRAME$CAMERA_SCOPE = FFConstants.id("textures/misc/camera_scope.png");

	@Unique
	private boolean freezeFrame$isPlayerScopingInFirstPerson(Player player) {
		return player != null
			&& player.isScoping()
			&& ScopeItemHelper.isPlayerUsingScopeItem(player)
			&& this.minecraft.options.getCameraType().isFirstPerson();
	}

	@Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
	public void freezeFrame$removeCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo info) {
		if (FFConfig.SCOPE_HIDES_HOTBAR.get() && this.freezeFrame$isPlayerScopingInFirstPerson(this.minecraft.player)) info.cancel();
	}

	@Inject(
		method = {
			"extractExperienceLevel",
			"extractItemHotbar",
			"extractContextualInfoBarBackground",
			"extractContextualInfoBar",
			"extractHealthLevel",
			"extractArmorLevel",
			"extractFoodLevel",
			"extractAirLevel",
			"extractVehicleHealth",
			"maybeExtractSelectedItemName"
		},
		at = @At("HEAD"),
		cancellable = true
	)
	public void freezeFrame$removeHotbar(CallbackInfo info) {
		if (FFConfig.SCOPE_HIDES_HOTBAR.get() && this.freezeFrame$isPlayerScopingInFirstPerson(this.getCameraPlayer())) info.cancel();
	}

	@ModifyExpressionValue(
		method = "extractSpyglassOverlay",
		at = @At(
			value = "INVOKE",
			target = "Lnet/neoforged/neoforge/client/extensions/common/IClientItemExtensions;getScopeOverlayTexture(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/resources/Identifier;"
		)
	)
	private Identifier freezeFrame$useCameraOverlay(Identifier original) {
		if (this.minecraft.player == null || !ScopeItemHelper.isCameraItem(this.minecraft.player.getUseItem())) return original;
		return FREEZE_FRAME$CAMERA_SCOPE;
	}
}
