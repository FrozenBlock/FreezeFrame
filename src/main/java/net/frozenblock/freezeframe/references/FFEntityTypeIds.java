package net.frozenblock.freezeframe.references;

import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

public final class FFEntityTypeIds {
	public static final ResourceKey<EntityType<?>> CAMERA = create("camera");
	public static final ResourceKey<EntityType<?>> DISC_CAMERA = create("disc_camera");

	private static ResourceKey<EntityType<?>> create(String name) {
		return ResourceKey.create(Registries.ENTITY_TYPE, FFConstants.id(name));
	}
}
