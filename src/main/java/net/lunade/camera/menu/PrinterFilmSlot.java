package net.lunade.camera.menu;

import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PrinterFilmSlot extends Slot {

	public PrinterFilmSlot(Container container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.is(CameraPortItems.CAMERA);
	}
}
