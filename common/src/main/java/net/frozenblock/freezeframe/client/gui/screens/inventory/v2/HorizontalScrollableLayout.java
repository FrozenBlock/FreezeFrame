package net.frozenblock.freezeframe.client.gui.screens.inventory.v2;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.GroupNarration;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@ClientOnly
public class HorizontalScrollableLayout implements Layout {
	private static final int DEFAULT_SCROLLBAR_SPACING = ScrollableLayout.DEFAULT_SCROLLBAR_SPACING;
	private final Layout content;
	private final Container container;
	private final ReserveStrategy reserveStrategy;
	private final @Nullable GroupNarration groupNarration;
	private int scrollbarSpacing;
	private int minWidth;
	private int maxWidth;
	private int minHeight;
	private float xAlignment;

	public HorizontalScrollableLayout(Minecraft minecraft, Layout content, int maxWidth) {
		this(
			minecraft,
			content,
			maxWidth,
			AbstractHorizontalScrollArea.defaultSettings(10),
			4,
			ReserveStrategy.BOTH,
			null
		);
	}

	public HorizontalScrollableLayout(
		Minecraft minecraft,
		Layout content,
		int maxWidth,
		AbstractHorizontalScrollArea.ScrollbarSettings scrollbarSettings,
		int scrollbarSpacing,
		ReserveStrategy reserveStrategy,
		@Nullable GroupNarration groupNarration
	) {
		this.content = content;
		this.maxWidth = maxWidth;
		this.reserveStrategy = reserveStrategy;
		this.groupNarration = groupNarration;
		this.scrollbarSpacing = scrollbarSpacing;
		this.container = new Container(minecraft, maxWidth, 0, scrollbarSettings);
	}

