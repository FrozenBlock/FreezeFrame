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

package net.frozenblock.freezeframe.client.gui.screens.inventory.v2;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationTrigger;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@ClientOnly
public abstract class AbstractHorizontalContainerWidget extends AbstractHorizontalScrollArea implements ContainerEventHandler {
	private @Nullable GuiEventListener focused;
	private boolean isDragging;
	private boolean allowTabNavigationInside = true;

	public AbstractHorizontalContainerWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message, ScrollbarSettings.NO_SCROLL);
	}

	public AbstractHorizontalContainerWidget(int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
		super(x, y, width, height, message, scrollbarSettings);
	}

	@Override
	public final boolean isDragging() {
		return this.isDragging;
	}

	@Override
	public final void setDragging(boolean dragging) {
		this.isDragging = dragging;
	}

	@Override
	public @Nullable GuiEventListener getFocused() {
		return this.focused;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener focused) {
		if (this.focused != null) this.focused.setFocused(false);
		if (focused != null) focused.setFocused(true);
		this.focused = focused;
	}

	@Override
	public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
		if (!this.allowTabNavigationInside && navigationEvent instanceof FocusNavigationEvent.TabNavigation) {
			return this.isFocused()
				? null
				: ContainerEventHandler.super.nextFocusPath(new FocusNavigationEvent.TabNavigation(true));
		}

		return ContainerEventHandler.super.nextFocusPath(navigationEvent);
	}

	protected void allowTabNavigationInside(boolean allowTabNavigationInside) {
		this.allowTabNavigationInside = allowTabNavigationInside;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		final boolean scrolling = this.updateScrolling(event);
		return ContainerEventHandler.super.mouseClicked(event, doubleClick) || scrolling;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		super.mouseReleased(event);
		return ContainerEventHandler.super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		super.mouseDragged(event, dx, dy);
		return ContainerEventHandler.super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean isFocused() {
		return ContainerEventHandler.super.isFocused();
	}

	@Override
	public void setFocused(boolean focused) {
		ContainerEventHandler.super.setFocused(focused);
	}

	protected static <T> @Nullable T getNarratedChild(NarrationTrigger narrationTrigger, @Nullable T hovered, @Nullable T focused) {
		return narrationTrigger != NarrationTrigger.KEYBOARD && hovered != null ? hovered : focused;
	}
}
