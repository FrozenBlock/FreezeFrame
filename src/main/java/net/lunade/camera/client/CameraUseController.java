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
import net.lunade.camera.networking.packet.QuickCameraPhotographPacket;
import net.lunade.camera.util.ScopeItemHelper;
import net.lunade.camera.util.ScopeZoomHelper;
import net.lunade.camera.util.client.CameraZoomManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class CameraUseController {
	private static boolean wasAttackDown = false;
	private static boolean wasUsingCamera = false;
	private static boolean forcedFirstPerson = false;
	private static CameraType previousCameraType = CameraType.FIRST_PERSON;

	private CameraUseController() {
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(CameraUseController::tick);
	}

	private static void tick(Minecraft minecraft) {
		final LocalPlayer player = minecraft.player;
		if (player == null) {
			resetState(minecraft);
			return;
		}

		final boolean usingScopeItem = ScopeItemHelper.isPlayerUsingScopeItem(player);
		final boolean holdingCamera = ScopeItemHelper.isPlayerHoldingCamera(player);
		if (usingScopeItem) {
			ensureFirstPerson(minecraft);
		} else {
			restoreCameraType(minecraft);
		}

		if (!usingScopeItem && !holdingCamera) {
			CameraZoomManager.resetActiveRange();
			wasAttackDown = minecraft.options.keyAttack.isDown();
			wasUsingCamera = false;
			return;
		}

		if (usingScopeItem && !wasUsingCamera) {
			final ItemStack useItem = player.getUseItem();
			applyZoomProfile(useItem, true);
		}
		if (!usingScopeItem && holdingCamera) {
			final ItemStack cameraStack = player.getMainHandItem();
			applyZoomProfile(cameraStack, false);
		}

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
		wasUsingCamera = usingScopeItem;
	}

	private static void ensureFirstPerson(Minecraft minecraft) {
		if (!forcedFirstPerson) {
			previousCameraType = minecraft.options.getCameraType();
			forcedFirstPerson = true;
		}
		if (!minecraft.options.getCameraType().isFirstPerson()) {
			minecraft.options.setCameraType(CameraType.FIRST_PERSON);
		}
	}

	private static void restoreCameraType(Minecraft minecraft) {
		if (!forcedFirstPerson) return;
		minecraft.options.setCameraType(previousCameraType);
		forcedFirstPerson = false;
	}

	private static void resetState(Minecraft minecraft) {
		restoreCameraType(minecraft);
		CameraZoomManager.resetActiveRange();
		wasAttackDown = false;
		wasUsingCamera = false;
	}

	private static void applyZoomProfile(ItemStack stack, boolean applyStoredZoom) {
		CameraZoomManager.setActiveRange(ScopeZoomHelper.getMinZoomFor(stack), ScopeZoomHelper.getMaxZoomFor(stack));
		CameraZoomManager.setActiveZoomStep(ScopeZoomHelper.getZoomIncrementFor(stack));
		if (applyStoredZoom) {
			CameraZoomManager.setZoom(ScopeZoomHelper.getStoredZoom(stack));
		}
	}
}
