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

package net.frozenblock.freezeframe.util.client;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.networking.packet.ChangeScopeZoomPacket;
import net.frozenblock.freezeframe.util.ScopeZoomHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public final class ScopeZoomManager {
	public static final float DEFAULT_FOV = 70F;
	public static final float BASE_ZOOM = 1F;
	public static final float MIN_ZOOM = ScopeZoomHelper.MIN_SUPPORTED_ZOOM;
	public static final float MAX_ZOOM = ScopeZoomHelper.MAX_SUPPORTED_ZOOM;
	private static final float DEFAULT_ZOOM_STEP = ScopeZoomHelper.SPYGLASS_DEFAULTS.zoomIncrement();

	private static float zoom = BASE_ZOOM;
	private static float activeMinZoom = MIN_ZOOM;
	private static float activeMaxZoom = MAX_ZOOM;
	private static float activeDefaultZoom = BASE_ZOOM;
	private static float activeZoomStep = DEFAULT_ZOOM_STEP;
	private static Optional<Holder<SoundEvent>> activeZoomInSound = Optional.empty();
	private static Optional<Holder<SoundEvent>> activeZoomOutSound = Optional.empty();
	private static boolean hasForcedZoom = false;
	private static float forcedZoom = BASE_ZOOM;

	private ScopeZoomManager() {
	}

	public static float getZoom() {
		return hasForcedZoom ? forcedZoom : zoom;
	}

	public static void resetZoom() {
		zoom = BASE_ZOOM;
	}

	public static void setActiveZoomProfile(float minZoom, float maxZoom, float defaultZoom, Optional<Holder<SoundEvent>> zoomInSound, Optional<Holder<SoundEvent>> zoomOutSound) {
		activeMinZoom = Mth.clamp(minZoom, MIN_ZOOM, MAX_ZOOM);
		activeMaxZoom = Mth.clamp(maxZoom, MIN_ZOOM, MAX_ZOOM);
		activeDefaultZoom =  Mth.clamp(defaultZoom, MIN_ZOOM, MAX_ZOOM);
		if (activeMinZoom > activeMaxZoom) {
			final float previousMin = activeMinZoom;
			activeMinZoom = activeMaxZoom;
			activeMaxZoom = previousMin;
		}
		zoom = Mth.clamp(zoom, activeMinZoom, activeMaxZoom);
		activeZoomInSound = zoomInSound;
		activeZoomOutSound = zoomOutSound;
	}

	public static void resetActiveZoomProfile() {
		activeMinZoom = MIN_ZOOM;
		activeMaxZoom = MAX_ZOOM;
		activeZoomStep = DEFAULT_ZOOM_STEP;
		activeDefaultZoom = BASE_ZOOM;
		activeZoomInSound = Optional.empty();
		activeZoomOutSound = Optional.empty();
	}

	public static void setActiveZoomStep(float value) {
		activeZoomStep = Mth.clamp(value, 0.001F, activeMaxZoom - activeMinZoom);
	}

	public static void setZoom(float value) {
		zoom = Mth.clamp(value, activeMinZoom, activeMaxZoom);
	}

	public static void pushForcedZoom(float value) {
		forcedZoom = Mth.clamp(value, MIN_ZOOM, MAX_ZOOM);
		hasForcedZoom = true;
	}

	public static void clearForcedZoom() {
		hasForcedZoom = false;
	}

	public static boolean adjustZoom(Minecraft minecraft, Player player, int wheelDirection) {
		if (wheelDirection == 0) return false;

		final float initalZoom = zoom;
		final boolean increase = wheelDirection < 0;
		if (!increase) {
			zoom = Mth.clamp(zoom - activeZoomStep, activeMinZoom, activeMaxZoom);
		} else {
			zoom = Mth.clamp(zoom + activeZoomStep, activeMinZoom, activeMaxZoom);
		}

		if (initalZoom != zoom) {
			final float zoomProgress = zoom / activeMaxZoom;
			final Optional<Holder<SoundEvent>> zoomSound = increase ? activeZoomInSound : activeZoomOutSound;
			zoomSound.ifPresent(soundEventHolder -> minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEventHolder.value(), 0.9F + zoomProgress)));
			ClientPlayNetworking.send(new ChangeScopeZoomPacket(player.getUsedItemHand(), zoom));
		}

		return initalZoom != zoom;
	}

	public static boolean setZoomToDefault(Minecraft minecraft, Player player) {
		final float initalZoom = zoom;
		final boolean increase = initalZoom < activeDefaultZoom;
		zoom = activeDefaultZoom;

		if (initalZoom != zoom) {
			final float zoomProgress = zoom / activeMaxZoom;
			final Optional<Holder<SoundEvent>> zoomSound = increase ? activeZoomInSound : activeZoomOutSound;
			zoomSound.ifPresent(soundEventHolder -> minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEventHolder.value(), 0.9F + zoomProgress)));
			ClientPlayNetworking.send(new ChangeScopeZoomPacket(player.getUsedItemHand(), zoom));
		}

		return initalZoom != zoom;
	}
}
