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
import net.frozenblock.freezeframe.client.gui.components.inventory.FilmPhotographButton;
import net.frozenblock.freezeframe.client.photograph.PhotographDetailsUtil;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.inventory.IconRadioButton;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.narration.GroupNarration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

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
	private static final int FILM_PHOTOGRAPH_SIZE = 78;

	// NAME EDIT BOX
	private static final int NAME_LABEL_X = 17;
	private static final int NAME_LABEL_Y = 111;
	private static final int NAME_BOX_X = 88;
	private static final int NAME_BOX_Y = 5;
	private static final int NAME_BOX_WIDTH = 80;
	private static final int NAME_BOX_HEIGHT = 16;
	private static final int NAME_TEXT_INSET_X = 4;
	private static final int NAME_TEXT_INSET_Y = 3;
	private static final int NAME_TEXT_OFFSET_Y = 1;
	private static final int NAME_TEXT_WIDTH = NAME_BOX_WIDTH - (NAME_TEXT_INSET_X * 2);
	private static final int NAME_TEXT_HEIGHT = NAME_BOX_HEIGHT - (NAME_TEXT_INSET_Y * 2);
	private static final int MAX_NAME_LENGTH = 16;

	protected int leftPos;
	protected int topPos;
	private final Player owner;
	private final InteractionHand hand;
	private @Nullable HorizontalScrollableLayout photographArea;
	private double photographScrollAmount;
	private int selectedPhotographIndex;


	private EditBox nameEditBox;

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

		this.nameEditBox = new EditBox(
			this.font,
			this.leftPos + NAME_BOX_X + NAME_TEXT_INSET_X,
			this.topPos + NAME_BOX_Y + NAME_TEXT_INSET_Y + NAME_TEXT_OFFSET_Y,
			NAME_TEXT_WIDTH,
			NAME_TEXT_HEIGHT,
			Component.translatable("screen.freezeframe.film.name")
		);
		this.nameEditBox.setMaxLength(MAX_NAME_LENGTH);
		this.nameEditBox.setCanLoseFocus(false);
		this.nameEditBox.setBordered(false);
		this.nameEditBox.setTextColor(0xFFFFFFFF);
		this.nameEditBox.setTextColorUneditable(0xFFFFFFFF);
		this.nameEditBox.setResponder(this::onNameChanged);
		this.addRenderableWidget(this.nameEditBox);

		this.photographArea = this.createScrollableLayout(new FrameLayout());
	}

	private void initPhotographs() {
		if (this.photographArea != null) {
			this.photographScrollAmount = this.photographArea.getScrollAmount();
			this.photographArea.visitWidgets(this::removeWidget);
		}

		final ItemStack filmStack = this.owner.getItemInHand(this.hand);
		final FilmContents filmContents = filmStack.isEmpty()
			? FilmContents.EMPTY
			: filmStack.getOrDefault(FFDataComponents.FILM_CONTENTS.get(), FilmContents.EMPTY);
		if (filmContents.isEmpty()) {
			this.photographArea = this.createScrollableLayout(new FrameLayout());
			return;
		}

		final GridLayout layout = new GridLayout();
		for (int index = 0; index < filmContents.size(); index++) {
			final int currentIndex = index;
			final Photograph photograph = filmContents.getPhotographAtIndex(index);

			// TODO: message?????
			final Component message = Component.literal(photograph.name());
			final FilmPhotographButton photographButton = new FilmPhotographButton(
				FILM_PHOTOGRAPH_SIZE,
				message,
				button -> {
					// TODO: how on earth are we going to influence scrolling with this
				},
				Supplier::get,
				photograph.id(),
				() -> new Tooltip(
					CommonComponents.joinLines(PhotographDetailsUtil.buildTooltipLines(photograph)),
					// TODO: narration
					null,
					Optional.empty(),
					null,
					false,
					true
				),
				() -> this.selectedPhotographIndex == currentIndex,
				() -> this.saveButton.isFocused() && this.isPhotographModified(currentIndex),
				() -> this.deleteButton.isFocused() && this.selectedPhotographIndex == currentIndex
			);
			layout.addChild(photographButton, 1, currentIndex);
		}

		this.photographArea = this.createScrollableLayout(layout);
		this.photographArea.setScrollAmount(this.photographScrollAmount);
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
