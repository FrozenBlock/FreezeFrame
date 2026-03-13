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

package net.lunade.camera.component.tooltip.client;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.item.FilmItem;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientCameraTooltip implements ClientTooltipComponent {
	private static final Identifier PROGRESSBAR_BORDER_SPRITE = CameraPortConstants.id("container/camera/camera_progressbar_border");
	private static final Identifier PROGRESSBAR_FULL_SPRITE = CameraPortConstants.id("container/camera/camera_progressbar_full");
	private static final int GRID_WIDTH = 96;
	private static final int PROGRESSBAR_HEIGHT = 13;
	private static final int PROGRESSBAR_WIDTH = 96;
	private static final int PROGRESSBAR_BORDER = 1;
	private static final int PROGRESSBAR_FILL_MAX = 94;
	private static final int PROGRESSBAR_FILL_COLOR = 0xFFB8895F;
	private static final Component CAMERA_FULL_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.full");
	private static final Component CAMERA_EMPTY_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.empty");
	private static final Component CAMERA_FILM_EMPTY_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.film.empty");
	private static final Component CAMERA_EMPTY_DESCRIPTION = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.empty.description");
	private static final Component CAMERA_CONTAINS_DESCRIPTION = Component.translatable("item." + CameraPortConstants.MOD_ID + ".camera.contains.description");
	private final CameraContents contents;

	public ClientCameraTooltip(CameraContents contents) {
		this.contents = contents;
	}

	@Override
	public int getHeight(final Font font) {
		return this.contents.isEmpty() ? getEmptyBundleBackgroundHeight(font) : this.backgroundHeight(font);
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

	private int backgroundHeight(final Font font) {
		return getContainsDescriptionTextHeight(font) + PROGRESSBAR_HEIGHT + 8;
	}

	private static int getContentXOffset(final int tooltipWidth) {
		return (tooltipWidth - GRID_WIDTH) / 2;
	}

	@Override
	public void renderImage(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
		final Fraction weight = this.getInsertedFilmWeight();

		if (this.contents.isEmpty()) {
			renderEmptyBundleTooltip(font, x, y, w, h, graphics);
		} else {
			this.renderBundleWithItemsTooltip(font, x, y, w, h, graphics, weight);
		}
	}

	private Fraction getInsertedFilmWeight() {
		if (this.contents.isEmpty()) return Fraction.ZERO;

		final ItemStackTemplate filmStack = this.contents.items().get(0);
		final FilmContents filmContents = Objects.requireNonNullElse(filmStack.get(CameraPortDataComponents.FILM_CONTENTS), FilmContents.EMPTY);
		return FilmItem.getWeightSafe(filmContents);
	}

	private static void renderEmptyBundleTooltip(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
		final int left = x + getContentXOffset(w);
		drawEmptyBundleDescriptionText(left, y, font, graphics);
		drawProgressbar(left, y + getEmptyBundleDescriptionTextHeight(font) + 4, font, graphics, Fraction.ZERO, CAMERA_EMPTY_TEXT);
	}

	private void renderBundleWithItemsTooltip(Font font, int x, int y, int w, int h, GuiGraphics graphics, Fraction weight) {
		final int left = x + getContentXOffset(w);
		drawContainsDescriptionText(left, y, font, graphics);
		drawProgressbar(left, y + getContainsDescriptionTextHeight(font) + 4, font, graphics, weight, CAMERA_FILM_EMPTY_TEXT);
	}

	private static void drawProgressbar(int x, int y, Font font, GuiGraphics graphics, Fraction weight, Component emptyText) {
		final int progressBarFill = getProgressBarFill(weight);
		if (weight.compareTo(Fraction.ONE) >= 0) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_FULL_SPRITE, x + PROGRESSBAR_BORDER, y, progressBarFill, PROGRESSBAR_HEIGHT);
		} else if (progressBarFill > 0) {
			graphics.fill(x + PROGRESSBAR_BORDER, y, x + PROGRESSBAR_BORDER + progressBarFill, y + PROGRESSBAR_HEIGHT, PROGRESSBAR_FILL_COLOR);
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, PROGRESSBAR_WIDTH, PROGRESSBAR_HEIGHT);
		final Component progressBarFillText = getProgressBarFillText(weight, emptyText);
		if (progressBarFillText != null) graphics.drawCenteredString(font, progressBarFillText, x + 48, y + 3, -1);
	}

	private static void drawEmptyBundleDescriptionText(int x, int y, Font font, GuiGraphics graphics) {
		graphics.drawWordWrap(font, CAMERA_EMPTY_DESCRIPTION, x, y, GRID_WIDTH, -5592406);
	}

	private static void drawContainsDescriptionText(int x, int y, Font font, GuiGraphics graphics) {
		graphics.drawWordWrap(font, CAMERA_CONTAINS_DESCRIPTION, x, y, GRID_WIDTH, -5592406);
	}

	private static int getEmptyBundleDescriptionTextHeight(Font font) {
		return font.split(CAMERA_EMPTY_DESCRIPTION, GRID_WIDTH).size() * 9;
	}

	private static int getContainsDescriptionTextHeight(Font font) {
		return font.split(CAMERA_CONTAINS_DESCRIPTION, GRID_WIDTH).size() * 9;
	}

	private static int getProgressBarFill(Fraction weight) {
		return Mth.clamp(Mth.mulAndTruncate(weight, PROGRESSBAR_FILL_MAX), 0, PROGRESSBAR_FILL_MAX);
	}

	@Nullable
	private static Component getProgressBarFillText(final Fraction weight, Component emptyText) {
		if (weight.compareTo(Fraction.ZERO) == 0) return emptyText;
		return weight.compareTo(Fraction.ONE) >= 0 ? CAMERA_FULL_TEXT : null;
	}
}
