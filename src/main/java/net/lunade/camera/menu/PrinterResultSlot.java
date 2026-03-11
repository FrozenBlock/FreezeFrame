package net.lunade.camera.menu;

import net.lunade.camera.registry.CameraPortSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PrinterResultSlot extends Slot {
	private final PrinterMenu menu;

	public PrinterResultSlot(PrinterMenu menu, Container container, int slot, int x, int y) {
		super(container, slot, x, y);
		this.menu = menu;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		stack.onCraftedBy(player, stack.getCount());
		final ItemStack input = this.menu.paperSlot.remove(1);
		if (!input.isEmpty()) this.menu.setupResultSlot();

		this.menu.access.execute((level, pos) -> {
			final long gameTime = level.getGameTime();
			if (this.menu.lastSoundTime == gameTime) return;

			level.playSound(null, pos, CameraPortSounds.CAMERA_SNAP, SoundSource.BLOCKS, 1F, 1F);
			this.menu.lastSoundTime = gameTime;
		});

		super.onTake(player, stack);
	}
}
