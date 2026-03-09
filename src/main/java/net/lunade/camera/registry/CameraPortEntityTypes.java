package net.lunade.camera.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.entity.CameraEntity;
import net.lunade.camera.entity.DiscCameraEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class CameraPortEntityTypes {
	public static final EntityType<CameraEntity> CAMERA = register(
		"camera",
		EntityType.Builder.of(CameraEntity::new, MobCategory.MISC)
			.sized(0.6F, 1.75F)
			.eyeHeight(1.619999999999999F)
			.clientTrackingRange(8)
	);

	public static final EntityType<DiscCameraEntity> DISC_CAMERA = register(
		"disc_camera",
		EntityType.Builder.of(DiscCameraEntity::new, MobCategory.MISC)
			.sized(0.55F, 0.9F)
			.eyeHeight(0.81F)
			.clientTrackingRange(8)
	);

	public static void init() {
	}

	static {
		FabricDefaultAttributeRegistry.register(CAMERA, CameraEntity.addAttributes());
		FabricDefaultAttributeRegistry.register(DISC_CAMERA, DiscCameraEntity.addAttributes());
	}

	private static <T extends Entity> EntityType<T> register(String string, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> resourceKey = ResourceKey.create(Registries.ENTITY_TYPE, CameraPortConstants.id(string));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.build(resourceKey));
	}
}
