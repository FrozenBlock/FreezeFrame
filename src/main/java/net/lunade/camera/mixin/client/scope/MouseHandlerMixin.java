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

package net.lunade.camera.mixin.client.scope;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.client.ScopeZoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapOperation(
		method = "onScroll",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"
		)
	)
	private void cameraPort$zoomInsteadOfChangingHotbarSlots(
		Inventory instance, int selected, Operation<Void> original,
		@Local(name = "wheel") int wheel
	) {
		if (this.minecraft.player == null || !ScopeItemHelper.isPlayerUsingScopeItem(this.minecraft.player)) {
			original.call(instance, selected);
			return;
		}

		ScopeZoomManager.adjustZoom(this.minecraft, this.minecraft.player, -wheel);
	}
}
