package net.lunade.camera.component.tooltip.client;

import com.mojang.serialization.DataResult;
import java.util.Date;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.photograph.PhotographLoader;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.PhotographComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

@Environment(EnvType.CLIENT)
public class ClientFilmTooltip implements ClientTooltipComponent {
	private static final int PHOTOGRAPH_RENDER_SIZE = 32;
	private static final int PHOTOGRAPH_RENDER_OFFSET_X = 3;
	private static final int BELOW_PHOTOGRAPH_SPACING = 6;
	private static final int TOOLTIP_HEIGHT = PHOTOGRAPH_RENDER_SIZE + BELOW_PHOTOGRAPH_SPACING;
	private static final int TOOLTIP_WIDTH = 96;
	private static final Identifier PROGRESSBAR_BORDER_SPRITE = CameraPortConstants.id("container/film/film_progressbar_border");
	private static final Identifier PROGRESSBAR_FILL_SPRITE = CameraPortConstants.id("container/film/film_progressbar_fill");
	private static final Identifier PROGRESSBAR_FULL_SPRITE = CameraPortConstants.id("container/film/film_progressbar_full");
	private static final Component FILM_FULL_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".film.full");
	private static final Component FILM_EMPTY_TEXT = Component.translatable("item." + CameraPortConstants.MOD_ID + ".film.empty");
	private static final Component FILM_EMPTY_DESCRIPTION = Component.translatable("item." + CameraPortConstants.MOD_ID + ".film.empty.description");

	private final FilmContents contents;
	private final boolean empty;
	@Nullable
	private final PhotographComponent photograph;
	@Nullable
	private final Identifier photographId;
	@Nullable
	private final Component photographer;
	@Nullable
	private final Component dateAndTime;
	@Nullable
	private final Component copy;

	public ClientFilmTooltip(FilmContents contents) {
		this.contents = contents;
		this.empty = contents.isEmpty();
		this.photograph = this.empty ? null : contents.getSelectedPhotograph();

		this.photographId = this.photograph == null
			? null
			: this.photograph.identifier();

		this.photographer = this.photograph == null || StringUtil.isNullOrEmpty(this.photograph.photographer())
			? null
			: Component.translatable("photograph.photographer", this.photograph.photographer()).withStyle(ChatFormatting.GRAY);

		final Optional<Date> optionalDate = this.photograph == null ? Optional.empty() : PhotographLoader.parseDate(this.photographId.getPath());
		this.dateAndTime = optionalDate
			.map(date -> Component.translatable("photograph.date", date.toLocaleString()).withStyle(ChatFormatting.GRAY))
			.orElse(null);

		this.copy = this.photograph == null || !this.photograph.isCopy()
			? null
			: ClientPhotographTooltip.COPY_COMPONENT;
	}

	@Override
	public int getHeight(Font font) {
		return this.contents.isEmpty() ? getEmptyFilmBackgroundHeight(font) : this.backgroundHeight(font);
	}

	@Override
	public int getWidth(Font font) {
		final int textWidth = Math.max(
			Math.max(
				this.photographer != null ? font.width(this.photographer) : 0,
				this.dateAndTime != null ? font.width(this.dateAndTime) : 0
			),
			this.copy != null ? font.width(this.copy) : 0
		);
		return Math.max(TOOLTIP_WIDTH, textWidth);
	}

	@Override
	public boolean showTooltipWithItemInHand() {
		return true;
	}

	private static int getEmptyFilmBackgroundHeight(Font font) {
		return getEmptyFilmDescriptionTextHeight(font) + 13 + 8;
	}

	private int backgroundHeight(Font font) {
		return this.photographGridHeight(font) + 13 + 8;
	}

	private int photographGridHeight(Font font) {
		if (this.contents.isEmpty()) return 0;

		int extension = 0;
		if (this.photographer != null) extension += font.lineHeight;
		if (this.dateAndTime != null) extension += font.lineHeight;
		if (this.copy != null) extension += font.lineHeight;
		return PHOTOGRAPH_RENDER_SIZE + extension;
	}

	private static int getContentXOffset(int tooltipWidth) {
		return (tooltipWidth - 96) / 2;
	}

	@Override
	public void renderImage(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
		final DataResult<Fraction> weight = this.contents.weight();
		if (weight.isError()) return;

		if (this.contents.isEmpty()) {
			renderEmptyFilmTooltip(font, x, y, w, h, graphics);
		} else {
			this.renderPhotographTooltip(font, x, y, w, h, graphics, weight.getOrThrow());
		}
	}

	private static void renderEmptyFilmTooltip(Font font, int x, int y, int w, int h, GuiGraphics graphics) {
		final int left = x + getContentXOffset(w);
		drawEmptyFilmDescriptionText(left, y, font, graphics);
		drawProgressbar(left, y + getEmptyFilmDescriptionTextHeight(font) + 4, font, graphics, Fraction.ZERO);
	}

	private void renderPhotographTooltip(Font font, int x, int y, int w, int h, GuiGraphics graphics, Fraction weight) {
		this.drawSelectedPhotographTooltip(font, graphics, x, y, w);
		drawProgressbar(x + getContentXOffset(w), y + this.photographGridHeight(font) + 4, font, graphics, weight);
	}


	private void drawSelectedPhotographTooltip(Font font, GuiGraphics graphics, int x, int y, int w) {
		if (this.empty) return;

		final int drawX = x + getContentXOffset(w) + ((this.getWidth(font) / 2) - PHOTOGRAPH_RENDER_SIZE);
		boolean hasMultiple = this.contents.size() > 1;

		final PhotographComponent photograph = this.contents.getSelectedPhotograph();
		PhotographRenderer.blit(drawX, y, 0, 0, graphics, photograph.identifier(), PHOTOGRAPH_RENDER_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
	}

	private static void drawProgressbar(int x, int y, Font font, GuiGraphics graphics, Fraction weight) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getProgressBarTexture(weight), x + 1, y, getProgressBarFill(weight), 13);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESSBAR_BORDER_SPRITE, x, y, 96, 13);
		Component progressBarFillText = getProgressBarFillText(weight);
		if (progressBarFillText != null) {
			graphics.drawCenteredString(font, progressBarFillText, x + 48, y + 3, -1);
		}
	}

	private static void drawEmptyFilmDescriptionText(int x, int y, Font font, GuiGraphics graphics) {
		graphics.drawWordWrap(font, FILM_EMPTY_DESCRIPTION, x, y, 96, -5592406);
	}

	private static int getEmptyFilmDescriptionTextHeight(Font font) {
		return font.split(FILM_EMPTY_DESCRIPTION, 96).size() * 9;
	}

	private static int getProgressBarFill(Fraction weight) {
		return Mth.clamp(Mth.mulAndTruncate(weight, 94), 0, 94);
	}

	private static Identifier getProgressBarTexture(Fraction weight) {
		return weight.compareTo(Fraction.ONE) >= 0 ? PROGRESSBAR_FULL_SPRITE : PROGRESSBAR_FILL_SPRITE;
	}

	@Nullable
	private static Component getProgressBarFillText(Fraction weight) {
		if (weight.compareTo(Fraction.ZERO) == 0) return FILM_EMPTY_TEXT;
		return weight.compareTo(Fraction.ONE) >= 0 ? FILM_FULL_TEXT : null;
	}
}
