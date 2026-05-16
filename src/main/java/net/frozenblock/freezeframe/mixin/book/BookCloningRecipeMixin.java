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

package net.frozenblock.freezeframe.mixin.book;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.frozenblock.freezeframe.component.BookPagePhotographs;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.util.BookPagePhotographHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BookCloningRecipe.class)
public class BookCloningRecipeMixin {

	@ModifyReturnValue(
		method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;",
		at = @At("RETURN")
	)
	private ItemStack freezeFrame$incrementBookPhotoGenerationOnClone(
		ItemStack original,
		@Local(name = "source") ItemStack source
	) {
		return this.freezeFrame$applyPhotoGenerationIncrement(original, source);
	}

	@Unique
	private ItemStack freezeFrame$applyPhotoGenerationIncrement(ItemStack original, ItemStack source) {
		if (!original.is(Items.WRITTEN_BOOK) || original.isEmpty() || source.isEmpty()) return original;

		final BookPagePhotographs sourcePhotographs = source.get(FFDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (sourcePhotographs == null || sourcePhotographs.photographs().isEmpty()) return original;

		original.set(
			FFDataComponents.BOOK_PAGE_PHOTOGRAPHS,
			BookPagePhotographHelper.incrementPhotoGenerationsForBookCopy(sourcePhotographs)
		);
		return original;
	}
}
