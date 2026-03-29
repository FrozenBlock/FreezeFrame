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

package net.lunade.camera.mixin.client.book;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.BookPagePhotographCache;
import net.lunade.camera.client.photograph.PhotographHoverTooltipRenderer;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.registry.CameraPortDataComponents;
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
	private static final Identifier CAMERA_PORT$PHOTO_FRAME = CameraPortConstants.id("container/written_book/photograph");
	@Unique
	private static final int CAMERA_PORT$PHOTO_SIZE = 84;
	@Unique
	private static final int CAMERA_PORT$PHOTO_X_OFFSET = 51;
	@Unique
	private static final int CAMERA_PORT$PHOTO_Y_OFFSET = 30;
	@Unique
	private static final int CAMERA_PORT$PHOTO_TEXT_Y_SHIFT = 81;

	@Shadow
	private BookViewScreen.BookAccess bookAccess;
	@Shadow
	private int currentPage;
	@Shadow
	private int backgroundLeft() {
		throw new AssertionError("Mixin injection failed - Camera Port BookViewScreenMixin.");
	}

	@ModifyConstant(method = "visitText", constant = @Constant(intValue = 30), require = 0)
	private int cameraPort$shiftWrittenBookTextForPhotos(int original) {
		return BookPagePhotographCache.getPhoto(this.bookAccess, this.currentPage).isEmpty() ? original : original + CAMERA_PORT$PHOTO_TEXT_Y_SHIFT;
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void cameraPort$renderBookPhotograph(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo info) {
		final ItemStack photo = BookPagePhotographCache.getPhoto(this.bookAccess, this.currentPage);
		if (photo.isEmpty()) return;

		final int backgroundLeft = this.backgroundLeft();
		final int x = backgroundLeft + CAMERA_PORT$PHOTO_X_OFFSET;
		final int y = 2 + CAMERA_PORT$PHOTO_Y_OFFSET;
		final Photograph photograph = photo.get(CameraPortDataComponents.PHOTOGRAPH);
		final Identifier photoId = photograph == null ? null : photograph.identifier();
		if (photoId == null) return;

		PhotographRenderer.blit(0, 0, x, y, graphics, photoId, CAMERA_PORT$PHOTO_SIZE, PhotographRenderer.FrameType.NONE);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CAMERA_PORT$PHOTO_FRAME, x, y, CAMERA_PORT$PHOTO_SIZE, CAMERA_PORT$PHOTO_SIZE);

		if (mouseX >= x && mouseX < x + CAMERA_PORT$PHOTO_SIZE && mouseY >= y && mouseY < y + CAMERA_PORT$PHOTO_SIZE) {
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
