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

package net.frozenblock.freezeframe.client.gui.screens.inventory.book;

import java.util.Optional;
import net.frozenblock.freezeframe.component.BookPagePhotographs;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.world.item.ItemStack;

public interface BookAccessWithPhotographs {

	default void freezeFrame$setPhotographs(BookPagePhotographs photographs) {
		throw new AssertionError();
	}

	default BookPagePhotographs freezeFrame$getPhotographs() {
		throw new AssertionError();
	}

	default ItemStack freezeFrame$tryGetPhotographItemOnPage(int pageIndex) {
		if (pageIndex < 0) return ItemStack.EMPTY;

		final BookPagePhotographs bookPagePhotographs = this.freezeFrame$getPhotographs();
		if (bookPagePhotographs == null || bookPagePhotographs.photographs().isEmpty()) return ItemStack.EMPTY;

		for (BookPagePhotographs.PagePhotograph pagePhotograph : bookPagePhotographs.photographs()) {
			if (pagePhotograph.pageIndex() == pageIndex) return pagePhotograph.photograph();
		}

		return ItemStack.EMPTY;
	}

	default Optional<Photograph> freezeFrame$tryGetPhotographOnPage(int pageIndex) {
		final ItemStack item = this.freezeFrame$tryGetPhotographItemOnPage(pageIndex);
		if (item == null || item.isEmpty()) return Optional.empty();

		return Optional.ofNullable(item.get(FFDataComponents.PHOTOGRAPH));
	}

	default boolean freezeFrame$hasPhotographOnPage(int pageIndex) {
		return this.freezeFrame$tryGetPhotographOnPage(pageIndex).isPresent();
	}
}
