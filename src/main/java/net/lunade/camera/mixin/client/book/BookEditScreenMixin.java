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

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.BookPagePhotographUiState;
import net.lunade.camera.client.photograph.PhotographHoverTooltipRenderer;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.networking.packet.OpenBookPagePhotographInventoryPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.util.BookPagePhotographHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin {
	@Unique
	private static final Identifier CAMERA_PORT$ADD_PHOTO = CameraPortConstants.id("container/written_book/add_photo");
	@Unique
	private static final Identifier CAMERA_PORT$ADD_PHOTO_HOVER = CameraPortConstants.id("container/written_book/add_photo_hover");
	@Unique
	private static final Identifier CAMERA_PORT$PHOTO_FRAME = CameraPortConstants.id("container/written_book/photograph");
	@Unique
	private static final int CAMERA_PORT$PHOTO_SIZE = 84;
	@Unique
	private static final int CAMERA_PORT$PHOTO_X_OFFSET = 51;
	@Unique
	private static final int CAMERA_PORT$PHOTO_Y_OFFSET = 30;
	@Unique
	private static final int CAMERA_PORT$ADD_BUTTON_SIZE = 16;
	@Unique
	private static final int CAMERA_PORT$ADD_BUTTON_X_OFFSET = 84;
	@Unique
	private static final int CAMERA_PORT$ADD_BUTTON_Y_OFFSET = 155;
	@Unique
	private static final int CAMERA_PORT$TEXT_WIDTH = 114;
	@Unique
	private static final int CAMERA_PORT$TEXT_LINE_HEIGHT = 9;
	@Unique
	private static final int CAMERA_PORT$PHOTO_TEXT_LINES = 3;
	@Unique
	private static final int CAMERA_PORT$DEFAULT_TEXT_LINES = 14;

	@Shadow
	private ItemStack book;
	@Shadow
	private int currentPage;
	@Shadow
	private List<String> pages;
	@Shadow
	private MultiLineEditBox page;
	@Shadow
	private InteractionHand hand;
	@Shadow
	private int backgroundLeft() {
		throw new AssertionError("Mixin injection failed - Camera Port BookEditScreenMixin.");
	}
	@Shadow
	private int backgroundTop() {
		throw new AssertionError("Mixin injection failed - Camera Port BookEditScreenMixin.");
	}

	@Unique
	private Button cameraPort$addPhotoButton;
	@Unique
	private boolean cameraPort$capturedDefaultPageBox;
	@Unique
	private int cameraPort$defaultPageY;
	@Unique
	private int cameraPort$defaultPageHeight;
	@Unique
	private int cameraPort$defaultPageLineLimit = CAMERA_PORT$DEFAULT_TEXT_LINES;
	@Unique
	private boolean cameraPort$wasPhotoLayout;

	@Inject(method = "init", at = @At("TAIL"))
	private void cameraPort$initPhotoControls(CallbackInfo info) {
		if (BookPagePhotographUiState.suppressBookEditorPhotoControls()) return;
		final int addButtonX = this.cameraPort$backgroundLeft() + CAMERA_PORT$ADD_BUTTON_X_OFFSET;
		final int addButtonY = this.cameraPort$backgroundTop() + CAMERA_PORT$ADD_BUTTON_Y_OFFSET;
		this.cameraPort$addPhotoButton = ((ScreenWidgetAdderMixin) (Object) this).cameraPort$addWidget(
			Button.builder(Component.empty(), button -> this.cameraPort$openPhotoInventory())
				.bounds(addButtonX, addButtonY, CAMERA_PORT$ADD_BUTTON_SIZE, CAMERA_PORT$ADD_BUTTON_SIZE)
				.build()
		);
		this.cameraPort$captureDefaultPageBox();
		this.cameraPort$applyPhotoTextLayout();
		this.cameraPort$updateButtonState();
	}

	@Inject(method = "updatePageContent", at = @At("TAIL"))
	private void cameraPort$onPageChanged(CallbackInfo info) {
		if (BookPagePhotographUiState.suppressBookEditorPhotoControls()) return;
		this.cameraPort$applyPhotoTextLayout();
		this.cameraPort$updateButtonState();
	}

	@Inject(method = "eraseEmptyTrailingPages", at = @At("HEAD"), cancellable = true)
	private void cameraPort$preservePhotoTrailingPages(CallbackInfo info) {
		int i = this.pages.size() - 1;
		while (i >= 0) {
			final boolean hasText = !this.pages.get(i).isEmpty();
			final boolean hasPhoto = BookPagePhotographHelper.hasPhoto(this.book, i);
			if (hasText || hasPhoto) break;
			this.pages.remove(i);
			i--;
		}
		if (this.pages.isEmpty()) this.pages.add("");
		if (this.currentPage >= this.pages.size()) this.currentPage = this.pages.size() - 1;
		info.cancel();
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void cameraPort$renderPhotoControls(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (BookPagePhotographUiState.suppressBookEditorPhotoControls()) return;
		this.cameraPort$applyPhotoTextLayout();
		this.cameraPort$updateButtonState();
		this.cameraPort$renderPagePhoto(graphics, mouseX, mouseY);
		this.cameraPort$renderAddPhotoButton(graphics, mouseX, mouseY);
	}

	@Unique
	private void cameraPort$openPhotoInventory() {
		if (!this.cameraPort$canOpenPhotoInventory()) return;
		final int pageIndex = this.currentPage;
		((BookEditScreenAccessor) (Object) this).cameraPort$invokeSaveChanges();
		BookPagePhotographUiState.rememberOpenRequest(this.hand, pageIndex);
		ClientPlayNetworking.send(new OpenBookPagePhotographInventoryPacket(this.hand, pageIndex));
	}

	@Unique
	private void cameraPort$updateButtonState() {
		if (this.cameraPort$addPhotoButton == null) return;
		this.cameraPort$addPhotoButton.visible = true;
		this.cameraPort$addPhotoButton.active = this.cameraPort$canOpenPhotoInventory();
	}

	@Unique
	private void cameraPort$renderPagePhoto(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		final ItemStack photoStack = BookPagePhotographHelper.getPhoto(this.book, this.currentPage);
		if (photoStack.isEmpty()) return;

		final Photograph photograph = photoStack.get(CameraPortDataComponents.PHOTOGRAPH);
		if (photograph == null) return;

		final int x = this.cameraPort$backgroundLeft() + CAMERA_PORT$PHOTO_X_OFFSET;
		final int y = this.cameraPort$backgroundTop() + CAMERA_PORT$PHOTO_Y_OFFSET;
		PhotographRenderer.blit(0, 0, x, y, graphics, photograph.identifier(), CAMERA_PORT$PHOTO_SIZE, PhotographRenderer.FrameType.NONE);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, CAMERA_PORT$PHOTO_FRAME, x, y, CAMERA_PORT$PHOTO_SIZE, CAMERA_PORT$PHOTO_SIZE);

		if (this.cameraPort$isWithin(mouseX, mouseY, x, y, CAMERA_PORT$PHOTO_SIZE, CAMERA_PORT$PHOTO_SIZE)) {
			final Minecraft minecraft = Minecraft.getInstance();
			PhotographHoverTooltipRenderer.extractRenderState(
				graphics,
				this.cameraPort$getFont(),
				minecraft.getWindow().getGuiScaledWidth(),
				minecraft.getWindow().getGuiScaledHeight(),
				mouseX,
				mouseY,
				photograph
			);
		}
	}

	@Unique
	private void cameraPort$renderAddPhotoButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		final boolean canOpen = this.cameraPort$canOpenPhotoInventory();
		final int x = this.cameraPort$backgroundLeft() + CAMERA_PORT$ADD_BUTTON_X_OFFSET;
		final int y = this.cameraPort$backgroundTop() + CAMERA_PORT$ADD_BUTTON_Y_OFFSET;
		final boolean hovered = this.cameraPort$isWithin(mouseX, mouseY, x, y, CAMERA_PORT$ADD_BUTTON_SIZE, CAMERA_PORT$ADD_BUTTON_SIZE);
		final Identifier sprite = hovered && canOpen ? CAMERA_PORT$ADD_PHOTO_HOVER : CAMERA_PORT$ADD_PHOTO;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, CAMERA_PORT$ADD_BUTTON_SIZE, CAMERA_PORT$ADD_BUTTON_SIZE);
		if (hovered && !canOpen) {
			graphics.setTooltipForNextFrame(
				this.cameraPort$getFont(),
				Component.translatable("screen.camera_port.book_photograph.empty_page_only"),
				mouseX,
				mouseY
			);
		}
	}

	@Unique
	private boolean cameraPort$canOpenPhotoInventory() {
		if (this.currentPage < 0 || this.currentPage >= this.pages.size()) return false;
		if (BookPagePhotographHelper.hasPhoto(this.book, this.currentPage)) return true;
		final String pageText = this.page == null ? this.pages.get(this.currentPage) : this.page.getValue();
		return this.cameraPort$getFont().wordWrapHeight(Component.literal(pageText), CAMERA_PORT$TEXT_WIDTH) <= (CAMERA_PORT$PHOTO_TEXT_LINES * CAMERA_PORT$TEXT_LINE_HEIGHT);
	}

	@Unique
	private void cameraPort$captureDefaultPageBox() {
		if (this.cameraPort$capturedDefaultPageBox || this.page == null) return;
		this.cameraPort$capturedDefaultPageBox = true;
		this.cameraPort$defaultPageY = this.page.getY();
		this.cameraPort$defaultPageHeight = this.page.getHeight();
		this.cameraPort$defaultPageLineLimit = Math.max(1, this.cameraPort$defaultPageHeight / CAMERA_PORT$TEXT_LINE_HEIGHT);
	}

	@Unique
	private void cameraPort$applyPhotoTextLayout() {
		if (this.page == null || this.currentPage < 0 || this.currentPage >= this.pages.size()) return;
		this.cameraPort$captureDefaultPageBox();
		if (!this.cameraPort$capturedDefaultPageBox) return;
		final boolean hasPhoto = BookPagePhotographHelper.hasPhoto(this.book, this.currentPage);
		final boolean layoutChanged = this.cameraPort$wasPhotoLayout != hasPhoto;
		this.cameraPort$wasPhotoLayout = hasPhoto;

		if (hasPhoto) {
			final int photoTextHeight = CAMERA_PORT$PHOTO_TEXT_LINES * CAMERA_PORT$TEXT_LINE_HEIGHT;
			this.page.setLineLimit(CAMERA_PORT$PHOTO_TEXT_LINES);
			this.page.setHeight(photoTextHeight);
			this.page.setY(this.cameraPort$defaultPageY + (this.cameraPort$defaultPageHeight - photoTextHeight) - CAMERA_PORT$TEXT_LINE_HEIGHT);
			if (layoutChanged) this.page.setValue(this.page.getValue(), true);
			this.cameraPort$trimCurrentPageToVisibleLineLimit(CAMERA_PORT$PHOTO_TEXT_LINES);
			return;
		}

		this.page.setLineLimit(this.cameraPort$defaultPageLineLimit);
		this.page.setHeight(this.cameraPort$defaultPageHeight);
		this.page.setY(this.cameraPort$defaultPageY);
		if (layoutChanged) this.page.setValue(this.page.getValue(), true);
	}

	@Unique
	private void cameraPort$trimCurrentPageToVisibleLineLimit(int lines) {
		String value = this.page.getValue();
		final int maxHeight = lines * CAMERA_PORT$TEXT_LINE_HEIGHT;
		while (!value.isEmpty() && this.cameraPort$getFont().wordWrapHeight(Component.literal(value), CAMERA_PORT$TEXT_WIDTH) > maxHeight) {
			value = value.substring(0, value.length() - 1);
		}
		if (!value.equals(this.page.getValue())) {
			this.page.setValue(value, true);
		}
	}

	@Unique
	private int cameraPort$backgroundLeft() {
		return this.backgroundLeft();
	}

	@Unique
	private int cameraPort$backgroundTop() {
		return this.backgroundTop();
	}

	@Unique
	private boolean cameraPort$isWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	@Unique
	private Font cameraPort$getFont() {
		return Minecraft.getInstance().font;
	}
}
