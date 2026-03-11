package net.lunade.camera.menu;

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
		return stack.is(CameraPortItems.CAMERA)
			|| (stack.is(CameraPortItems.PHOTOGRAPH) && stack.has(CameraPortItems.PHOTO_COMPONENT) && !stack.get(CameraPortItems.PHOTO_COMPONENT).isCopy());
	}
}
