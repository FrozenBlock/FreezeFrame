package net.lunade.camera.menu;

import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PrinterSourceSlot extends Slot {

	public PrinterSourceSlot(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return isValidAsSource(stack);
	}

	public static boolean isValidAsSource(ItemStack stack) {
		return isValidFilmForPrinting(stack) || isValidPhotographForCopying(stack);
	}

	public static boolean isValidFilmForPrinting(ItemStack stack) {
		return stack.is(CameraPortItems.CAMERA);
	}

	public static boolean isValidPhotographForCopying(ItemStack stack) {
		return stack.is(CameraPortItems.PHOTOGRAPH) && stack.has(CameraPortDataComponents.PHOTOGRAPH) && !stack.get(CameraPortDataComponents.PHOTOGRAPH).isCopy();
	}
}
