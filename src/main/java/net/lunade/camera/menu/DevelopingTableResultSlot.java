/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
 *
 * This program is free software; you can modify it under
 * the terms of version 1 of the FrozenBlock Modding Oasis License
 * as published by FrozenBlock Modding Oasis.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * FrozenBlock Modding Oasis License for more details.
 *
 * You should have received a copy of the FrozenBlock Modding Oasis License
 * along with this program; if not, see <https://github.com/FrozenBlock/Licenses>.
 */

package net.lunade.camera.menu;

import net.lunade.camera.registry.CameraPortSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DevelopingTableResultSlot extends Slot {
	private final DevelopingTableMenu menu;

	public DevelopingTableResultSlot(DevelopingTableMenu menu, Container container, int slot, int x, int y) {
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
