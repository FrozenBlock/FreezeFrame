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

package net.lunade.camera.util.client;

import net.lunade.camera.util.ScopeZoomHelper;
import net.minecraft.util.Mth;

public final class ScopeZoomManager {
	public static final float DEFAULT_FOV = 70F;
	public static final float BASE_ZOOM = 1F;
	public static final float MIN_ZOOM = ScopeZoomHelper.MIN_SUPPORTED_ZOOM;
	public static final float MAX_ZOOM = ScopeZoomHelper.MAX_SUPPORTED_ZOOM;
	private static final float DEFAULT_ZOOM_STEP = ScopeZoomHelper.SPYGLASS_DEFAULTS.zoomIncrement();

	private static float zoom = BASE_ZOOM;
	private static float activeMinZoom = MIN_ZOOM;
	private static float activeMaxZoom = MAX_ZOOM;
	private static float activeZoomStep = DEFAULT_ZOOM_STEP;
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

	public static void setActiveRange(float minZoom, float maxZoom) {
		activeMinZoom = Mth.clamp(minZoom, MIN_ZOOM, MAX_ZOOM);
		activeMaxZoom = Mth.clamp(maxZoom, MIN_ZOOM, MAX_ZOOM);
		if (activeMinZoom > activeMaxZoom) {
			final float previousMin = activeMinZoom;
			activeMinZoom = activeMaxZoom;
			activeMaxZoom = previousMin;
		}
		zoom = Mth.clamp(zoom, activeMinZoom, activeMaxZoom);
	}

	public static void resetActiveRange() {
		activeMinZoom = MIN_ZOOM;
		activeMaxZoom = MAX_ZOOM;
		activeZoomStep = DEFAULT_ZOOM_STEP;
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

	public static boolean adjustZoom(int wheelDirection) {
		if (wheelDirection == 0) return false;

		final float previousZoom = zoom;
		if (wheelDirection > 0) {
			zoom = Mth.clamp(zoom - activeZoomStep, activeMinZoom, activeMaxZoom);
		} else {
			zoom = Mth.clamp(zoom + activeZoomStep, activeMinZoom, activeMaxZoom);
		}

		return previousZoom != zoom;
	}
}
