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

package net.lunade.camera.mixin.camera;

import net.lunade.camera.component.BookPagePhotographs;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.util.BookPagePhotographHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookCloningRecipe.class)
public class BookCloningRecipeMixin {

	@Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
	private void cameraPort$incrementBookPhotoGenerationOnClone(
		CraftingInput craftingInput,
		HolderLookup.Provider provider,
		CallbackInfoReturnable<ItemStack> info
	) {
		this.cameraPort$applyPhotoGenerationIncrement(craftingInput, info);
	}

	@Inject(method = "assemble(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true, require = 0)
	private void cameraPort$incrementBookPhotoGenerationOnCloneBridge(
		RecipeInput recipeInput,
		HolderLookup.Provider provider,
		CallbackInfoReturnable<ItemStack> info
	) {
		if (recipeInput instanceof CraftingInput craftingInput) {
			this.cameraPort$applyPhotoGenerationIncrement(craftingInput, info);
		}
	}

	private void cameraPort$applyPhotoGenerationIncrement(CraftingInput craftingInput, CallbackInfoReturnable<ItemStack> info) {
		final ItemStack result = info.getReturnValue();
		if (!result.is(Items.WRITTEN_BOOK)) return;

		ItemStack sourceBook = ItemStack.EMPTY;
		for (int i = 0; i < craftingInput.size(); i++) {
			final ItemStack stack = craftingInput.getItem(i);
			if (stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
				sourceBook = stack;
				break;
			}
		}
		if (sourceBook.isEmpty()) return;

		final BookPagePhotographs sourcePhotographs = sourceBook.get(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (sourcePhotographs == null || sourcePhotographs.photographs().isEmpty()) return;

		result.set(
			CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS,
			BookPagePhotographHelper.incrementPhotoGenerationsForBookCopy(sourcePhotographs)
		);
		info.setReturnValue(result);
	}
}
