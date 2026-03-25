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

package net.frozenblock.freezeframe.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FreezeFrameClient;
import net.frozenblock.freezeframe.component.ScopeZoomConfig;
import net.frozenblock.freezeframe.networking.packet.ChangeScopeZoomPacket;
import net.frozenblock.freezeframe.networking.packet.QuickCameraPhotographPacket;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.frozenblock.freezeframe.util.client.ScopeZoomManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class ScopeAndCameraUseController {
	private static boolean wasPlayerNull;
	private static boolean wasAttackDown = false;
	private static boolean wasResetZoomDown = false;
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
			//ensureFirstPerson(minecraft);
		} else {
			restoreCameraType(minecraft);
		}

		final ItemStack scopeItem = usingScopeItem ? player.getUseItem() : ItemStack.EMPTY;
		final boolean isScopeConfigDifferent = !previousScopeItem.getOrDefault(FFDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomConfig.EMPTY)
				.equals(scopeItem.getOrDefault(FFDataComponents.SCOPE_ZOOM_CONFIG, ScopeZoomConfig.EMPTY));
		if (!usingScopeItem || isScopeConfigDifferent) {
			ScopeZoomManager.resetActiveZoomProfile();
			ScopeZoomManager.resetZoom();
		}

		if (usingScopeItem && isScopeConfigDifferent) applyZoomProfile(scopeItem);

		final boolean resetZoomDown = FreezeFrameClient.RESET_SCOPE_ZOOM.isDown();
		if (resetZoomDown && !wasResetZoomDown) ScopeZoomManager.setZoomToDefault(minecraft, player);

		final boolean attackDown = minecraft.options.keyAttack.isDown();
		if (attackDown && !wasAttackDown && minecraft.gameMode != null && holdingCamera) {
			if (ScopeItemHelper.isPlayerUsingCamera(player)) {
				ClientPlayNetworking.send(new ChangeScopeZoomPacket(player.getUsedItemHand(), ScopeZoomManager.getZoom()));
				minecraft.gameMode.useItem(player, player.getUsedItemHand());
			} else {
				ClientPlayNetworking.send(new QuickCameraPhotographPacket());
			}
		}

		wasAttackDown = attackDown;
		wasResetZoomDown = resetZoomDown;
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

	private static void applyZoomProfile(ItemStack stack) {
		ScopeZoomManager.setActiveZoomProfile(
			ScopeZoomHelper.getMinZoomFor(stack),
			ScopeZoomHelper.getMaxZoomFor(stack),
			ScopeZoomHelper.getDefaultZoomFor(stack),
			ScopeZoomHelper.getZoomInSoundFor(stack),
			ScopeZoomHelper.getZoomOutSoundFor(stack)
		);
		ScopeZoomManager.setActiveZoomStep(ScopeZoomHelper.getZoomIncrementFor(stack));
		ScopeZoomManager.setZoom(ScopeZoomHelper.getStoredZoom(stack));
	}
}
