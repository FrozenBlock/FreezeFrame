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

package net.frozenblock.freezeframe.mixin.client.book;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.gui.screens.inventory.book.BookAccessWithPhotographs;
import net.frozenblock.freezeframe.component.BookPagePhotographs;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(BookViewScreen.BookAccess.class)
public class BookViewScreenBookAccessMixin implements BookAccessWithPhotographs {

	@Unique
	private BookPagePhotographs freezeframe$photographs;

	@ModifyReturnValue(method = "fromItem", at = @At("RETURN"))
	private static BookViewScreen.BookAccess freezeFrame$rememberBookPhotographs(
		BookViewScreen.BookAccess original,
		ItemStack itemStack
	) {
		if (original != null) original.freezeFrame$setPhotographs(itemStack.getOrDefault(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS, BookPagePhotographs.EMPTY));
		return original;
	}

	@Unique
	@Override
	public void freezeFrame$setPhotographs(BookPagePhotographs photographs) {
		this.freezeframe$photographs = photographs;
	}

	@Unique
	@Override
	public BookPagePhotographs freezeFrame$getPhotographs() {
		return this.freezeframe$photographs;
	}
}
