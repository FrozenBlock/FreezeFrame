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

package net.frozenblock.freezeframe.component.tooltip.client;

import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.photograph.PhotographDetails;
import net.frozenblock.freezeframe.client.photograph.PhotographLoader;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.FilmItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

@Environment(EnvType.CLIENT)
public class ClientFilmTooltip implements ClientTooltipComponent {
	private static final int PHOTOGRAPH_RENDER_SIZE = 32;
	private static final int BELOW_PHOTOGRAPH_SPACING = 6;
	private static final int TOOLTIP_HEIGHT = PHOTOGRAPH_RENDER_SIZE + BELOW_PHOTOGRAPH_SPACING;
	private static final int GRID_WIDTH = 96;
	private static final Identifier PROGRESSBAR_BORDER_SPRITE = FFConstants.id("container/film/film_progressbar_border");
	private static final Identifier PROGRESSBAR_FULL_SPRITE = FFConstants.id("container/film/film_progressbar_full");
	private static final int PROGRESSBAR_FILL_COLOR = 0xFFB8895F;
	private static final Component FILM_FULL_TEXT = Component.translatable("item." + FFConstants.MOD_ID + ".film.full");
	private static final Component FILM_EMPTY_TEXT = Component.translatable("item." + FFConstants.MOD_ID + ".film.empty");
	private static final String FILM_EMPTY_DESCRIPTION_KEY = "item." + FFConstants.MOD_ID + ".film.empty.description";
	private static final String FILM_FULLNESS_KEY = "item." + FFConstants.MOD_ID + ".film.fullness";
	private final FilmContents contents;
	private final boolean empty;
	private final boolean hasMultiplePhotographs;
	@Nullable
	private final Identifier photographId;
	@Nullable
	private final Component name;
	@Nullable
	private final Component photographer;
	@Nullable
	private final Component dateAndTime;
	private final Component emptyDescription;
	private final boolean hidePhotographPreviewAndInfo;
	private final int maxPhotographs;

	public ClientFilmTooltip(FilmContents contents, int maxPhotographs) {
		this.contents = contents;
		this.empty = contents.isEmpty();
		this.hasMultiplePhotographs = !this.empty && contents.size() > 1;
		this.maxPhotographs = FilmItem.normalizeMaxPhotographs(maxPhotographs);
		@Nullable Photograph photograph = this.empty ? null : contents.getSelectedPhotograph();

		this.photographId = photograph == null
			? null
			: photograph.identifier();

		this.name = photograph == null
			? null
			: PhotographDetails.getPhotographNameLine(photograph);

		this.photographer = photograph == null || StringUtil.isNullOrEmpty(photograph.photographer())
			? null
			: Component.translatable("photograph.photographer", photograph.photographer()).withStyle(ChatFormatting.GRAY);

		final Optional<Date> optionalDate = photograph == null ? Optional.empty() : PhotographLoader.parseDate(this.photographId.getPath());
		this.dateAndTime = optionalDate
			.map(date -> Component.translatable("photograph.date", formatDate(date)).withStyle(ChatFormatting.GRAY))
			.orElse(null);

		this.emptyDescription = Component.translatable(FILM_EMPTY_DESCRIPTION_KEY, this.maxPhotographs);

		this.hidePhotographPreviewAndInfo = FFConfig.HIDE_FILM_PHOTO_PREVIEW_AND_INFO.get();
		contents.photographs().forEach(component -> PhotographLoader.getAndLoadPhotograph(component.identifier()));
	}

	@Override
	public int getHeight(Font font) {
		if (this.hidePhotographPreviewAndInfo) return this.getEmptyFilmBackgroundHeight(font);
		return this.empty ? this.getEmptyFilmBackgroundHeight(font) : this.backgroundHeight(font);
	}

	@Override
	public int getWidth(Font font) {
		final int textWidth = Math.max(
			this.name != null ? getTextHeight(this.name, font) : 0,
			Math.max(
				this.photographer != null ? getTextHeight(this.photographer, font) : 0,
				this.dateAndTime != null ? getTextHeight(this.dateAndTime, font) : 0
			)
		);
		return Math.max(GRID_WIDTH, textWidth);
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	private int getEmptyFilmBackgroundHeight(Font font) {
		return this.getEmptyFilmDescriptionTextHeight(font) + 13 + 8;
	}

	private int backgroundHeight(Font font) {
		return this.photographGridHeight(font) + 13 + 8;
	}

	private int photographGridHeight(Font font) {
		if (this.empty) return 0;
		if (this.hidePhotographPreviewAndInfo) return 0;

		int extension = 0;
		if (this.name != null) extension += getTextHeight(this.name, font);
		if (this.photographer != null) extension += getTextHeight(this.photographer, font);
		if (this.dateAndTime != null) extension += getTextHeight(this.dateAndTime, font);
		return PHOTOGRAPH_RENDER_SIZE + extension;
	}

	private static int getContentXOffset(int tooltipWidth) {
		return (tooltipWidth - GRID_WIDTH) / 2;
	}

	@Override
	public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
		final Fraction weight = FilmItem.getWeightSafe(this.contents, this.maxPhotographs);

		if (this.empty) {
			this.extractEmptyFilmTooltip(font, x, y, w, h, graphics);
		} else {
			this.extractPhotographTooltip(font, x, y, w, h, graphics, weight);
		}
	}

