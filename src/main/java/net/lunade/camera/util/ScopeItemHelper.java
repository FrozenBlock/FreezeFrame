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

package net.lunade.camera.util;

import net.lunade.camera.item.CameraItem;
import net.lunade.camera.tag.CameraPortItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ScopeItemHelper {

	private ScopeItemHelper() {
	}

	public static boolean isCameraItem(ItemStack stack) {
		return stack.is(CameraPortItemTags.CAMERAS);
	}

	public static boolean isScopeItem(ItemStack stack) {
		return ScopeZoomHelper.isScopeItem(stack);
	}

	public static boolean isPlayerUsingCamera(Player player) {
		return player.isUsingItem() && isCameraItem(player.getUseItem());
	}

	public static boolean isPlayerUsingScopeItem(Player player) {
		return player.isUsingItem() && isScopeItem(player.getUseItem());
	}

	public static boolean isPlayerHoldingCamera(Player player) {
		return isCameraItem(player.getMainHandItem());
	}

	public static boolean isPlayerHoldingPhotoTakingCamera(Player player) {
		final ItemStack stack = player.getMainHandItem();
		return isCameraItem(stack) && !player.getCooldowns().isOnCooldown(stack) && CameraItem.isCapableOfTakingPhotos(stack);
	}
}
