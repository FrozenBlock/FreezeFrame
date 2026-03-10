package net.lunade.camera.tag;


import net.lunade.camera.CameraPortConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class CameraPortEntityTags {
	public static final TagKey<EntityType<?>> CAMERAS = bind("cameras");

	private CameraPortEntityTags() {
		throw new UnsupportedOperationException("CameraPortEntityTags contains only static declarations.");
	}

	private static TagKey<EntityType<?>> bind(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, CameraPortConstants.id(path));
	}
}
