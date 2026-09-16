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

package net.frozenblock.freezeframe.registry;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.frozenblock.lib.platform.api.registry.DeferredSoundEvent;

public final class FFSounds {
	private static final DeferredRegister.SoundEvents REGISTER = DeferredRegister.createSoundEvents(FFConstants.MOD_ID);

	public static final DeferredSoundEvent CAMERA_SNAP = REGISTER.register("item.camera.snap");
	public static final DeferredSoundEvent CAMERA_SNAP_FAIL = REGISTER.register("item.camera.snap_fail");
	public static final DeferredSoundEvent CAMERA_INSERT = REGISTER.register("item.camera.insert");
	public static final DeferredSoundEvent CAMERA_INSERT_FAIL = REGISTER.register("item.camera.insert_fail");
	public static final DeferredSoundEvent CAMERA_REMOVE_ONE = REGISTER.register("item.camera.remove_one");
	public static final DeferredSoundEvent CAMERA_SCOPE_START = REGISTER.register("item.camera.scope_start");
	public static final DeferredSoundEvent CAMERA_SCOPE_END = REGISTER.register("item.camera.scope_end");
	public static final DeferredSoundEvent CAMERA_ZOOM_INCREASE = REGISTER.register("item.camera.zoom_increase");
	public static final DeferredSoundEvent CAMERA_ZOOM_DECREASE = REGISTER.register("item.camera.zoom_decrease");
	public static final DeferredSoundEvent SPYGLASS_ZOOM_INCREASE = REGISTER.register("item.spyglass.zoom_increase");
	public static final DeferredSoundEvent SPYGLASS_ZOOM_DECREASE = REGISTER.register("item.spyglass.zoom_decrease");

	public static final DeferredSoundEvent CAMERA_BREAK = REGISTER.register("entity.camera.break");
	public static final DeferredSoundEvent CAMERA_FALL = REGISTER.register("entity.camera.fall");
	public static final DeferredSoundEvent CAMERA_HIT = REGISTER.register("entity.camera.hit");
	public static final DeferredSoundEvent CAMERA_PLACE = REGISTER.register("entity.camera.place");
	public static final DeferredSoundEvent CAMERA_PRIME = REGISTER.register("entity.camera.prime");
	public static final DeferredSoundEvent CAMERA_ALREADY_PRIMED = REGISTER.register("entity.camera.already_primed");
	public static final DeferredSoundEvent CAMERA_PRIME_FAIL = REGISTER.register("entity.camera.prime_fail");
	public static final DeferredSoundEvent CAMERA_PRIME_FAIL_FULL = REGISTER.register("entity.camera.prime_fail_full");
	public static final DeferredSoundEvent CAMERA_ADJUST = REGISTER.register("entity.camera.adjust");

	public static final DeferredSoundEvent DEVELOPING_TABLE_TAKE_RESULT = REGISTER.register("ui.developing_table.take_result");

	public static final DeferredSoundEvent FILM_RENAME = REGISTER.register("ui.film.rename");
	public static final DeferredSoundEvent FILM_ROLL = REGISTER.register("ui.film.roll");
	public static final DeferredSoundEvent FILM_TEAR = REGISTER.register("ui.film.tear");
	public static final DeferredSoundEvent FILM_TEAR_FINISH = REGISTER.register("ui.film.tear_finish");

	static {
		REGISTER.register();
	}

	public static void init() {}

	private FFSounds() {}
}
