package net.lunade.camera.tag;

import net.lunade.camera.CameraPortConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class CameraPortItemTags {

	private CameraPortItemTags() {
		throw new UnsupportedOperationException("CameraPortItemTags contains only static declarations.");
	}

	private static TagKey<Item> bind(String path) {
		return TagKey.create(Registries.ITEM, CameraPortConstants.id(path));
	}
}
