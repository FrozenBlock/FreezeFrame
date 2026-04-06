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

package net.frozenblock.freezeframe.util;

import java.util.ArrayList;
import java.util.List;
import net.frozenblock.freezeframe.component.BookPagePhotographs;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.minecraft.world.item.ItemStack;

public final class BookPagePhotographHelper {

	private BookPagePhotographHelper() {
	}

	public static ItemStack getPhoto(ItemStack book, int pageIndex) {
		if (pageIndex < 0) return ItemStack.EMPTY;
		final BookPagePhotographs photographs = book.get(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (photographs == null) return ItemStack.EMPTY;
		for (BookPagePhotographs.PagePhotograph pagePhotograph : photographs.photographs()) {
			if (pagePhotograph.pageIndex() == pageIndex) {
				final ItemStack stack = pagePhotograph.photograph();
				return stack.is(FFItems.PHOTOGRAPH) ? stack : ItemStack.EMPTY;
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
		final List<BookPagePhotographs.PagePhotograph> entries = new ArrayList<>();
		final BookPagePhotographs existing = book.get(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (existing != null) {
			for (BookPagePhotographs.PagePhotograph pagePhotograph : existing.photographs()) {
				if (pagePhotograph.pageIndex() == pageIndex) continue;
				if (!pagePhotograph.photograph().is(FFItems.PHOTOGRAPH)) continue;
				entries.add(new BookPagePhotographs.PagePhotograph(pagePhotograph.pageIndex(), pagePhotograph.photograph().copyWithCount(1)));
			}
		}

		if (photoStack.is(FFItems.PHOTOGRAPH)) {
			entries.add(new BookPagePhotographs.PagePhotograph(pageIndex, photoStack.copyWithCount(1)));
		}

		entries.sort((left, right) -> Integer.compare(left.pageIndex(), right.pageIndex()));
		if (entries.isEmpty()) {
			book.remove(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		} else {
			book.set(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS, new BookPagePhotographs(entries));
		}
	}

	public static BookPagePhotographs incrementPhotoGenerationsForBookCopy(BookPagePhotographs photographs) {
		final List<BookPagePhotographs.PagePhotograph> updatedPages = new ArrayList<>(photographs.photographs().size());
		for (BookPagePhotographs.PagePhotograph pagePhotograph : photographs.photographs()) {
			final ItemStack stack = pagePhotograph.photograph().copyWithCount(1);
			if (stack.is(FFItems.PHOTOGRAPH)) {
				final Photograph photograph = stack.get(FFDataComponents.PHOTOGRAPH);
				if (photograph != null) {
					stack.set(FFDataComponents.PHOTOGRAPH, photograph.asBookCopy());
				}
			}
			updatedPages.add(new BookPagePhotographs.PagePhotograph(pagePhotograph.pageIndex(), stack));
		}
		return new BookPagePhotographs(updatedPages);
	}
}
