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

package net.lunade.camera.mixin.client.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.ScopeZoomHelper;
import net.lunade.camera.util.client.CameraZoomManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Inventory.class)
public class InventoryMixin {

	@Inject(method = "setSelectedSlot", at = @At("HEAD"), cancellable = true)
	private void cameraPort$convertHotbarScrollToZoom(int slot, CallbackInfo info) {
		final Minecraft minecraft = Minecraft.getInstance();
		final LocalPlayer player = minecraft.player;
		if (player == null || !ScopeItemHelper.isPlayerUsingScopeItem(player)) return;

		final Inventory inventory = (Inventory) (Object) this;
		if (inventory != player.getInventory()) return;

		final int current = inventory.getSelectedSlot();
		if (slot == current) {
			info.cancel();
			return;
		}

		int delta = slot - current;
		if (delta > 4) delta -= 9;
		if (delta < -4) delta += 9;

		if (delta != 0) {
			if (CameraZoomManager.adjustZoom(delta)) {
				ScopeZoomHelper.setStoredZoom(player.getUseItem(), CameraZoomManager.getZoom());
			}
		}
		info.cancel();
	}
}
