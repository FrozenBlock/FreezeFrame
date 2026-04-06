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

package net.frozenblock.freezeframe.client.gui.screens.inventory;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.BookPagePhotographUiState;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.menu.BookPagePhotographMenu;
import net.frozenblock.freezeframe.mixin.client.book.BookEditScreenAccessor;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.util.BookPagePhotographHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;

@Environment(EnvType.CLIENT)
public class BookPagePhotographScreen extends AbstractContainerScreen<BookPagePhotographMenu> {
	private static final Identifier INVENTORY_TEXTURE = FFConstants.id("textures/gui/container/written_book.png");
	private static final Identifier PHOTO_FRAME_SPRITE = FFConstants.id("container/written_book/photograph");
	private static final Identifier PHOTO_HIGHLIGHT_SPRITE = FFConstants.id("container/written_book/photograph_highlight");
	private static final int IMAGE_WIDTH = 172;
	private static final int IMAGE_HEIGHT = 140;
	private static final int PHOTO_PREVIEW_SIZE = 84;
	private static final int PHOTO_PREVIEW_X_OFFSET = 51;
	private static final int PHOTO_PREVIEW_Y_OFFSET = 30;
	private static final int INVENTORY_BG_X_OFFSET = -1;
	private static final int INVENTORY_BG_Y = 48;
	private BookEditScreen freezeFrame$bookPreview;

	public BookPagePhotographScreen(BookPagePhotographMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
		this.titleLabelX = -1000;
		this.inventoryLabelX = -1000;
	}

	@Override
	protected void init() {
		super.init();
		BookPagePhotographUiState.setSuppressBookEditorPhotoControls(true);
		this.freezeFrame$bookPreview = this.freezeFrame$createBookPreview();
		this.addRenderableWidget(
			Button.builder(Component.translatable("screen.camera_port.book_photograph.close"), button -> this.onClose())
				.bounds(this.leftPos + this.imageWidth - 54, this.topPos + 5, 48, 20)
				.build()
		);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (this.freezeFrame$bookPreview != null) {
			BookPagePhotographUiState.setSuppressBookEditorPhotoControls(true);
			this.freezeFrame$bookPreview.extractBackground(graphics, mouseX, mouseY, partialTicks);
			this.freezeFrame$bookPreview.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		}
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			INVENTORY_TEXTURE,
			this.leftPos + INVENTORY_BG_X_OFFSET,
			this.topPos + INVENTORY_BG_Y,
			0F,
			0F,
			172,
			86,
			256,
			256
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		this.freezeFrame$renderSlotPhotograph(graphics, mouseX, mouseY);
	}

