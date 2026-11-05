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

import net.frozenblock.freezeframe.FFConstants;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.narration.GroupNarration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@ClientOnly
public class NewFilmScreen extends Screen {
	private static final Identifier TEXTURE = FFConstants.id("textures/gui/container/film.png");
	private static final Identifier SCROLLER_SPRITE = FFConstants.id("container/film/scroller");
	private static final Identifier SCROLLER_DISABLED_SPRITE = FFConstants.id("container/film/scroller_disabled");
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;
	private static final int BACKGROUND_WIDTH = 256;
	private static final int BACKGROUND_HEIGHT = 128;
	private static final int CONTENT_HEIGHT = 160;

	protected int leftPos;
	protected int topPos;
	private final Player owner;
	private final InteractionHand hand;
	private @Nullable HorizontalScrollableLayout photographArea;
	private double photographScrollAmount;

	public NewFilmScreen(Player owner, InteractionHand hand) {
		super(Component.translatable("screen.freezeframe.film.title"));
		this.owner = owner;
		this.hand = hand;
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
		this.topPos = (this.height - CONTENT_HEIGHT) / 2;

		this.photographArea = this.createScrollableLayout(new FrameLayout());
	}

	private void initPhotographSelection(ItemStack filmStack) {

	}

	// TODO: this is copied from the loom
	private HorizontalScrollableLayout createScrollableLayout(Layout layout) {
		final HorizontalScrollableLayout scrollableLayout = new HorizontalScrollableLayout(
			this.minecraft,
			layout,
			56,
			new AbstractHorizontalScrollArea.ScrollbarSettings(
				SCROLLER_SPRITE,
				SCROLLER_DISABLED_SPRITE,
				null,
				12,
				15,
				14,
				false
			),
			3,
			HorizontalScrollableLayout.ReserveStrategy.BELOW,
			GroupNarration.grid(4, "narration.group_navigation.usage.pattern")
		);
		scrollableLayout.setPosition(this.leftPos + 60, this.topPos + 13);
		scrollableLayout.setMinWidth(56);
		scrollableLayout.setMinHeight(56);
		scrollableLayout.arrangeElements();
		scrollableLayout.visitWidgets(this::addRenderableWidget);
		scrollableLayout.setTabGroup(7);
		scrollableLayout.allowTabNavigationInside(false);
		return scrollableLayout;
	}
}
