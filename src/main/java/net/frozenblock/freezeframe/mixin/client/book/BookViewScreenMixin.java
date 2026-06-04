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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.gui.screens.inventory.book.BookPagePhotographCache;
import net.frozenblock.freezeframe.client.photograph.PhotographHoverTooltipRenderer;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin extends Screen {
	@Unique
	private static final int FREEZE_FRAME$PHOTO_SIZE = 84;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_X_OFFSET = 51;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_Y_OFFSET = 33;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_TEXT_Y_SHIFT = 98;

	@Shadow
	private BookViewScreen.BookAccess bookAccess;
	@Shadow
	private int currentPage;

	protected BookViewScreenMixin(Component title) {
		super(title);
	}

	@Shadow
	private int backgroundLeft() {
		throw new AssertionError("Mixin injection failed - Freeze Frame BookViewScreenMixin.");
	}

	@WrapOperation(
		method = "visitText",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(IILnet/minecraft/util/FormattedCharSequence;)V"
		)
	)
	private void freezeFrame$shiftWrittenBookTextForPhotos(ActiveTextCollector instance, int x, int y, FormattedCharSequence text, Operation<Void> original) {
		if (!BookPagePhotographCache.getPhoto(this.bookAccess, this.currentPage).isEmpty()) y += FREEZE_FRAME$PHOTO_TEXT_Y_SHIFT;
		original.call(instance, x, y, text);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void freezeFrame$renderBookPhotograph(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
		final ItemStack photo = BookPagePhotographCache.getPhoto(this.bookAccess, this.currentPage);
		if (photo.isEmpty()) return;

		final Photograph photograph = photo.get(FFDataComponents.PHOTOGRAPH);
		final Identifier photoId = photograph == null ? null : photograph.identifier();
		if (photoId == null) return;

		final int backgroundLeft = this.backgroundLeft();
		final int x = backgroundLeft + FREEZE_FRAME$PHOTO_X_OFFSET;
		final int y = 2 + FREEZE_FRAME$PHOTO_Y_OFFSET;
		PhotographRenderer.blitForBook(x, y, graphics, photoId);

		if (mouseX >= x && mouseX < x + FREEZE_FRAME$PHOTO_SIZE && mouseY >= y && mouseY < y + FREEZE_FRAME$PHOTO_SIZE) {
			final Window window = this.minecraft.getWindow();
			PhotographHoverTooltipRenderer.extractRenderState(
				graphics,
				this.font,
				window.getGuiScaledWidth(),
				window.getGuiScaledHeight(),
				mouseX,
				mouseY,
				photograph
			);
		}
	}
}