	@Override
	protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
		final Slot photoSlot = this.menu.getSlot(BookPagePhotographMenu.PHOTO_SLOT);
		if (x == photoSlot.x && y == photoSlot.y) {
			return mouseX >= this.leftPos + PHOTO_PREVIEW_X_OFFSET
				&& mouseX < this.leftPos + PHOTO_PREVIEW_X_OFFSET + PHOTO_PREVIEW_SIZE
				&& mouseY >= this.topPos + PHOTO_PREVIEW_Y_OFFSET
				&& mouseY < this.topPos + PHOTO_PREVIEW_Y_OFFSET + PHOTO_PREVIEW_SIZE;
		}
		return super.isHovering(x, y, width, height, mouseX, mouseY);
	}

	@Override
	public void onClose() {
		this.freezeFrame$applyLocalBookPhotoUpdate();
		BookPagePhotographUiState.setSuppressBookEditorPhotoControls(false);
		super.onClose();
		this.freezeFrame$reopenBookEditor();
	}

	@Override
	public void removed() {
		BookPagePhotographUiState.setSuppressBookEditorPhotoControls(false);
		super.removed();
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.minecraft != null && this.minecraft.options.keyDrop.matches(event)) return true;
		return super.keyPressed(event);
	}

	private BookEditScreen freezeFrame$createBookPreview() {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return null;
		final ItemStack book = minecraft.player.getItemInHand(this.menu.getHand());
		if (!book.is(Items.WRITABLE_BOOK)) return null;
		WritableBookContent content = this.freezeFrame$ensurePageExists(book, this.freezeFrame$targetPageIndex());
		if (content == null) return null;

		final BookEditScreen screen = new BookEditScreen(minecraft.player, book, this.menu.getHand(), content);
		((BookEditScreenAccessor) (Object) screen).freezeFrame$setCurrentPage(this.freezeFrame$targetPageIndex());
		screen.init(this.width, this.height);
		((BookEditScreenAccessor) screen).freezeFrame$invokeUpdatePageContent();
		for (var child : screen.children()) {
			if (child instanceof AbstractWidget widget) {
				widget.active = false;
				widget.visible = false;
			}
		}
		return screen;
	}

	private void freezeFrame$reopenBookEditor() {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;
		final ItemStack book = minecraft.player.getItemInHand(this.menu.getHand());
		if (!book.is(Items.WRITABLE_BOOK)) return;
		final WritableBookContent content = this.freezeFrame$ensurePageExists(book, this.freezeFrame$targetPageIndex());
		if (content == null) return;

		final BookEditScreen screen = new BookEditScreen(minecraft.player, book, this.menu.getHand(), content);
		((BookEditScreenAccessor) screen).freezeFrame$setCurrentPage(this.freezeFrame$targetPageIndex());
		minecraft.setScreen(screen);
	}

	private void freezeFrame$applyLocalBookPhotoUpdate() {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;
		final ItemStack book = minecraft.player.getItemInHand(this.menu.getHand());
		if (!book.is(Items.WRITABLE_BOOK)) return;

		this.freezeFrame$ensurePageExists(book, this.freezeFrame$targetPageIndex());
		final ItemStack slot = this.menu.getSlot(BookPagePhotographMenu.PHOTO_SLOT).getItem();
		if (slot.is(FFItems.PHOTOGRAPH)) {
			BookPagePhotographHelper.setPhoto(book, this.freezeFrame$targetPageIndex(), slot.copyWithCount(1));
		} else {
			BookPagePhotographHelper.clearPhoto(book, this.freezeFrame$targetPageIndex());
		}
	}

	private int freezeFrame$targetPageIndex() {
		final int fallback = this.menu.getPageIndex();
		return BookPagePhotographUiState.resolveRequestedPage(this.menu.getHand(), fallback);
	}

	private WritableBookContent freezeFrame$ensurePageExists(ItemStack book, int pageIndex) {
		final WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
		if (content == null) return null;
		if (pageIndex < 0) return content;

		final List<Filterable<String>> pages = new ArrayList<>(content.pages());
		if (pageIndex < pages.size()) return content;
		while (pages.size() <= pageIndex) pages.add(Filterable.passThrough(""));

		final WritableBookContent updated = content.withReplacedPages(pages);
		book.set(DataComponents.WRITABLE_BOOK_CONTENT, updated);
		return updated;
	}

	private void freezeFrame$renderSlotPhotograph(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		final int x = this.leftPos + PHOTO_PREVIEW_X_OFFSET;
		final int y = this.topPos + PHOTO_PREVIEW_Y_OFFSET;
		final ItemStack slot = this.menu.getSlot(BookPagePhotographMenu.PHOTO_SLOT).getItem();
		boolean rendered = false;
		if (slot.is(FFItems.PHOTOGRAPH)) {
			final Photograph photograph = slot.get(FFDataComponents.PHOTOGRAPH);
			if (photograph != null) {
				PhotographRenderer.blit(0, 0, x, y, graphics, photograph.identifier(), PHOTO_PREVIEW_SIZE, PhotographRenderer.FrameType.NONE);
				rendered = true;
			}
		}
		if (rendered) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PHOTO_FRAME_SPRITE, x, y, PHOTO_PREVIEW_SIZE, PHOTO_PREVIEW_SIZE);
		}
		if (this.freezeFrame$isWithin(mouseX, mouseY, x, y, PHOTO_PREVIEW_SIZE, PHOTO_PREVIEW_SIZE)) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PHOTO_HIGHLIGHT_SPRITE, x, y, PHOTO_PREVIEW_SIZE, PHOTO_PREVIEW_SIZE);
		}
		if (this.freezeFrame$isWithin(mouseX, mouseY, x, y, PHOTO_PREVIEW_SIZE, PHOTO_PREVIEW_SIZE) && !slot.isEmpty()) {
			graphics.setTooltipForNextFrame(this.font, slot, mouseX, mouseY);
		}
	}

	private boolean freezeFrame$isWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
