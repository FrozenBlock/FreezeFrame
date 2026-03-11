package net.lunade.camera.registry;

import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.item.CameraItem;
import net.lunade.camera.item.PhotographItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class CameraPortItems {
	public static final CameraItem CAMERA = register(
		"camera",
		CameraItem::new,
		new Item.Properties()
			.stacksTo(1)
			.spawnEgg(CameraPortEntityTypes.CAMERA)
	);
	public static final CameraItem DISC_CAMERA = register(
		"disc_camera",
		CameraItem::new,
		new Item.Properties()
			.stacksTo(1)
			.spawnEgg(CameraPortEntityTypes.DISC_CAMERA)
	);
	public static final PhotographItem PHOTOGRAPH = register(
		"photograph",
		PhotographItem::new,
		new Item.Properties().stacksTo(1)
	);

	// public static final PortfolioItem PORTFOLIO = new PortfolioItem(new Item.Properties().stacksTo(1).component(WRITABLE_PORTFOLIO_CONTENT, WritablePortfolioContent.EMPTY));

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((entries) -> entries.insertAfter(Items.LODESTONE, DISC_CAMERA));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((entries) -> entries.insertAfter(Items.LODESTONE, CAMERA));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((entries) -> entries.insertAfter(Items.LODESTONE, PHOTOGRAPH));
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
		return (T) Items.registerItem(ResourceKey.create(Registries.ITEM, CameraPortConstants.id(name)), function, properties);
	}
}
