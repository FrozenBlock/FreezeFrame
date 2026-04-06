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

import java.util.ArrayList;
import java.util.List;
import net.frozenblock.freezeframe.component.BookPagePhotographs;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFBlocks;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.TransmuteRecipe;

public class DevelopingTableMenu extends AbstractContainerMenu {
	public static final int SOURCE_SLOT = 0;
	public static final int PAPER_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	private static final int INV_SLOT_START = 3;
	private static final int INV_SLOT_END = 30;
	private static final int USE_ROW_SLOT_START = 30;
	private static final int USE_ROW_SLOT_END = 39;
	private static final ItemStackTemplate PHOTOGRAPH_COPY_TEMPLATE = new ItemStackTemplate(FFItems.PHOTOGRAPH, 1);
	protected final ContainerLevelAccess access;
	public final DataSlot photographIndex = DataSlot.standalone();
	long lastSoundTime;
	final Slot sourceSlot;
	final Slot paperSlot;
	final Slot resultSlot;
	Runnable slotUpdateListener = () -> {};
	public final Container inputContainer = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			DevelopingTableMenu.this.slotsChanged(this);
			DevelopingTableMenu.this.slotUpdateListener.run();
		}
	};
	final ResultContainer resultContainer = new ResultContainer();

	public DevelopingTableMenu(int id, Inventory inventory) {
		this(id, inventory, ContainerLevelAccess.NULL);
	}

	public DevelopingTableMenu(int id, Inventory inventory, ContainerLevelAccess access) {
		super(FFMenuTypes.DEVELOPING_TABLE, id);
		this.access = access;
		this.sourceSlot = addSlot(new DevelopingTableSourceSlot(this.inputContainer, SOURCE_SLOT, 14, 15));
		this.paperSlot = addSlot(new DevelopingTablePaperSlot(this.inputContainer, PAPER_SLOT, 44, 113));
		this.resultSlot = addSlot(new DevelopingTableResultSlot(this, this.resultContainer, RESULT_SLOT, 116, 113));
		this.addStandardInventorySlots(inventory, 8, 144);
		this.addDataSlot(this.photographIndex);
	}

	public boolean hasSourceItem() {
		return this.sourceSlot.hasItem();
	}

	public ItemStack getSourceItem() {
		return this.sourceSlot.getItem();
	}

	public boolean hasPaper() {
		return this.paperSlot.hasItem() && this.paperSlot.getItem().is(Items.PAPER);
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, FFBlocks.DEVELOPING_TABLE);
	}

	@Override
	public void slotsChanged(Container container) {
		this.setupResultSlot();
	}

	void setupResultSlot() {
		ItemStack stack = ItemStack.EMPTY;

		if (this.hasPaper()) {
			final ItemStack sourceStack = this.getSourceItem();
			if (sourceStack.is(FFItems.PHOTOGRAPH)) {
				this.photographIndex.set(0);
				final Photograph photograph = sourceStack.get(FFDataComponents.PHOTOGRAPH);
				if (photograph != null && photograph.canCopy()) {
					stack = TransmuteRecipe.createWithOriginalComponents(PHOTOGRAPH_COPY_TEMPLATE, sourceStack);
					stack.set(FFDataComponents.PHOTOGRAPH, photograph.asCopy());
				}
			} else if (sourceStack.is(FFItems.FILM) && sourceStack.has(FFDataComponents.FILM_CONTENTS)) {
				final int clampedIndex = Math.max(0, Math.min(this.photographIndex.get(), sourceStack.get(FFDataComponents.FILM_CONTENTS).size() - 1));
				this.photographIndex.set(clampedIndex);
				final Photograph photograph = sourceStack.get(FFDataComponents.FILM_CONTENTS).getPhotographAtIndex(this.photographIndex.get());
				if (photograph != null) {
					stack = new ItemStack(FFItems.PHOTOGRAPH);
					stack.set(FFDataComponents.PHOTOGRAPH, photograph);
				}
			} else if (DevelopingTableSourceSlot.isValidBookSource(sourceStack)) {
				final List<Photograph> photographs = this.getBookPhotographs(sourceStack);
				if (!photographs.isEmpty()) {
					final int clampedIndex = Math.max(0, Math.min(this.photographIndex.get(), photographs.size() - 1));
					this.photographIndex.set(clampedIndex);
					final Photograph photograph = photographs.get(clampedIndex);
					if (photograph != null && photograph.canCopy()) {
						stack = new ItemStack(FFItems.PHOTOGRAPH);
						stack.set(FFDataComponents.PHOTOGRAPH, photograph.asCopy());
					}
				} else {
					this.photographIndex.set(0);
				}
			} else {
				this.photographIndex.set(0);
			}
		}

		this.resultSlot.set(stack);
		this.broadcastChanges();
	}

	public void registerUpdateListener(Runnable listener) {
		this.slotUpdateListener = listener;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int fromIndex) {
		final Slot slot = this.slots.get(fromIndex);
		ItemStack clicked = ItemStack.EMPTY;
		if (slot == null || !slot.hasItem()) return clicked;

		final ItemStack item = slot.getItem();
		clicked = item.copy();
		if (fromIndex == RESULT_SLOT) {
			item.onCraftedBy(player, 1);
			if (!this.moveItemStackTo(item, INV_SLOT_START, USE_ROW_SLOT_END, true)) return ItemStack.EMPTY;
			slot.onQuickCraft(item, clicked);
		} else if (fromIndex == SOURCE_SLOT || fromIndex == PAPER_SLOT) {
			if (!this.moveItemStackTo(item, INV_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (DevelopingTableSourceSlot.isValidAsSource(item)) {
			if (!this.moveItemStackTo(item, SOURCE_SLOT, SOURCE_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (item.is(Items.PAPER)) {
			if (!this.moveItemStackTo(item, PAPER_SLOT, PAPER_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= INV_SLOT_START && fromIndex < INV_SLOT_END) {
			if (!this.moveItemStackTo(item, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= USE_ROW_SLOT_START && fromIndex < USE_ROW_SLOT_END && !this.moveItemStackTo(item, INV_SLOT_START, INV_SLOT_END, false)) {
			return ItemStack.EMPTY;
		}

		if (item.isEmpty()) slot.set(ItemStack.EMPTY);

		slot.setChanged();
		if (item.getCount() == clicked.getCount()) return ItemStack.EMPTY;

		slot.onTake(player, item);
		this.setupResultSlot();

		return clicked;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultContainer.removeItemNoUpdate(RESULT_SLOT);
		this.access.execute((level, pos) -> this.clearContainer(player, this.inputContainer));
	}

	public void setupDataAndResultSlot(int photographIndex) {
		this.photographIndex.set(photographIndex);
		this.setupResultSlot();
	}

	private List<Photograph> getBookPhotographs(ItemStack sourceStack) {
		final BookPagePhotographs pagePhotographs = sourceStack.get(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (pagePhotographs == null || pagePhotographs.photographs().isEmpty()) return List.of();

		final List<Photograph> photographs = new ArrayList<>();
		for (BookPagePhotographs.PagePhotograph pagePhotograph : pagePhotographs.photographs()) {
			final ItemStack photoStack = pagePhotograph.photograph();
			if (!photoStack.is(FFItems.PHOTOGRAPH)) continue;
			final Photograph photograph = photoStack.get(FFDataComponents.PHOTOGRAPH);
			if (photograph != null) photographs.add(photograph);
		}
		return photographs;
	}
}
