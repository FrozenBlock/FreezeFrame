package net.frozenblock.freezeframe.references;

import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class FFItemIds {
	public static final ResourceKey<Item> CAMERA = create("camera");
	public static final ResourceKey<Item> DISC_CAMERA = create("disc_camera");
	public static final ResourceKey<Item> FILM = create("film");
	public static final ResourceKey<Item> PHOTOGRAPH = create("photograph");

	private static ResourceKey<Item> create(String name) {
		return ResourceKey.create(Registries.ITEM, FFConstants.id(name));
	}
}
