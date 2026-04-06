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

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.BookPagePhotographUiState;
import net.frozenblock.freezeframe.client.photograph.PhotographHoverTooltipRenderer;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.networking.packet.OpenBookPagePhotographInventoryPacket;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.util.BookPagePhotographHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
	@Unique
	private static final Identifier FREEZE_FRAME$ADD_PHOTO = FFConstants.id("container/book/add_photo");
	@Unique
	private static final Identifier FREEZE_FRAME$ADD_PHOTO_HOVER = FFConstants.id("container/book/add_photo_hover");
	@Unique
	private static final Identifier FREEZE_FRAME$PHOTO_FRAME = FFConstants.id("container/book/photograph");
	@Unique
	private static final int FREEZE_FRAME$PHOTO_SIZE = 84;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_X_OFFSET = 51;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_Y_OFFSET = 30;
	@Unique
	private static final int FREEZE_FRAME$ADD_BUTTON_SIZE = 16;
	@Unique
	private static final int FREEZE_FRAME$ADD_BUTTON_X_OFFSET = 84;
	@Unique
	private static final int FREEZE_FRAME$ADD_BUTTON_Y_OFFSET = 155;
	@Unique
	private static final int FREEZE_FRAME$TEXT_WIDTH = 114;
	@Unique
	private static final int FREEZE_FRAME$TEXT_LINE_HEIGHT = 9;
	@Unique
	private static final int FREEZE_FRAME$PHOTO_TEXT_LINES = 3;
	@Unique
	private static final int FREEZE_FRAME$DEFAULT_TEXT_LINES = 14;

	@Final
	@Shadow
	private ItemStack book;
	@Shadow
	private int currentPage;
	@Final
	@Shadow
	private List<String> pages;
	@Shadow
	private MultiLineEditBox page;
	@Final
	@Shadow
	private InteractionHand hand;

	protected BookEditScreenMixin(Component title) {
		super(title);
	}

	@Shadow
	private int backgroundLeft() {
		throw new AssertionError("Mixin injection failed - Freeze Frame BookEditScreenMixin.");
	}
	@Shadow
	private int backgroundTop() {
		throw new AssertionError("Mixin injection failed - Freeze Frame BookEditScreenMixin.");
	}

	@Shadow
	protected abstract void saveChanges();

	@Unique
	private Button freezeFrame$addPhotoButton;
	@Unique
	private boolean freezeFrame$capturedDefaultPageBox;
	@Unique
	private int freezeFrame$defaultPageY;
	@Unique
	private int freezeFrame$defaultPageHeight;
	@Unique
	private int freezeFrame$defaultPageLineLimit = FREEZE_FRAME$DEFAULT_TEXT_LINES;
	@Unique
	private boolean freezeFrame$wasPhotoLayout;

	@Inject(method = "init", at = @At("TAIL"))
	private void freezeFrame$initPhotoControls(CallbackInfo info) {
		if (BookPagePhotographUiState.suppressBookEditorPhotoControls()) return;
		final int addButtonX = this.freezeFrame$backgroundLeft() + FREEZE_FRAME$ADD_BUTTON_X_OFFSET;
		final int addButtonY = this.freezeFrame$backgroundTop() + FREEZE_FRAME$ADD_BUTTON_Y_OFFSET;
		this.freezeFrame$addPhotoButton = this.addWidget(
			Button.builder(Component.empty(), button -> this.freezeFrame$openPhotoInventory())
				.bounds(addButtonX, addButtonY, FREEZE_FRAME$ADD_BUTTON_SIZE, FREEZE_FRAME$ADD_BUTTON_SIZE)
				.build()
		);
		this.freezeFrame$captureDefaultPageBox();
		this.freezeFrame$applyPhotoTextLayout();
		this.freezeFrame$updateButtonState();
	}

	@Inject(method = "updatePageContent", at = @At("TAIL"))
	private void freezeFrame$onPageChanged(CallbackInfo info) {
		if (BookPagePhotographUiState.suppressBookEditorPhotoControls()) return;
		this.freezeFrame$applyPhotoTextLayout();
		this.freezeFrame$updateButtonState();
	}

	@Inject(method = "eraseEmptyTrailingPages", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$preservePhotoTrailingPages(CallbackInfo info) {
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
	private void freezeFrame$renderPhotoControls(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (BookPagePhotographUiState.suppressBookEditorPhotoControls()) return;
		this.freezeFrame$applyPhotoTextLayout();
		this.freezeFrame$updateButtonState();
		this.freezeFrame$renderPagePhoto(graphics, mouseX, mouseY);
		this.freezeFrame$renderAddPhotoButton(graphics, mouseX, mouseY);
	}

	@Unique
	private void freezeFrame$openPhotoInventory() {
		if (!this.freezeFrame$canOpenPhotoInventory()) return;
		final int pageIndex = this.currentPage;
		this.saveChanges();
		BookPagePhotographUiState.rememberOpenRequest(this.hand, pageIndex);
		ClientPlayNetworking.send(new OpenBookPagePhotographInventoryPacket(this.hand, pageIndex));
	}

	@Unique
	private void freezeFrame$updateButtonState() {
		if (this.freezeFrame$addPhotoButton == null) return;
		this.freezeFrame$addPhotoButton.visible = true;
		this.freezeFrame$addPhotoButton.active = this.freezeFrame$canOpenPhotoInventory();
	}

	@Unique
	private void freezeFrame$renderPagePhoto(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		final ItemStack photoStack = BookPagePhotographHelper.getPhoto(this.book, this.currentPage);
		if (photoStack.isEmpty()) return;

		final Photograph photograph = photoStack.get(FFDataComponents.PHOTOGRAPH);
		if (photograph == null) return;

		final int x = this.freezeFrame$backgroundLeft() + FREEZE_FRAME$PHOTO_X_OFFSET;
		final int y = this.freezeFrame$backgroundTop() + FREEZE_FRAME$PHOTO_Y_OFFSET;
		PhotographRenderer.blit(0, 0, x, y, graphics, photograph.identifier(), FREEZE_FRAME$PHOTO_SIZE, PhotographRenderer.FrameType.NONE);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FREEZE_FRAME$PHOTO_FRAME, x, y, FREEZE_FRAME$PHOTO_SIZE, FREEZE_FRAME$PHOTO_SIZE);

		if (this.freezeFrame$isWithin(mouseX, mouseY, x, y, FREEZE_FRAME$PHOTO_SIZE, FREEZE_FRAME$PHOTO_SIZE)) {
			final Minecraft minecraft = Minecraft.getInstance();
			PhotographHoverTooltipRenderer.extractRenderState(
				graphics,
				this.freezeFrame$getFont(),
				minecraft.getWindow().getGuiScaledWidth(),
				minecraft.getWindow().getGuiScaledHeight(),
				mouseX,
				mouseY,
				photograph
			);
		}
	}

	@Unique
	private void freezeFrame$renderAddPhotoButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		final boolean canOpen = this.freezeFrame$canOpenPhotoInventory();
		final int x = this.freezeFrame$backgroundLeft() + FREEZE_FRAME$ADD_BUTTON_X_OFFSET;
		final int y = this.freezeFrame$backgroundTop() + FREEZE_FRAME$ADD_BUTTON_Y_OFFSET;
		final boolean hovered = this.freezeFrame$isWithin(mouseX, mouseY, x, y, FREEZE_FRAME$ADD_BUTTON_SIZE, FREEZE_FRAME$ADD_BUTTON_SIZE);
		final Identifier sprite = hovered && canOpen ? FREEZE_FRAME$ADD_PHOTO_HOVER : FREEZE_FRAME$ADD_PHOTO;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, FREEZE_FRAME$ADD_BUTTON_SIZE, FREEZE_FRAME$ADD_BUTTON_SIZE);
		if (hovered && !canOpen) {
			graphics.setTooltipForNextFrame(
				this.freezeFrame$getFont(),
				Component.translatable("screen.freezeframe.book_photograph.empty_page_only"),
				mouseX,
				mouseY
			);
		}
	}

	@Unique
	private boolean freezeFrame$canOpenPhotoInventory() {
		if (this.currentPage < 0 || this.currentPage >= this.pages.size()) return false;
		if (BookPagePhotographHelper.hasPhoto(this.book, this.currentPage)) return true;
		final String pageText = this.page == null ? this.pages.get(this.currentPage) : this.page.getValue();
		return this.freezeFrame$getFont().wordWrapHeight(Component.literal(pageText), FREEZE_FRAME$TEXT_WIDTH) <= (FREEZE_FRAME$PHOTO_TEXT_LINES * FREEZE_FRAME$TEXT_LINE_HEIGHT);
	}

	@Unique
	private void freezeFrame$captureDefaultPageBox() {
		if (this.freezeFrame$capturedDefaultPageBox || this.page == null) return;
		this.freezeFrame$capturedDefaultPageBox = true;
		this.freezeFrame$defaultPageY = this.page.getY();
		this.freezeFrame$defaultPageHeight = this.page.getHeight();
		this.freezeFrame$defaultPageLineLimit = Math.max(1, this.freezeFrame$defaultPageHeight / FREEZE_FRAME$TEXT_LINE_HEIGHT);
	}

	@Unique
	private void freezeFrame$applyPhotoTextLayout() {
		if (this.page == null || this.currentPage < 0 || this.currentPage >= this.pages.size()) return;
		this.freezeFrame$captureDefaultPageBox();
		if (!this.freezeFrame$capturedDefaultPageBox) return;
		final boolean hasPhoto = BookPagePhotographHelper.hasPhoto(this.book, this.currentPage);
		final boolean layoutChanged = this.freezeFrame$wasPhotoLayout != hasPhoto;
		this.freezeFrame$wasPhotoLayout = hasPhoto;

		if (hasPhoto) {
			final int photoTextHeight = FREEZE_FRAME$PHOTO_TEXT_LINES * FREEZE_FRAME$TEXT_LINE_HEIGHT;
			this.page.setLineLimit(FREEZE_FRAME$PHOTO_TEXT_LINES);
			this.page.setHeight(photoTextHeight);
			this.page.setY(this.freezeFrame$defaultPageY + (this.freezeFrame$defaultPageHeight - photoTextHeight) - FREEZE_FRAME$TEXT_LINE_HEIGHT);
			if (layoutChanged) this.page.setValue(this.page.getValue(), true);
			this.freezeFrame$trimCurrentPageToVisibleLineLimit(FREEZE_FRAME$PHOTO_TEXT_LINES);
			return;
		}

		this.page.setLineLimit(this.freezeFrame$defaultPageLineLimit);
		this.page.setHeight(this.freezeFrame$defaultPageHeight);
		this.page.setY(this.freezeFrame$defaultPageY);
		if (layoutChanged) this.page.setValue(this.page.getValue(), true);
	}

	@Unique
	private void freezeFrame$trimCurrentPageToVisibleLineLimit(int lines) {
		String value = this.page.getValue();
		final int maxHeight = lines * FREEZE_FRAME$TEXT_LINE_HEIGHT;
		while (!value.isEmpty() && this.freezeFrame$getFont().wordWrapHeight(Component.literal(value), FREEZE_FRAME$TEXT_WIDTH) > maxHeight) {
			value = value.substring(0, value.length() - 1);
		}
		if (!value.equals(this.page.getValue())) {
			this.page.setValue(value, true);
		}
	}

	@Unique
	private int freezeFrame$backgroundLeft() {
		return this.backgroundLeft();
	}

	@Unique
	private int freezeFrame$backgroundTop() {
		return this.backgroundTop();
	}

	@Unique
	private boolean freezeFrame$isWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	@Unique
	private Font freezeFrame$getFont() {
		return Minecraft.getInstance().font;
	}
}
