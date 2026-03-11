package net.lunade.camera.component.tooltip.client;

import java.util.Date;
import java.util.Optional;
import net.lunade.camera.client.photograph.PhotographLoader;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.tooltip.PhotographTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

public class ClientPhotographTooltipComponent implements ClientTooltipComponent {
	private static final int PHOTOGRAPH_RENDER_SIZE = 32;
	private static final int PHOTOGRAPH_RENDER_OFFSET_X = 3;
	private static final int BELOW_PHOTOGRAPH_SPACING = 6;
	private static final int TOOLTIP_HEIGHT = PHOTOGRAPH_RENDER_SIZE + BELOW_PHOTOGRAPH_SPACING;
	private static final int TOOLTIP_WIDTH = PHOTOGRAPH_RENDER_SIZE + (PHOTOGRAPH_RENDER_OFFSET_X * 2);
	private static final Component COPY_COMPONENT = Component.translatable("photograph.copy").withStyle(ChatFormatting.GRAY);
	private final Identifier photographId;
	@Nullable
	private final Component photographer;
	@Nullable
	private final Component dateAndTime;
	@Nullable
	private final Component copy;

	public ClientPhotographTooltipComponent(PhotographTooltipComponent component) {
		this.photographId = component.identifier();
		this.photographer = StringUtil.isNullOrEmpty(component.photographer())
			? null
			: Component.translatable("photograph.photographer", component.photographer()).withStyle(ChatFormatting.GRAY);

		final Optional<Date> optionalDate = PhotographLoader.parseDate(component.identifier().getPath());
		this.dateAndTime = optionalDate
			.map(date -> Component.translatable("photograph.date", date.toLocaleString()).withStyle(ChatFormatting.GRAY))
			.orElse(null);

		this.copy = !component.isCopy()
			? null
			: COPY_COMPONENT;
	}

	@Override
	public int getHeight(Font font) {
		int extension = 0;
		if (this.photographer != null) extension += font.lineHeight;
		if (this.dateAndTime != null) extension += font.lineHeight;
		if (this.copy != null) extension += font.lineHeight;
		return TOOLTIP_HEIGHT + extension;
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
	public void renderImage(Font font, int x, int y, int k, int l, GuiGraphics graphics) {
		PhotographRenderer.blit(x, y, PHOTOGRAPH_RENDER_OFFSET_X, 0, graphics, this.photographId, PHOTOGRAPH_RENDER_SIZE, PhotographRenderer.FrameType.FRAME);
	}

	@Override
	public void renderText(GuiGraphics graphics, Font font, int x, int y) {
		y += TOOLTIP_HEIGHT;

		if (this.photographer != null) {
			graphics.drawString(font, this.photographer, x, y, -1, true);
			y += font.lineHeight;
		}

		if (this.dateAndTime != null) {
			graphics.drawString(font, this.dateAndTime, x, y, -1, true);
			y += font.lineHeight;
		}

		if (this.copy != null) {
			graphics.drawString(font, this.copy, x, y, -1, true);
			y += font.lineHeight;
		}
	}
}
