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

package net.lunade.camera.util;

import java.util.ArrayList;
import java.util.List;
import net.lunade.camera.component.BookPagePhotographs;
import net.lunade.camera.component.BookPagePhotographs.PagePhotograph;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.world.item.ItemStack;

public final class BookPagePhotographHelper {
	private BookPagePhotographHelper() {
	}

	public static ItemStack getPhoto(ItemStack book, int pageIndex) {
		if (pageIndex < 0) return ItemStack.EMPTY;
		final BookPagePhotographs photographs = book.get(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (photographs == null) return ItemStack.EMPTY;
		for (PagePhotograph pagePhotograph : photographs.photographs()) {
			if (pagePhotograph.pageIndex() == pageIndex) {
				final ItemStack stack = pagePhotograph.photograph();
				return stack.is(CameraPortItems.PHOTOGRAPH) ? stack : ItemStack.EMPTY;
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean hasPhoto(ItemStack book, int pageIndex) {
		return !getPhoto(book, pageIndex).isEmpty();
	}

	public static void clearPhoto(ItemStack book, int pageIndex) {
		setPhoto(book, pageIndex, ItemStack.EMPTY);
	}

	public static void setPhoto(ItemStack book, int pageIndex, ItemStack photoStack) {
		if (pageIndex < 0 || pageIndex >= BookPagePhotographs.MAX_PAGES) return;
		final List<PagePhotograph> entries = new ArrayList<>();
		final BookPagePhotographs existing = book.get(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (existing != null) {
			for (PagePhotograph pagePhotograph : existing.photographs()) {
				if (pagePhotograph.pageIndex() == pageIndex) continue;
				if (!pagePhotograph.photograph().is(CameraPortItems.PHOTOGRAPH)) continue;
				entries.add(new PagePhotograph(pagePhotograph.pageIndex(), pagePhotograph.photograph().copyWithCount(1)));
			}
		}

		if (photoStack.is(CameraPortItems.PHOTOGRAPH)) {
			entries.add(new PagePhotograph(pageIndex, photoStack.copyWithCount(1)));
		}

		entries.sort((left, right) -> Integer.compare(left.pageIndex(), right.pageIndex()));
		if (entries.isEmpty()) {
			book.remove(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		} else {
			book.set(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS, new BookPagePhotographs(entries));
		}
	}

	public static BookPagePhotographs incrementPhotoGenerationsForBookCopy(BookPagePhotographs photographs) {
		final List<PagePhotograph> updatedPages = new ArrayList<>(photographs.photographs().size());
		for (PagePhotograph pagePhotograph : photographs.photographs()) {
			final ItemStack stack = pagePhotograph.photograph().copyWithCount(1);
			if (stack.is(CameraPortItems.PHOTOGRAPH)) {
				final Photograph photograph = stack.get(CameraPortDataComponents.PHOTOGRAPH);
				if (photograph != null) {
					stack.set(CameraPortDataComponents.PHOTOGRAPH, photograph.asBookCopy());
				}
			}
			updatedPages.add(new PagePhotograph(pagePhotograph.pageIndex(), stack));
		}
		return new BookPagePhotographs(updatedPages);
	}
}
