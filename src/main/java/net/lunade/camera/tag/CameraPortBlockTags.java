package net.lunade.camera.tag;

import net.lunade.camera.CameraPortConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public final class CameraPortBlockTags {

	private CameraPortBlockTags() {
		throw new UnsupportedOperationException("CameraPortBlockTags contains only static declarations.");
	}


	private static TagKey<Block> bind(String path) {
		return TagKey.create(Registries.BLOCK, CameraPortConstants.id(path));
	}
}
