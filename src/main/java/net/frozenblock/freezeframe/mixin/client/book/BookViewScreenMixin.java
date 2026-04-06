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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.BookPagePhotographCache;
import net.frozenblock.freezeframe.client.photograph.PhotographHoverTooltipRenderer;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {
	@Unique
	private static final Identifier FREEZE_FRAME$PHOTO_FRAME = FFConstants.id("container/book/photograph");
	@Unique
	private static final int FREEZE_FRAME$PHOTO_SIZE = 84;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_X_OFFSET = 51;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_Y_OFFSET = 30;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_TEXT_Y_SHIFT = 81;

	@Shadow
	private BookViewScreen.BookAccess bookAccess;
	@Shadow
	private int currentPage;
	@Shadow
	private int backgroundLeft() {
		throw new AssertionError("Mixin injection failed - Freeze Frame BookViewScreenMixin.");
	}

	@ModifyConstant(method = "visitText", constant = @Constant(intValue = 30), require = 0)
	private int freezeFrame$shiftWrittenBookTextForPhotos(int original) {
		return BookPagePhotographCache.getPhoto(this.bookAccess, this.currentPage).isEmpty() ? original : original + FREEZE_FRAME$PHOTO_TEXT_Y_SHIFT;
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void freezeFrame$renderBookPhotograph(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo info) {
		final ItemStack photo = BookPagePhotographCache.getPhoto(this.bookAccess, this.currentPage);
		if (photo.isEmpty()) return;

		final int backgroundLeft = this.backgroundLeft();
		final int x = backgroundLeft + FREEZE_FRAME$PHOTO_X_OFFSET;
		final int y = 2 + FREEZE_FRAME$PHOTO_Y_OFFSET;
		final Photograph photograph = photo.get(FFDataComponents.PHOTOGRAPH);
		final Identifier photoId = photograph == null ? null : photograph.identifier();
		if (photoId == null) return;

		PhotographRenderer.blit(0, 0, x, y, graphics, photoId, FREEZE_FRAME$PHOTO_SIZE, PhotographRenderer.FrameType.NONE);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FREEZE_FRAME$PHOTO_FRAME, x, y, FREEZE_FRAME$PHOTO_SIZE, FREEZE_FRAME$PHOTO_SIZE);

		if (mouseX >= x && mouseX < x + FREEZE_FRAME$PHOTO_SIZE && mouseY >= y && mouseY < y + FREEZE_FRAME$PHOTO_SIZE) {
			final Minecraft minecraft = Minecraft.getInstance();
			PhotographHoverTooltipRenderer.extractRenderState(
				graphics,
				minecraft.font,
				minecraft.getWindow().getGuiScaledWidth(),
				minecraft.getWindow().getGuiScaledHeight(),
				mouseX,
				mouseY,
				photograph
			);
		}
	}
}
