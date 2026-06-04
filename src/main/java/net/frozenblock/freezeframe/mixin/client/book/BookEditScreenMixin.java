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

import com.mojang.blaze3d.platform.Window;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.gui.screens.inventory.book.BookPagePhotographScreen;
import net.frozenblock.freezeframe.client.gui.screens.inventory.book.BookPagePhotographUiState;
import net.frozenblock.freezeframe.client.photograph.PhotographHoverTooltipRenderer;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.networking.packet.OpenBookPagePhotographInventoryPacket;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.util.BookPagePhotographHelper;
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
	private static final Identifier FREEZE_FRAME$ADD_PHOTOGRAPH = FFConstants.id("container/book/add_photograph");
	@Unique
	private static final Identifier FREEZE_FRAME$ADD_PHOTOGRAPH_HOVER = FFConstants.id("container/book/add_photograph_hover");
	@Unique
	private static final int FREEZE_FRAME$PHOTOGRAPH_SIZE = 84;
	@Unique
	private static final int FREEZE_FRAME$PHOTOGRAPH_X_OFFSET = 51;
	@Unique
	private static final int FREEZE_FRAME$PHOTOGRAPH_Y_OFFSET = 33;
	@Unique
	private static final int FREEZE_FRAME$ADD_BUTTON_SIZE = 16;
	@Unique
	private static final int FREEZE_FRAME$ADD_BUTTON_X_OFFSET = 84;
	@Unique
	private static final int FREEZE_FRAME$ADD_BUTTON_Y_OFFSET = 155;

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
	public int backgroundLeft() {
		throw new AssertionError("Mixin injection failed - Freeze Frame BookEditScreenMixin.");
	}

	@Shadow
	public int backgroundTop() {
		throw new AssertionError("Mixin injection failed - Freeze Frame BookEditScreenMixin.");
	}

	@Shadow
	public abstract void saveChanges();

	@Shadow
	@Final
	public static int TEXT_WIDTH;
	@Unique
	private Button freezeFrame$addPhotoButton;
	@Unique
	private boolean freezeFrame$capturedDefaultPageBox;
	@Unique
	private int freezeFrame$defaultPageY;
	@Unique
	private int freezeFrame$defaultPageHeight;
	@Unique
	private int freezeFrame$defaultPageLineLimit = BookPagePhotographScreen.DEFAULT_TEXT_LINES;
	@Unique
	private boolean freezeFrame$wasPhotoLayout;

	@Inject(method = "init", at = @At("TAIL"))
	private void freezeFrame$initPhotoControls(CallbackInfo info) {
		if (BookPagePhotographUiState.suppressingBookEditorPhotoControls()) return;
		final int addButtonX = this.backgroundLeft() + FREEZE_FRAME$ADD_BUTTON_X_OFFSET;
		final int addButtonY = this.backgroundTop() + FREEZE_FRAME$ADD_BUTTON_Y_OFFSET;
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
		if (BookPagePhotographUiState.suppressingBookEditorPhotoControls()) return;
		this.freezeFrame$applyPhotoTextLayout();
		this.freezeFrame$updateButtonState();
	}

	@Inject(method = "eraseEmptyTrailingPages", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$preservePhotoTrailingPages(CallbackInfo info) {
		int pageIndex = this.pages.size() - 1;
		while (pageIndex >= 0) {
			final boolean hasText = !this.pages.get(pageIndex).isEmpty();
			final boolean hasPhoto = BookPagePhotographHelper.hasPhoto(this.book, pageIndex);
			if (hasText || hasPhoto) break;
			this.pages.remove(pageIndex);
			pageIndex--;
		}
		if (this.pages.isEmpty()) this.pages.add("");
		if (this.currentPage >= this.pages.size()) this.currentPage = this.pages.size() - 1;
		info.cancel();
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void freezeFrame$renderPhotoControls(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
		if (BookPagePhotographUiState.suppressingBookEditorPhotoControls()) return;
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

		final int x = this.backgroundLeft() + FREEZE_FRAME$PHOTOGRAPH_X_OFFSET;
		final int y = this.backgroundTop() + FREEZE_FRAME$PHOTOGRAPH_Y_OFFSET;
		PhotographRenderer.blitForBook(x, y, graphics, photograph.identifier());

		if (this.freezeFrame$isWithin(mouseX, mouseY, x, y, FREEZE_FRAME$PHOTOGRAPH_SIZE, FREEZE_FRAME$PHOTOGRAPH_SIZE)) {
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

	@Unique
	private void freezeFrame$renderAddPhotoButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		final boolean canOpen = this.freezeFrame$canOpenPhotoInventory();
		final int x = this.backgroundLeft() + FREEZE_FRAME$ADD_BUTTON_X_OFFSET;
		final int y = this.backgroundTop() + FREEZE_FRAME$ADD_BUTTON_Y_OFFSET;
		final boolean hovered = this.freezeFrame$isWithin(mouseX, mouseY, x, y, FREEZE_FRAME$ADD_BUTTON_SIZE, FREEZE_FRAME$ADD_BUTTON_SIZE);
		final Identifier sprite = hovered && canOpen ? FREEZE_FRAME$ADD_PHOTOGRAPH_HOVER : FREEZE_FRAME$ADD_PHOTOGRAPH;

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, FREEZE_FRAME$ADD_BUTTON_SIZE, FREEZE_FRAME$ADD_BUTTON_SIZE);

		if (hovered && !canOpen) {
			graphics.setTooltipForNextFrame(
				this.font,
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
		return this.font.wordWrapHeight(Component.literal(pageText), TEXT_WIDTH) <= (BookPagePhotographScreen.PHOTO_TEXT_LINES * 9);
	}

	@Unique
	private void freezeFrame$captureDefaultPageBox() {
		if (this.freezeFrame$capturedDefaultPageBox || this.page == null) return;
		this.freezeFrame$capturedDefaultPageBox = true;
		this.freezeFrame$defaultPageY = this.page.getY();
		this.freezeFrame$defaultPageHeight = this.page.getHeight();
		this.freezeFrame$defaultPageLineLimit = Math.max(1, this.freezeFrame$defaultPageHeight / 9);
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
			final int photoTextHeight = BookPagePhotographScreen.PHOTO_TEXT_LINES * 9;
			this.page.setLineLimit(BookPagePhotographScreen.PHOTO_TEXT_LINES);
			this.page.setHeight(photoTextHeight);
			this.page.setY(this.freezeFrame$defaultPageY + (this.freezeFrame$defaultPageHeight - photoTextHeight) - 9);
			if (layoutChanged) this.page.setValue(this.page.getValue(), true);
			this.freezeFrame$trimCurrentPageToVisibleLineLimit(BookPagePhotographScreen.PHOTO_TEXT_LINES);
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
		final int maxHeight = lines * 9;
		while (!value.isEmpty() && this.font.wordWrapHeight(Component.literal(value), TEXT_WIDTH) > maxHeight) {
			value = value.substring(0, value.length() - 1);
		}

		if (!value.equals(this.page.getValue())) this.page.setValue(value, true);
	}

	@Unique
	private boolean freezeFrame$isWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
