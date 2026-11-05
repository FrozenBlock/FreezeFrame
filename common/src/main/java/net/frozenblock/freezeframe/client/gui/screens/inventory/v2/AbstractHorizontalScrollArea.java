package net.frozenblock.freezeframe.client.gui.screens.inventory.v2;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@ClientOnly
public abstract class AbstractHorizontalScrollArea extends AbstractWidget {
	public static final int SCROLLBAR_HEIGHT = 6;
	private static final int SCROLLBAR_MIN_WIDTH = 32;
	private static final Identifier SCROLLER_SPRITE = AbstractScrollArea.SCROLLER_SPRITE;
	private static final Identifier SCROLLER_BACKGROUND_SPRITE = AbstractScrollArea.SCROLLER_BACKGROUND_SPRITE;
	private final ScrollbarSettings scrollbarSettings;
	private double scrollAmount;
	private boolean scrolling;

	public AbstractHorizontalScrollArea(int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
		super(x, y, width, height, message);
		this.scrollbarSettings = scrollbarSettings;
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
		if (!this.isVisible()) return false;

		this.setScrollAmount(this.scrollAmount() - scrollY * this.scrollRate());
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (!this.scrolling) return super.mouseDragged(event, dx, dy);

		if (event.y() < this.getY()) {
			this.setScrollAmount(0D);
		} else if (event.y() > this.getBottom()) {
			this.setScrollAmount(this.maxScrollAmount());
		} else {
			final double max = Math.max(1D, this.maxScrollAmount());
			final int barWidth = this.scrollerWidth();
			final double xDragScale = max / Math.max(1, this.width - barWidth);
			this.setScrollAmount(this.scrollAmount() + (dx * xDragScale));
		}

		return true;
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		this.scrolling = false;
	}

	public double scrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double scrollAmount) {
		this.scrollAmount = Math.clamp(scrollAmount, 0D, this.maxScrollAmount());
	}

	public boolean updateScrolling(MouseButtonEvent event) {
		this.scrolling = this.scrollable() && this.isValidClickButton(event.buttonInfo()) && this.isOverScrollbar(event.x(), event.y());
		return this.scrolling;
	}

	protected boolean isOverScrollbar(double x, double y) {
		return (x >= this.getX() && x < this.getRight())
			&& (y >= this.scrollBarY() && y <= this.scrollBarY() + this.scrollbarHeight());
	}

	public void refreshScrollAmount() {
		this.setScrollAmount(this.scrollAmount);
	}

	public int maxScrollAmount() {
		return Math.max(0, this.contentWidth() - this.width);
	}

	protected boolean scrollable() {
		return this.maxScrollAmount() > 0;
	}

	public int scrollbarMinWidth() {
		return this.scrollbarSettings.scrollbarMinWidth();
	}

	public int scrollbarHeight() {
		return this.scrollbarSettings.scrollbarHeight();
	}

	protected int scrollerWidth() {
		return this.scrollbarSettings.resizingScrollbar()
			? Math.clamp((int)((float)(this.width * this.width) / this.contentWidth()), this.scrollbarMinWidth(), Math.max(this.width - 8, SCROLLBAR_MIN_WIDTH))
			: this.scrollbarMinWidth();
	}

	protected int scrollBarX() {
		return this.maxScrollAmount() == 0
			? this.getX()
			: Math.max(this.getX(), (int)this.scrollAmount * (this.width - this.scrollerWidth()) / this.maxScrollAmount() + this.getX());
	}

	public int scrollBarY() {
		return this.getY() - this.scrollbarHeight();
	}

	protected void extractScrollbar(GuiGraphicsExtractor graphics) {
		final int scrollerWidth = this.scrollerWidth();
		final int scrollbarX = this.scrollBarX();
		final int scrollbarY = this.scrollBarY();

		if (!this.scrollable() && this.scrollbarSettings.disabledScrollerSprite() != null) {
			this.extractBackground(graphics, scrollbarY);

			graphics.blitSprite(
				GuiPipeline.TEXTURED,
				this.scrollbarSettings.disabledScrollerSprite(),
				this.getX(),
				scrollbarY,
				scrollerWidth,
				this.scrollbarHeight()
			);

			if (this.isOverScrollbar(graphics.mouseX(), graphics.mouseY())) graphics.requestCursor(CursorTypes.NOT_ALLOWED);
		}

		if (this.scrollable()) {
			this.extractBackground(graphics, scrollbarY);

			graphics.blitSprite(
				GuiPipeline.TEXTURED,
				this.scrollbarSettings.scrollerSprite(),
				scrollbarX,
				scrollbarY,
				scrollerWidth,
				this.scrollbarHeight()
			);

			if (this.isOverScrollbar(graphics.mouseX(), graphics.mouseY())) graphics.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
		}
	}

	private void extractBackground(GuiGraphicsExtractor graphics, int scrollbarY) {
		if (this.scrollbarSettings.backgroundSprite() == null) return;

		graphics.blitSprite(
			GuiPipeline.TEXTURED,
			this.scrollbarSettings.backgroundSprite(),
			this.getX(),
			scrollbarY,
			this.getWidth(),
			this.scrollbarHeight()
		);
	}

	protected abstract int contentWidth();

	protected double scrollRate() {
		return this.scrollbarSettings.scrollRate();
	}

	public static ScrollbarSettings defaultSettings(int scrollRate) {
		return new ScrollbarSettings(SCROLLER_SPRITE, null, SCROLLER_BACKGROUND_SPRITE, SCROLLBAR_MIN_WIDTH, SCROLLBAR_HEIGHT, scrollRate, true);
	}

	public record ScrollbarSettings(
		Identifier scrollerSprite,
		@Nullable Identifier disabledScrollerSprite,
		@Nullable Identifier backgroundSprite,
		int scrollbarMinWidth,
		int scrollbarHeight,
		int scrollRate,
		boolean resizingScrollbar
	) {
		public static final ScrollbarSettings NO_SCROLL = defaultSettings(0);
	}
}
