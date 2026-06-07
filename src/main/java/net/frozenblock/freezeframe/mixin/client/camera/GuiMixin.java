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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.gui.screens.inventory.book.BookPagePhotographScreen;
import net.frozenblock.freezeframe.client.gui.screens.inventory.book.BookPagePhotographUiState;
import net.frozenblock.freezeframe.client.screenshot.FFScreenshotUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public class GuiMixin {

	@Shadow
	@Final
	private GuiRenderState guiRenderState;

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$disableGuiWhileScreenshotting(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo info) {
		if (!FFScreenshotUtil.screenshotting()) return;
		info.cancel();
		this.guiRenderState.reset();
		this.guiRenderState.isHudHidden = true;
	}

	@Inject(method = "setScreen", at = @At("HEAD"))
	private void freezeFrame$clearBookPhotoSuppressionOnScreenSwap(@Nullable Screen screen, CallbackInfo info) {
		if (!(screen instanceof BookPagePhotographScreen)) BookPagePhotographUiState.setSuppressBookEditorPhotoControls(false);
	}
}