	public void setScrollbarSpacing(int scrollbarSpacing) {
		this.scrollbarSpacing = scrollbarSpacing;
	}

	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
		this.container.setWidth(Math.max(this.content.getWidth(), minWidth));
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
		this.container.setWidth(Math.min(this.content.getWidth(), maxWidth));
		this.container.refreshScrollAmount();
	}

	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
		this.container.setHeight(Math.max(this.content.getHeight(), minHeight));
	}

	public void alignHorizontally(float xAlignment) {
		this.xAlignment = xAlignment;
	}

	public void alignHorizontallyLeft() {
		this.alignHorizontally(0F);
	}

	public void alignHorizontallyMiddle() {
		this.alignHorizontally(0.5F);
	}

	public void alignHorizontallyRight() {
		this.alignHorizontally(1F);
	}

	@Override
	public void arrangeElements() {
		this.content.arrangeElements();

		final int contentHeight = this.content.getHeight();
		final int scrollbarReserve = switch (this.reserveStrategy) {
			case BELOW -> this.container.scrollbarReserve();
			case BOTH -> 2 * this.container.scrollbarReserve();
		};
		this.container.setWidth(Math.clamp(this.container.getWidth(), this.minWidth, this.maxWidth));
		this.container.setHeight(Math.max(contentHeight, this.minHeight) + scrollbarReserve);

		this.container.refreshChildren();
		this.container.refreshScrollAmount();
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> layoutElementVisitor) {
		layoutElementVisitor.accept(this.container);
	}

	@Override
	public void removeChildren() {
		this.container.children().clear();
		this.content.removeChildren();
	}

	@Override
	public void setX(int x) {
		this.container.setX(x);
	}

	@Override
	public void setY(int y) {
		this.container.setY(y);
	}

	@Override
	public int getX() {
		return this.container.getX();
	}

	@Override
	public int getY() {
		return this.container.getY();
	}

	@Override
	public int getWidth() {
		return this.container.getWidth();
	}

	@Override
	public int getHeight() {
		return this.container.getHeight();
	}

	public double getScrollAmount() {
		return this.container.scrollAmount();
	}

	public void setScrollAmount(double scrollAmount) {
		this.container.setScrollAmount(scrollAmount);
	}

	public void setTabGroup(int tabGroup) {
		this.container.setTabOrderGroup(tabGroup);
		this.container.children.forEach(child -> child.setTabOrderGroup(tabGroup));
	}

	public void allowTabNavigationInside(boolean allowTabNavigationInside) {
		this.container.allowTabNavigationInside(allowTabNavigationInside);
	}

	private class Container extends AbstractHorizontalContainerWidget {
		private final Minecraft minecraft;
		private final List<AbstractWidget> children = new ArrayList<>();

		public Container(Minecraft minecraft, int width, int height, ScrollbarSettings scrollbarSettings) {
			super(0, 0, width, height, CommonComponents.EMPTY, scrollbarSettings);
			this.minecraft = minecraft;
			this.refreshChildren();
		}

		public void refreshChildren() {
			this.children.clear();
			HorizontalScrollableLayout.this.content.visitWidgets(this.children::add);
		}

		@Override
		protected int contentWidth() {
			return HorizontalScrollableLayout.this.content.getWidth();
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics) {
			graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height);

			for (AbstractWidget child : this.children) {
				if (child.getBottom() >= this.getY() && child.getY() <= this.getBottom()) child.extractRenderState(graphics);
			}

			graphics.disableScissor();
			this.extractScrollbar(graphics);
		}

		@Override
		public NarratableEntry.NarrationPriority narrationPriority() {
			return NarratableEntry.NarrationPriority.of(this.isFocused(), this.getHoveredChild() != null);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			if (HorizontalScrollableLayout.this.groupNarration == null) return;

			final AbstractWidget narrated = getNarratedChild(output.narrationTrigger(), this.getHoveredChild(), this.getFocusedChild());
			if (narrated != null) {
				narrated.updateNarration(output.nest());
				HorizontalScrollableLayout.this.groupNarration.narratePosition(output, this.children, narrated);
			}

			if (this.isFocused()) HorizontalScrollableLayout.this.groupNarration.narrateUsage(output, this.children);
		}

		private @Nullable AbstractWidget getHoveredChild() {
			for (AbstractWidget child : this.children) {
				if (child.isHovered()) return child;
			}

			return null;
		}

		private @Nullable AbstractWidget getFocusedChild() {
			return this.getFocused() instanceof AbstractWidget widget ? widget : null;
		}

		@Override
		public ScreenRectangle getBorderForArrowNavigation(ScreenDirection opposite) {
			final GuiEventListener focused = this.getFocused();
			return focused != null
				? focused.getBorderForArrowNavigation(opposite)
				: new ScreenRectangle(this.getX(), this.getY(), this.contentWidth(), this.height).getBorder(opposite);
		}

		@Override
		public void setFocused(@Nullable GuiEventListener focused) {
			super.setFocused(focused);

			if (focused == null || !this.minecraft.getLastInputType().isKeyboard()) return;

			final ScreenRectangle area = this.getRectangle();
			final ScreenRectangle focusedRect = focused.getRectangle();
			final int leftDelta = focusedRect.left() - area.left();
			final int rightDelta = focusedRect.right() - area.right();
			final double scrollRate = this.scrollRate();
			if (leftDelta < 0) {
				this.setScrollAmount(this.scrollAmount() + leftDelta - scrollRate);
			} else if (rightDelta > 0) {
				this.setScrollAmount(this.scrollAmount() + rightDelta + scrollRate);
			}
		}

		private int horizontalOffset() {
			final int remainingWidth = this.width - this.contentWidth();
			return Math.round(HorizontalScrollableLayout.this.xAlignment * remainingWidth) - (int)this.scrollAmount();
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			HorizontalScrollableLayout.this.content.setX(x + this.horizontalOffset());
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			HorizontalScrollableLayout.this.content
				.setY(y + (HorizontalScrollableLayout.this.reserveStrategy == ReserveStrategy.BOTH ? this.scrollbarReserve() : 0));
		}

		private int scrollbarReserve() {
			return HorizontalScrollableLayout.this.scrollbarSpacing + this.scrollbarHeight();
		}

		@Override
		public void setScrollAmount(double scrollAmount) {
			super.setScrollAmount(scrollAmount);
			HorizontalScrollableLayout.this.content.setX(this.getRectangle().left() + this.horizontalOffset());
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.children;
		}

		@Override
		public Collection<? extends NarratableEntry> getNarratables() {
			return HorizontalScrollableLayout.this.groupNarration != null ? List.of(this) : this.children;
		}
	}

	public enum ReserveStrategy {
		BELOW,
		BOTH;
	}
}
