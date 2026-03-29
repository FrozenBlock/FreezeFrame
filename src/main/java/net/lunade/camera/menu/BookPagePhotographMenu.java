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

import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortMenuTypes;
import net.lunade.camera.util.BookPagePhotographHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;

public class BookPagePhotographMenu extends AbstractContainerMenu {
	public static final int PHOTO_SLOT = 0;
	private static final int INVENTORY_START_SLOT = 1;
	private static final int INVENTORY_END_SLOT = 28;
	private static final int HOTBAR_START_SLOT = 28;
	private static final int HOTBAR_END_SLOT = 37;
	private static final int PHOTO_SLOT_X = -1000;
	private static final int PHOTO_SLOT_Y = -1000;
	private static final int INVENTORY_SLOT_X = 5;
	private static final int INVENTORY_SLOT_Y = 54;
	private static final int HOTBAR_SLOT_Y = 112;

	private final Container photoContainer;
	private final DataSlot handData = DataSlot.standalone();
	private final DataSlot pageData = DataSlot.standalone();

	public BookPagePhotographMenu(int id, Inventory inventory) {
		this(id, inventory, InteractionHand.MAIN_HAND, 0);
	}

	public BookPagePhotographMenu(int id, Inventory inventory, InteractionHand hand, int pageIndex) {
		super(CameraPortMenuTypes.BOOK_PAGE_PHOTOGRAPH, id);
		this.photoContainer = new SimpleContainer(1);
		this.handData.set(hand == InteractionHand.OFF_HAND ? 1 : 0);
		this.pageData.set(Math.max(0, pageIndex));

		final ItemStack book = inventory.player.getItemInHand(hand);
		if (book.is(Items.WRITABLE_BOOK)) {
			this.photoContainer.setItem(PHOTO_SLOT, BookPagePhotographHelper.getPhoto(book, pageIndex).copy());
		}

		this.addSlot(new Slot(this.photoContainer, PHOTO_SLOT, PHOTO_SLOT_X, PHOTO_SLOT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(CameraPortItems.PHOTOGRAPH);
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});

		this.addStandardInventorySlots(inventory, INVENTORY_SLOT_X, INVENTORY_SLOT_Y);
		this.addDataSlot(this.handData);
		this.addDataSlot(this.pageData);
	}

	public InteractionHand getHand() {
		return this.handData.get() == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	public int getPageIndex() {
		return this.pageData.get();
	}

	@Override
	public boolean stillValid(Player player) {
		final ItemStack stack = player.getItemInHand(this.getHand());
		return stack.is(Items.WRITABLE_BOOK);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int fromIndex) {
		final Slot slot = this.slots.get(fromIndex);
		ItemStack clicked = ItemStack.EMPTY;
		if (slot == null || !slot.hasItem()) return clicked;

		final ItemStack item = slot.getItem();
		clicked = item.copy();
		if (fromIndex == PHOTO_SLOT) {
			if (!this.moveItemStackTo(item, INVENTORY_START_SLOT, HOTBAR_END_SLOT, true)) return ItemStack.EMPTY;
		} else if (item.is(CameraPortItems.PHOTOGRAPH)) {
			if (!this.moveItemStackTo(item, PHOTO_SLOT, PHOTO_SLOT + 1, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= INVENTORY_START_SLOT && fromIndex < INVENTORY_END_SLOT) {
			if (!this.moveItemStackTo(item, HOTBAR_START_SLOT, HOTBAR_END_SLOT, false)) return ItemStack.EMPTY;
		} else if (fromIndex >= HOTBAR_START_SLOT && fromIndex < HOTBAR_END_SLOT) {
			if (!this.moveItemStackTo(item, INVENTORY_START_SLOT, INVENTORY_END_SLOT, false)) return ItemStack.EMPTY;
		} else {
			return ItemStack.EMPTY;
		}

		if (item.isEmpty()) slot.set(ItemStack.EMPTY);
		slot.setChanged();
		if (item.getCount() == clicked.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, item);
		return clicked;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (player.level().isClientSide()) return;

		final ItemStack remainingPhoto = this.photoContainer.getItem(PHOTO_SLOT).copy();
		final ItemStack book = player.getItemInHand(this.getHand());
		if (!book.is(Items.WRITABLE_BOOK)) {
			if (!remainingPhoto.isEmpty()) player.getInventory().placeItemBackInInventory(remainingPhoto);
			this.photoContainer.setItem(PHOTO_SLOT, ItemStack.EMPTY);
			return;
		}

		final WritableBookContent bookContent = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
		if (bookContent == null) {
			if (!remainingPhoto.isEmpty()) player.getInventory().placeItemBackInInventory(remainingPhoto);
			this.photoContainer.setItem(PHOTO_SLOT, ItemStack.EMPTY);
			return;
		}

		if (remainingPhoto.is(CameraPortItems.PHOTOGRAPH)) {
			BookPagePhotographHelper.setPhoto(book, this.getPageIndex(), remainingPhoto.copyWithCount(1));
		} else {
			BookPagePhotographHelper.clearPhoto(book, this.getPageIndex());
		}
		this.photoContainer.setItem(PHOTO_SLOT, ItemStack.EMPTY);
	}
}
