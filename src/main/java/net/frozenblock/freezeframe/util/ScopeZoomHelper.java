/*
 * Copyright 2026 FrozenBlock
 * This file is part of Freeze Frame.
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

package net.frozenblock.freezeframe.util;

import java.util.Optional;
import net.frozenblock.freezeframe.component.ScopeZoomConfig;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFSounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScopeZoomHelper {
	public static final ScopeZoomConfig CAMERA_DEFAULTS = new ScopeZoomConfig(
		0.75F,
		3F,
		0.25F,
		1F,
		false,
		Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(FFSounds.CAMERA_ZOOM_INCREASE)),
		Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(FFSounds.CAMERA_ZOOM_DECREASE))
	);
	public static final ScopeZoomConfig SPYGLASS_DEFAULTS = new ScopeZoomConfig(
		1F,
		10F,
		0.5F,
		10F,
		true,
		Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(FFSounds.SPYGLASS_ZOOM_INCREASE)),
		Optional.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(FFSounds.SPYGLASS_ZOOM_DECREASE))
	);
	public static final float MIN_SUPPORTED_ZOOM = 0.75F;
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

	public static Optional<Holder<SoundEvent>> getZoomInSoundFor(ItemStack stack) {
		return resolveZoomConfig(stack).zoomInSound();
	}

	public static Optional<Holder<SoundEvent>> getZoomOutSoundFor(ItemStack stack) {
		return resolveZoomConfig(stack).zoomOutSound();
	}

	public static float getStoredZoom(ItemStack stack) {
		final ScopeZoomConfig config = resolveZoomConfig(stack);
		return stack.getOrDefault(FFDataComponents.SCOPE_ZOOM, config.defaultZoom());
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
		final ScopeZoomConfig explicitConfig = stack.get(FFDataComponents.SCOPE_ZOOM_CONFIG);
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
		return new ScopeZoomConfig(min, max, increment, def, config.offhandEnabled(), config.zoomInSound(), config.zoomOutSound());
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

		return new ScopeZoomConfig(min, max, increment, def, config.offhandEnabled(), config.zoomInSound(), config.zoomOutSound());
	}
}
