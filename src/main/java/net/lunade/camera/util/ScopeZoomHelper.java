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
import net.minecraft.world.item.ItemUseAnimation;
import org.jspecify.annotations.Nullable;

public final class ScopeZoomHelper {
	public static final ScopeZoomConfig CAMERA_DEFAULTS = new ScopeZoomConfig(1.0F, 4.0F, 0.25F, 1.0F);
	public static final ScopeZoomConfig SPYGLASS_DEFAULTS = new ScopeZoomConfig(1.0F, 10.0F, 0.5F, 1.0F);
	public static final float MIN_SUPPORTED_ZOOM = 1.0F;
	public static final float MAX_SUPPORTED_ZOOM = 64.0F;

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
		final float stored = stack.getOrDefault(CameraPortDataComponents.CAMERA_ZOOM, config.defaultZoom());
		if (stored <= 0F) return config.defaultZoom();

		if (stored < config.minZoom() && config.minZoom() >= 1.0F) {
			return Mth.clamp(1.0F / stored, config.minZoom(), config.maxZoom());
		}

		return Mth.clamp(stored, config.minZoom(), config.maxZoom());
	}

	public static void setStoredZoom(ItemStack stack, float zoom) {
		final ScopeZoomConfig config = resolveZoomConfig(stack);
		stack.set(CameraPortDataComponents.CAMERA_ZOOM, Mth.clamp(zoom, config.minZoom(), config.maxZoom()));
	}

	public static float toFovModifier(float zoomFactor) {
		return 1.0F / Mth.clamp(zoomFactor, MIN_SUPPORTED_ZOOM, MAX_SUPPORTED_ZOOM);
	}

	private static ScopeZoomConfig resolveZoomConfig(ItemStack stack) {
		final ScopeZoomConfig config = getZoomConfig(stack);
		return config != null ? config : SPYGLASS_DEFAULTS;
	}

	private static @Nullable ScopeZoomConfig getZoomConfig(ItemStack stack) {
		final ScopeZoomConfig explicitConfig = stack.get(CameraPortDataComponents.SCOPE_ZOOM_CONFIG);
		if (explicitConfig != null) return sanitize(explicitConfig);
		if (stack.getUseAnimation() == ItemUseAnimation.SPYGLASS) return SPYGLASS_DEFAULTS;
		return null;
	}

	private static ScopeZoomConfig sanitize(ScopeZoomConfig config) {
		if (config.maxZoom() <= 1.0F && config.minZoom() > 0F) {
			return sanitizeLegacyMultiplierConfig(config);
		}

		final float min = Mth.clamp(config.minZoom(), MIN_SUPPORTED_ZOOM, MAX_SUPPORTED_ZOOM);
		final float max = Mth.clamp(config.maxZoom(), min, MAX_SUPPORTED_ZOOM);
		final float span = Math.max(max - min, 0.001F);
		final float increment = Mth.clamp(config.zoomIncrement(), 0.001F, span);
		final float def = Mth.clamp(config.defaultZoom(), min, max);
		return new ScopeZoomConfig(min, max, increment, def);
	}

	private static ScopeZoomConfig sanitizeLegacyMultiplierConfig(ScopeZoomConfig config) {
		final float legacyMinMultiplier = Mth.clamp(Math.min(config.minZoom(), config.maxZoom()), 0.01F, 1.0F);
		final float legacyMaxMultiplier = Mth.clamp(Math.max(config.minZoom(), config.maxZoom()), legacyMinMultiplier, 1.0F);

		final float min = Mth.clamp(1.0F / legacyMaxMultiplier, MIN_SUPPORTED_ZOOM, MAX_SUPPORTED_ZOOM);
		final float max = Mth.clamp(1.0F / legacyMinMultiplier, min, MAX_SUPPORTED_ZOOM);
		final float span = Math.max(max - min, 0.001F);

		final float defaultMultiplier = Mth.clamp(config.defaultZoom(), legacyMinMultiplier, legacyMaxMultiplier);
		final float def = Mth.clamp(1.0F / defaultMultiplier, min, max);
		final float increment = Mth.clamp(span / 20.0F, 0.05F, span);

		return new ScopeZoomConfig(min, max, increment, def);
	}
}
