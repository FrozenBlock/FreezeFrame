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

import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortMenuTypes;
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

public class DevelopmentTableMenu extends AbstractContainerMenu {
	public static final int SOURCE_SLOT = 0;
	public static final int PAPER_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	private static final int INV_SLOT_START = 3;
	private static final int INV_SLOT_END = 30;
	private static final int USE_ROW_SLOT_START = 30;
	private static final int USE_ROW_SLOT_END = 39;
	private static final ItemStackTemplate PHOTOGRAPH_COPY_TEMPLATE = new ItemStackTemplate(CameraPortItems.PHOTOGRAPH, 1);
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
			DevelopmentTableMenu.this.slotsChanged(this);
			DevelopmentTableMenu.this.slotUpdateListener.run();
		}
	};
	final ResultContainer resultContainer = new ResultContainer();

	public DevelopmentTableMenu(int id, Inventory inventory) {
		this(id, inventory, ContainerLevelAccess.NULL);
	}

	public DevelopmentTableMenu(int id, Inventory inventory, ContainerLevelAccess access) {
		super(CameraPortMenuTypes.DEVELOPMENT_TABLE, id);
		this.access = access;
		this.sourceSlot = addSlot(new DevelopmentTableSourceSlot(this.inputContainer, SOURCE_SLOT, 14, 15));
		this.paperSlot = addSlot(new DevelopmentTablePaperSlot(this.inputContainer, PAPER_SLOT, 80, 113));
		this.resultSlot = addSlot(new DevelopmentTableResultSlot(this, this.resultContainer, RESULT_SLOT, 134, 113));
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
		return stillValid(this.access, player, CameraPortBlocks.DEVELOPMENT_TABLE);
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		this.setupResultSlot();
		this.broadcastChanges();
	}

	void setupResultSlot() {
		if (!this.hasPaper() || !this.hasSourceItem()) {
			this.photographIndex.set(0);
			this.resultSlot.set(ItemStack.EMPTY);
			this.broadcastChanges();
			return;
		}

		final ItemStack sourceStack = this.getSourceItem();
		ItemStack stack = ItemStack.EMPTY;
		if (sourceStack.is(CameraPortItems.PHOTOGRAPH)) {
			this.photographIndex.set(0);
			final PhotographComponent photographComponent = sourceStack.get(CameraPortDataComponents.PHOTOGRAPH);
			if (photographComponent != null && photographComponent.canCopy()) {
				stack = TransmuteRecipe.createWithOriginalComponents(PHOTOGRAPH_COPY_TEMPLATE, sourceStack);
				stack.set(CameraPortDataComponents.PHOTOGRAPH, photographComponent.asCopy());
			}
		} else if (sourceStack.is(CameraPortItems.FILM) && sourceStack.has(CameraPortDataComponents.FILM_CONTENTS)) {
			final int clampedIndex = Math.max(0, Math.min(this.photographIndex.get(), sourceStack.get(CameraPortDataComponents.FILM_CONTENTS).size() - 1));
			this.photographIndex.set(clampedIndex);
			final PhotographComponent photographComponent = sourceStack.get(CameraPortDataComponents.FILM_CONTENTS).getPhotographAtIndex(this.photographIndex.get());
			if (photographComponent != null) {
				stack = new ItemStack(CameraPortItems.PHOTOGRAPH);
				stack.set(CameraPortDataComponents.PHOTOGRAPH, photographComponent);
			}
		} else {
			this.photographIndex.set(0);
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
		} else if (DevelopmentTableSourceSlot.isValidAsSource(item)) {
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
		this.broadcastChanges();

		return clicked;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.resultContainer.removeItemNoUpdate(1);
		this.access.execute((level, pos) -> this.clearContainer(player, this.inputContainer));
	}

	public void setupDataAndResultSlot(int photographIndex) {
		this.photographIndex.set(photographIndex);
		this.setupResultSlot();
	}
}
