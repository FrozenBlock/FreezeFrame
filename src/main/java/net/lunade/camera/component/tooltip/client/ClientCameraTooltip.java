package net.lunade.camera.component.tooltip.client;

import com.mojang.serialization.DataResult;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.CameraContents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientCameraTooltip implements ClientTooltipComponent {
	private static final Identifier PROGRESSBAR_BORDER_SPRITE = CameraPortConstants.id("container/camera/camera_progressbar_border");
	private static final Identifier PROGRESSBAR_FILL_SPRITE = CameraPortConstants.id("container/camera/camera_progressbar_fill");
	private static final Identifier PROGRESSBAR_FULL_SPRITE = CameraPortConstants.id("container/camera/camera_progressbar_full");
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = CameraPortConstants.id("container/camera/slot_highlight_back");
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = CameraPortConstants.id("container/camera/slot_highlight_front");
	private static final Identifier SLOT_BACKGROUND_SPRITE = CameraPortConstants.id("container/camera/slot_background");
	private static final int SLOT_MARGIN = 4;
	private static final int SLOT_SIZE = 24;
	private static final int GRID_WIDTH = 96;
	private static final int PROGRESSBAR_HEIGHT = 13;
	private static final int PROGRESSBAR_WIDTH = 96;
	private static final int PROGRESSBAR_BORDER = 1;
	private static final int PROGRESSBAR_FILL_MAX = 94;
	private static final Component BUNDLE_FULL_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.full");
	private static final Component BUNDLE_EMPTY_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.empty");
	private static final Component BUNDLE_EMPTY_DESCRIPTION = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.empty.description");
	private final CameraContents contents;

	public ClientCameraTooltip(CameraContents contents) {
		this.contents = contents;
	}

	@Override
	public int getHeight(final Font font) {
		return this.contents.isEmpty() ? getEmptyBundleBackgroundHeight(font) : this.backgroundHeight();
	}

	@Override
	public int getWidth(final Font font) {
		return GRID_WIDTH;
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	private static int getEmptyBundleBackgroundHeight(final Font font) {
		return getEmptyBundleDescriptionTextHeight(font) + 13 + 8;
	}

	private int backgroundHeight() {
		return this.itemGridHeight() + 13 + 8;
	}

	private int itemGridHeight() {
		return this.gridSizeY() * SLOT_SIZE;
	}

	private static int getContentXOffset(final int tooltipWidth) {
		return (tooltipWidth - GRID_WIDTH) / 2;
	}

	private int gridSizeY() {
		return Mth.positiveCeilDiv(this.slotCount(), 4);
	}

	private int slotCount() {
		return Math.min(12, this.contents.size());
	}

	@Override
	public void renderImage(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
		final DataResult<Fraction> weight = this.contents.weight();
		if (weight.isError()) return;

		if (this.contents.isEmpty()) {
			renderEmptyBundleTooltip(font, x, y, w, h, graphics);
		} else {
			this.renderBundleWithItemsTooltip(font, x, y, w, h, graphics, weight.getOrThrow());
		}
	}

	private static void renderEmptyBundleTooltip(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
		final int left = x + getContentXOffset(w);
		drawEmptyBundleDescriptionText(left, y, font, graphics);
		drawProgressbar(left, y + getEmptyBundleDescriptionTextHeight(font) + 4, font, graphics, Fraction.ZERO);
	}

	private void renderBundleWithItemsTooltip(Font font, int x, int y, int w, int h, GuiGraphics graphics, Fraction weight) {
		final boolean isOverflowing = this.contents.size() > 12;
		final List<ItemStackTemplate> shownItems = this.getShownItems(this.contents.getNumberOfItemsToShow());
		final int xStartPos = x + getContentXOffset(w) + GRID_WIDTH;
		final int yStartPos = y + this.gridSizeY() * SLOT_SIZE;
		int slotNumber = 1;

		for (int rowNumber = 1; rowNumber <= this.gridSizeY(); rowNumber++) {
			for (int columnNumber = 1; columnNumber <= 4; columnNumber++) {
				int drawX = xStartPos - columnNumber * SLOT_SIZE;
				int drawY = yStartPos - rowNumber * SLOT_SIZE;
				if (shouldRenderSurplusText(isOverflowing, columnNumber, rowNumber)) {
					renderCount(drawX, drawY, this.getAmountOfHiddenItems(shownItems), font, graphics);
				} else if (shouldRenderItemSlot(shownItems, slotNumber)) {
					this.renderSlot(slotNumber, drawX, drawY, shownItems, slotNumber, font, graphics);
					slotNumber++;
				}
			}
		}

		this.drawSelectedItemTooltip(font, graphics, x, y, w);
		drawProgressbar(x + getContentXOffset(w), y + this.itemGridHeight() + 4, font, graphics, weight);
	}

	private List<ItemStackTemplate> getShownItems(int amountOfItemsToShow) {
		final int lastToDisplay = Math.min(this.contents.size(), amountOfItemsToShow);
		return this.contents.items().subList(0, lastToDisplay);
	}

	private static boolean shouldRenderSurplusText(boolean isOverflowing, int column, int row) {
		return isOverflowing && column * row == 1;
	}

	private static boolean shouldRenderItemSlot(List<? extends ItemInstance> shownItems, int slotNumber) {
		return shownItems.size() >= slotNumber;
	}

	private int getAmountOfHiddenItems(final List<ItemStackTemplate> shownItems) {
		return this.contents.items().stream().skip(shownItems.size()).mapToInt(ItemInstance::count).sum();
	}

	private void renderSlot(int slotNumber, int drawX, int drawY, List<ItemStackTemplate> shownItems, int slotIndex, Font font, GuiGraphics graphics) {
		final int itemVisualOrderIndex = shownItems.size() - slotNumber;
		final boolean hasHighlight = itemVisualOrderIndex == this.contents.getSelectedItemIndex();
		final ItemStack item = shownItems.get(itemVisualOrderIndex).create();
		if (hasHighlight) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
		} else {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_BACKGROUND_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
		}

		graphics.renderItem(item, drawX + SLOT_MARGIN, drawY + SLOT_MARGIN, slotIndex);
		graphics.renderItemDecorations(font, item, drawX + SLOT_MARGIN, drawY + SLOT_MARGIN);
		if (hasHighlight) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, drawX, drawY, SLOT_SIZE, SLOT_SIZE);
	}

	private static void renderCount(int drawX, int drawY, int hiddenItemCount, Font font, GuiGraphics graphics) {
		graphics.drawCenteredString(font, "+" + hiddenItemCount, drawX + 12, drawY + 10, -1);
	}

	private void drawSelectedItemTooltip(Font font, GuiGraphics graphics, int x, int y, int w) {
		final ItemStackTemplate selectedItem = this.contents.getSelectedItem();
		if (selectedItem == null) return;

		final ItemStack itemStack = selectedItem.create();
		final Component selectedItemName = itemStack.getStyledHoverName();
		final int textWidth = font.width(selectedItemName.getVisualOrderText());
		final int centerTooltip = x + w / 2 - 12;
		final ClientTooltipComponent selectedItemNameTooltip = ClientTooltipComponent.create(selectedItemName.getVisualOrderText());
		graphics.renderTooltip(
			font,
			List.of(selectedItemNameTooltip),
			centerTooltip - textWidth / 2,
			y - 15,
			DefaultTooltipPositioner.INSTANCE,
			itemStack.get(DataComponents.TOOLTIP_STYLE)
		);
	}

	private static void drawProgressbar(int x, int y, Font font, GuiGraphics graphics, Fraction weight) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getProgressBarTexture(weight), x + PROGRESSBAR_BORDER, y, getProgressBarFill(weight), PROGRESSBAR_HEIGHT);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, PROGRESSBAR_WIDTH, PROGRESSBAR_HEIGHT);
		final Component progressBarFillText = getProgressBarFillText(weight);
		if (progressBarFillText != null) graphics.drawCenteredString(font, progressBarFillText, x + 48, y + 3, -1);
	}

	private static void drawEmptyBundleDescriptionText(int x, int y, Font font, GuiGraphics graphics) {
		graphics.drawWordWrap(font, BUNDLE_EMPTY_DESCRIPTION, x, y, GRID_WIDTH, -5592406);
	}

	private static int getEmptyBundleDescriptionTextHeight(Font font) {
		return font.split(BUNDLE_EMPTY_DESCRIPTION, GRID_WIDTH).size() * 9;
	}

	private static int getProgressBarFill(Fraction weight) {
		return Mth.clamp(Mth.mulAndTruncate(weight, PROGRESSBAR_FILL_MAX), 0, PROGRESSBAR_FILL_MAX);
	}

	private static Identifier getProgressBarTexture(final Fraction weight) {
		return weight.compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
	}

	@Nullable
	private static Component getProgressBarFillText(final Fraction weight) {
		if (weight.compareTo(Fraction.ZERO) == 0) return BUNDLE_EMPTY_TEXT;
		return weight.compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL_TEXT : null;
	}
}
