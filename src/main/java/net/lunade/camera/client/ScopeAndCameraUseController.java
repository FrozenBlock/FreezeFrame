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

package net.lunade.camera.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.lunade.camera.component.ScopeZoomConfig;
import net.lunade.camera.networking.packet.QuickCameraPhotographPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.ScopeZoomHelper;
import net.lunade.camera.util.client.ScopeZoomManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class ScopeAndCameraUseController {
	private static boolean wasPlayerNull;
	private static boolean wasAttackDown = false;
	private static ItemStack previousScopeItem = ItemStack.EMPTY;
	private static boolean forcedFirstPerson = false;
	private static CameraType previousCameraType = CameraType.FIRST_PERSON;

	private ScopeAndCameraUseController() {
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(ScopeAndCameraUseController::tick);
	}

	private static void tick(Minecraft minecraft) {
		final LocalPlayer player = minecraft.player;
		if (player == null) {
			if (!wasPlayerNull) resetState(minecraft);
			wasPlayerNull = true;
			return;
		}

		wasPlayerNull = false;
		final boolean usingScopeItem = ScopeItemHelper.isPlayerUsingScopeItem(player);
		final boolean holdingCamera = ScopeItemHelper.isPlayerHoldingCamera(player);
		if (usingScopeItem) {
			ensureFirstPerson(minecraft);
		} else {
			restoreCameraType(minecraft);
		}

		final ItemStack scopeItem = usingScopeItem ? player.getUseItem() : ItemStack.EMPTY;
		final boolean isScopeConfigDifferent = !previousScopeItem.getOrDefault(CameraPortDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomConfig.EMPTY)
				.equals(scopeItem.getOrDefault(CameraPortDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomConfig.EMPTY));
		final boolean isScopeDataDifferent = !Objects.equals(previousScopeItem.get(CameraPortDataComponents.SCOPE_ZOOM_DATA), scopeItem.get(CameraPortDataComponents.SCOPE_ZOOM_DATA));
		if (!usingScopeItem || isScopeConfigDifferent) {
			ScopeZoomManager.resetActiveZoomProfile();
			ScopeZoomManager.resetZoom();
		}

		if (usingScopeItem && (isScopeConfigDifferent || isScopeDataDifferent)) applyZoomProfile(player, scopeItem);

		final boolean attackDown = minecraft.options.keyAttack.isDown();
		if (attackDown && !wasAttackDown && holdingCamera) {
			final MultiPlayerGameMode gameMode = minecraft.gameMode;
			if (gameMode != null) {
				if (ScopeItemHelper.isPlayerUsingCamera(player)) {
					gameMode.useItem(player, player.getUsedItemHand());
				} else {
					ClientPlayNetworking.send(new QuickCameraPhotographPacket());
				}
			}
		}

		wasAttackDown = attackDown;
		previousScopeItem = scopeItem;
	}

	private static void ensureFirstPerson(Minecraft minecraft) {
		if (!forcedFirstPerson) {
			previousCameraType = minecraft.options.getCameraType();
			forcedFirstPerson = true;
		}
		minecraft.options.setCameraType(CameraType.FIRST_PERSON);
	}

	private static void restoreCameraType(Minecraft minecraft) {
		if (!forcedFirstPerson) return;
		minecraft.options.setCameraType(previousCameraType);
		forcedFirstPerson = false;
	}

	private static void resetState(Minecraft minecraft) {
		restoreCameraType(minecraft);
		ScopeZoomManager.resetActiveZoomProfile();
		wasAttackDown = false;
		previousScopeItem = ItemStack.EMPTY;
	}

	private static void applyZoomProfile(Player player, ItemStack stack) {
		ScopeZoomManager.setActiveZoomProfile(
			ScopeZoomHelper.getMinZoomFor(stack),
			ScopeZoomHelper.getMaxZoomFor(stack),
			ScopeZoomHelper.getZoomInSoundFor(stack),
			ScopeZoomHelper.getZoomOutSoundFor(stack)
		);
		ScopeZoomManager.setActiveZoomStep(ScopeZoomHelper.getZoomIncrementFor(stack));
		ScopeZoomManager.setZoom(ScopeZoomHelper.getStoredZoom(player, stack));
	}
}
