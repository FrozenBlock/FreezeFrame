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

import net.lunade.camera.component.ScopeZoomConfig;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScopeZoomHelper {
	public static final ScopeZoomConfig CAMERA_DEFAULTS = new ScopeZoomConfig(1F, 4F, 0.25F, 1F, false);
	public static final ScopeZoomConfig SPYGLASS_DEFAULTS = new ScopeZoomConfig(1F, 10F, 0.5F, 1F, true);
	public static final float MIN_SUPPORTED_ZOOM = 1F;
	public static final float MAX_SUPPORTED_ZOOM = 64F;

	private ScopeZoomHelper() {
	}

	public static boolean isScopeItem(ItemStack stack) {
		return getZoomConfig(stack) != null;
	}

	public static float getMinZoomFor(ItemStack stack) {
		return resolveZoomConfig(stack).minZoom();
	}

	public static float getMaxZoomFor(ItemStack stack) {
		return resolveZoomConfig(stack).maxZoom();
	}

	public static float getZoomIncrementFor(ItemStack stack) {
		return resolveZoomConfig(stack).zoomIncrement();
	}

	public static float getDefaultZoomFor(ItemStack stack) {
		return resolveZoomConfig(stack).defaultZoom();
	}

	public static float getStoredZoom(ItemStack stack) {
		final ScopeZoomConfig config = resolveZoomConfig(stack);
		final float stored = stack.getOrDefault(CameraPortDataComponents.SCOPE_ZOOM, config.defaultZoom());
		if (stored <= 0F) return config.defaultZoom();

		if (stored < config.minZoom() && config.minZoom() >= 1F) {
			return Mth.clamp(1F / stored, config.minZoom(), config.maxZoom());
		}

		return Mth.clamp(stored, config.minZoom(), config.maxZoom());
	}

	public static void setStoredZoom(ItemStack stack, float zoom) {
		final ScopeZoomConfig config = resolveZoomConfig(stack);
		stack.set(CameraPortDataComponents.SCOPE_ZOOM, Mth.clamp(zoom, config.minZoom(), config.maxZoom()));
	}

	public static float toFovModifier(float zoomFactor) {
		return 1F / Mth.clamp(zoomFactor, MIN_SUPPORTED_ZOOM, MAX_SUPPORTED_ZOOM);
	}

	private static ScopeZoomConfig resolveZoomConfig(ItemStack stack) {
		final ScopeZoomConfig config = getZoomConfig(stack);
		return config != null ? config : SPYGLASS_DEFAULTS;
	}

	@Nullable
	private static ScopeZoomConfig getZoomConfig(ItemStack stack) {
		final ScopeZoomConfig explicitConfig = stack.get(CameraPortDataComponents.SCOPE_ZOOM_CONFIG);
		if (explicitConfig != null) return sanitize(explicitConfig);
		return null;
	}

	private static ScopeZoomConfig sanitize(ScopeZoomConfig config) {
		if (config.maxZoom() <= 1F && config.minZoom() > 0F) return sanitizeLegacyMultiplierConfig(config);

		final float min = Mth.clamp(config.minZoom(), MIN_SUPPORTED_ZOOM, MAX_SUPPORTED_ZOOM);
		final float max = Mth.clamp(config.maxZoom(), min, MAX_SUPPORTED_ZOOM);
		final float span = Math.max(max - min, 0.001F);
		final float increment = Mth.clamp(config.zoomIncrement(), 0.001F, span);
		final float def = Mth.clamp(config.defaultZoom(), min, max);
		return new ScopeZoomConfig(min, max, increment, def, config.offhandEnabled());
	}

	private static ScopeZoomConfig sanitizeLegacyMultiplierConfig(ScopeZoomConfig config) {
		final float legacyMinMultiplier = Mth.clamp(Math.min(config.minZoom(), config.maxZoom()), 0.01F, 1F);
		final float legacyMaxMultiplier = Mth.clamp(Math.max(config.minZoom(), config.maxZoom()), legacyMinMultiplier, 1F);

		final float min = Mth.clamp(1F / legacyMaxMultiplier, MIN_SUPPORTED_ZOOM, MAX_SUPPORTED_ZOOM);
		final float max = Mth.clamp(1F / legacyMinMultiplier, min, MAX_SUPPORTED_ZOOM);
		final float span = Math.max(max - min, 0.001F);

		final float defaultMultiplier = Mth.clamp(config.defaultZoom(), legacyMinMultiplier, legacyMaxMultiplier);
		final float def = Mth.clamp(1F / defaultMultiplier, min, max);
		final float increment = Mth.clamp(span / 20F, 0.05F, span);

		return new ScopeZoomConfig(min, max, increment, def, config.offhandEnabled());
	}
}