	private void extractEmptyFilmTooltip(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
		final int left = x + getContentXOffset(w);
		this.extractEmptyFilmDescriptionText(left, y, font, graphics);
		extractProgressbar(left, y + this.getEmptyFilmDescriptionTextHeight(font) + 4, font, graphics, Fraction.ZERO, 0, this.maxPhotographs);
	}

	private void extractPhotographTooltip(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics, Fraction weight) {
		if (!this.hidePhotographPreviewAndInfo) {
			this.extractSelectedPhotographTooltip(font, graphics, x, y, w);
			extractProgressbar(
				x + getContentXOffset(w),
				y + this.photographGridHeight(font) + BELOW_PHOTOGRAPH_SPACING,
				font,
				graphics,
				weight,
				this.contents.size(),
				this.maxPhotographs
			);
			return;
		}

		final int left = x + getContentXOffset(w);
		this.extractEmptyFilmDescriptionText(left, y, font, graphics);
		extractProgressbar(left, y + this.getEmptyFilmDescriptionTextHeight(font) + 4, font, graphics, weight, this.contents.size(), this.maxPhotographs);
	}

	private void extractSelectedPhotographTooltip(Font font, GuiGraphicsExtractor graphics, int x, int y, int w) {
		if (this.empty) return;

		final int left = x + getContentXOffset(w);
		final int photoDrawX = left + ((w / 2) - (PHOTOGRAPH_RENDER_SIZE / 2));
		PhotographRenderer.blit(photoDrawX, y, 0, 0, graphics, this.photographId, PHOTOGRAPH_RENDER_SIZE, PhotographRenderer.FrameType.FILM_EMBED);

		// TODO: extract arrows
		if (this.hasMultiplePhotographs) {
		}

		this.extractPhotographTooltips(left, y + TOOLTIP_HEIGHT, font, graphics);
	}

	private static void extractProgressbar(int x, int y, Font font, GuiGraphicsExtractor graphics, Fraction weight, int photographCount, int maxPhotographs) {
		final int progressBarFill = getProgressBarFill(weight);
		if (weight.compareTo(Fraction.ONE) >= 0) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_FULL_SPRITE, x + 1, y, progressBarFill, 13);
		} else if (progressBarFill > 0) {
			graphics.fill(x + 1, y, x + 1 + progressBarFill, y + 13, PROGRESSBAR_FILL_COLOR);
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, 96, 13);

		final Component progressBarFillText = getProgressBarFillText(weight, photographCount, maxPhotographs);
		if (progressBarFillText != null) graphics.centeredText(font, progressBarFillText, x + 48, y + 3, -1);
	}

	private void extractEmptyFilmDescriptionText(int x, int y, Font font, GuiGraphicsExtractor graphics) {
		graphics.textWithWordWrap(font, this.emptyDescription, x, y, GRID_WIDTH, -5592406);
	}

	private void extractPhotographTooltips(int x, int y, Font font, GuiGraphicsExtractor graphics) {
		if (this.name != null) {
			graphics.textWithWordWrap(font, this.name, x, y, GRID_WIDTH, -1);
			y += getTextHeight(this.name, font);
		}

		if (this.photographer != null) {
			graphics.textWithWordWrap(font, this.photographer, x, y, GRID_WIDTH, -5592406);
			y += getTextHeight(this.photographer, font);
		}

		if (this.dateAndTime != null) {
			graphics.textWithWordWrap(font, this.dateAndTime, x, y, GRID_WIDTH, -5592406);
			y += getTextHeight(this.dateAndTime, font);
		}
	}

	private int getEmptyFilmDescriptionTextHeight(Font font) {
		return getTextHeight(this.emptyDescription, font);
	}

	private static int getTextHeight(FormattedText text, Font font) {
		return font.split(text, GRID_WIDTH).size() * font.lineHeight;
	}

	private static int getProgressBarFill(Fraction weight) {
		return Mth.clamp(Mth.mulAndTruncate(weight, 94), 0, 94);
	}

	private static String formatDate(Date date) {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
	}

	private static Component getProgressBarFillText(Fraction weight, int photographCount, int maxPhotographs) {
		if (weight.compareTo(Fraction.ZERO) == 0) return FILM_EMPTY_TEXT;
		if (weight.compareTo(Fraction.ONE) >= 0) return FILM_FULL_TEXT;
		return Component.translatable(FILM_FULLNESS_KEY, photographCount, maxPhotographs);
	}
}
