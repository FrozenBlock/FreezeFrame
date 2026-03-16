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

package net.lunade.camera.registry;

import net.lunade.camera.CameraPortConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class CameraPortSounds {
	public static final SoundEvent CAMERA_SNAP = register("item.camera.snap");
	public static final SoundEvent CAMERA_SNAP_FAIL = register("item.camera.snap_fail");
	public static final SoundEvent CAMERA_INSERT = register("item.camera.insert");
	public static final SoundEvent CAMERA_INSERT_FAIL = register("item.camera.insert_fail");
	public static final SoundEvent CAMERA_REMOVE_ONE = register("item.camera.remove_one");
	public static final SoundEvent CAMERA_REMOVE_ONE_FAIL = register("item.camera.remove_one_fail");

	public static final SoundEvent CAMERA_BREAK = register("entity.camera.break");
	public static final SoundEvent CAMERA_FALL = register("entity.camera.fall");
	public static final SoundEvent CAMERA_HIT = register("entity.camera.hit");
	public static final SoundEvent CAMERA_PLACE = register("entity.camera.place");
	public static final SoundEvent CAMERA_PRIME = register("entity.camera.prime");
	public static final SoundEvent CAMERA_ADJUST = register("entity.camera.adjust");

	private static Holder.Reference<SoundEvent> registerForHolder(String path) {
		return registerForHolder(CameraPortConstants.id(path));
	}

	private static Holder.Reference<SoundEvent> registerForHolder(Identifier id) {
		return registerForHolder(id, id);
	}

	public static SoundEvent register(String path) {
		final Identifier id = CameraPortConstants.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	private static Holder.Reference<SoundEvent> registerForHolder(Identifier id, Identifier id2) {
		return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id2));
	}

	public static void init() {}
}
