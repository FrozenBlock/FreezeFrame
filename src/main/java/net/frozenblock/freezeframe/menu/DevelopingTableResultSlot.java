/*
 * Copyright 2026 FrozenBlock
 * This file is part of Freeze Frame.
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

package net.frozenblock.freezeframe.menu;

import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DevelopingTableResultSlot extends Slot {
	private final DevelopingTableMenu menu;
	private final Player player;
	private int removeCount;

	public DevelopingTableResultSlot(DevelopingTableMenu menu, Player player, Container container, int slot, int x, int y) {
		super(container, slot, x, y);
		this.menu = menu;
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack remove(final int amount) {
		if (this.hasItem()) this.removeCount = this.removeCount + Math.min(amount, this.getItem().getCount());
		return super.remove(amount);
	}

	@Override
	protected void onQuickCraft(final ItemStack picked, final int count) {
		this.removeCount += count;
		this.checkTakeAchievements(picked);
	}

	@Override
	protected void checkTakeAchievements(ItemStack carried) {
		carried.onCraftedBy(this.player, this.removeCount);

		final Photograph photograph = carried.get(FFDataComponents.PHOTOGRAPH);
		if (photograph != null) PhotographTracker.incrementPhotographCountAndDeleteIfEmpty(player.level(), photograph.identifier().getPath(), carried.getCount());

		this.removeCount = 0;
	}

	@Override
	public void onTake(Player player, ItemStack carried) {
		this.checkTakeAchievements(carried);

		final ItemStack input = this.menu.paperSlot.remove(1);
		if (!input.isEmpty()) this.menu.setupResultSlot();

		this.menu.access.execute((level, pos) -> {
			final long gameTime = level.getGameTime();
			if (this.menu.lastSoundTime == gameTime) return;

			level.playSound(
				null,
				pos,
				FFSounds.DEVELOPING_TABLE_TAKE_RESULT,
				SoundSource.BLOCKS,
				1F,
				0.8F + (level.getRandom().nextFloat() * 0.4F)
			);
			this.menu.lastSoundTime = gameTime;
		});

		super.onTake(player, carried);
	}

	@Override
	public boolean isFake() {
		return true;
	}
}
