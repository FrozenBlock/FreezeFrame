package net.lunade.camera.registry;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.WritablePortfolioContent;
import net.lunade.camera.item.CameraItem;
import net.lunade.camera.item.PhotographItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
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

	public static final DataComponentType<PhotographComponent> PHOTO_COMPONENT = register(
		"photograph_component",
		builder -> builder.persistent(PhotographComponent.CODEC).networkSynchronized(PhotographComponent.STREAM_CODEC)
	);

	public static final DataComponentType<WritablePortfolioContent> WRITABLE_PORTFOLIO_CONTENT = register(
		"writable_portfolio_content",
		builder -> builder.persistent(WritablePortfolioContent.CODEC).networkSynchronized(WritablePortfolioContent.STREAM_CODEC)
	);

	// public static final PortfolioItem PORTFOLIO = new PortfolioItem(new Item.Properties().stacksTo(1).component(WRITABLE_PORTFOLIO_CONTENT, WritablePortfolioContent.EMPTY));


	public static void register() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((entries) -> entries.insertAfter(Items.LODESTONE, DISC_CAMERA));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((entries) -> entries.insertAfter(Items.LODESTONE, CAMERA));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((entries) -> entries.insertAfter(Items.LODESTONE, PHOTOGRAPH));
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
		return (T) Items.registerItem(ResourceKey.create(Registries.ITEM, CameraPortConstants.id(name)), function, properties);
	}

	private static <T> DataComponentType<T> register(String id, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, CameraPortConstants.id(id), unaryOperator.apply(DataComponentType.builder()).build());
	}
}
