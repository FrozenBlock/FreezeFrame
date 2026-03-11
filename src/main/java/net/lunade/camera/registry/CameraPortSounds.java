package net.lunade.camera.registry;

import net.lunade.camera.CameraPortConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class CameraPortSounds {
	public static final SoundEvent CAMERA_BREAK = register("entity.camera.break");
	public static final SoundEvent CAMERA_FALL = register("entity.camera.fall");
	public static final SoundEvent CAMERA_HIT = register("entity.camera.hit");
	public static final SoundEvent CAMERA_PLACE = register("entity.camera.place");
	public static final SoundEvent CAMERA_PRIME = register("entity.camera.prime");
	public static final SoundEvent CAMERA_SNAP = register("entity.camera.snap");
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
