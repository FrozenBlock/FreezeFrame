package net.lunade.camera.mixin.camera;

import net.lunade.camera.item.CameraItem;
import net.lunade.camera.menu.impl.ContainerMenuCameraInterface;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements ContainerMenuCameraInterface {

	@Shadow
	@Final
	public NonNullList<Slot> slots;

	@Unique
	@Override
	public void cameraPort$setSelectedCameraFilmIndex(int slotIndex, int selectedFilmIndex) {
		if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
		final ItemStack stack = this.slots.get(slotIndex).getItem();
		CameraItem.toggleSelectedItem(stack, selectedFilmIndex);
	}
}
