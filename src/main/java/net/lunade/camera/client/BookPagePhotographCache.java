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

package net.lunade.camera.client;

import java.util.Map;
import java.util.WeakHashMap;
import net.lunade.camera.component.BookPagePhotographs;
import net.lunade.camera.component.BookPagePhotographs.PagePhotograph;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.item.ItemStack;

public final class BookPagePhotographCache {
	private static final Map<BookViewScreen.BookAccess, BookPagePhotographs> CACHE = new WeakHashMap<>();

	private BookPagePhotographCache() {
	}

	public static void remember(BookViewScreen.BookAccess access, ItemStack stack) {
		final BookPagePhotographs photographs = stack.get(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (photographs == null || photographs.photographs().isEmpty()) {
			CACHE.remove(access);
		} else {
			CACHE.put(access, photographs);
		}
	}

	public static ItemStack getPhoto(BookViewScreen.BookAccess access, int pageIndex) {
		final BookPagePhotographs photographs = CACHE.get(access);
		if (photographs == null || pageIndex < 0) return ItemStack.EMPTY;
		for (PagePhotograph pagePhotograph : photographs.photographs()) {
			if (pagePhotograph.pageIndex() == pageIndex) return pagePhotograph.photograph();
		}
		return ItemStack.EMPTY;
	}
}
