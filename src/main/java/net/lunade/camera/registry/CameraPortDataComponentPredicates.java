package net.lunade.camera.registry;

import com.mojang.serialization.Codec;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.predicates.CameraPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class CameraPortDataComponentPredicates {
	public static final DataComponentPredicate.Type<CameraPredicate> CAMERA_CONTENTS = register("camera_contents", CameraPredicate.CODEC);

	public static void init() {
	}

	private static <T extends DataComponentPredicate> DataComponentPredicate.Type<T> register(String id, Codec<T> codec) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, CameraPortConstants.id(id), new DataComponentPredicate.ConcreteType<>(codec));
	}
}
