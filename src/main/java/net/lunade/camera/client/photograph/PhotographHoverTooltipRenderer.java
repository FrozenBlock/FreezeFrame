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

package net.lunade.camera.client.photograph;

import java.util.List;
import net.lunade.camera.component.PhotographComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class PhotographHoverTooltipRenderer {
	private static final int TOOLTIP_MARGIN = 4;
	private static final int TOOLTIP_PADDING = 3;
	private static final int BACKGROUND_COLOR = 0xF0100010;
	private static final int BORDER_TOP_COLOR = 0x505000FF;
	private static final int BORDER_BOTTOM_COLOR = 0x5028007F;

	private PhotographHoverTooltipRenderer() {
	}

	public static void render(GuiGraphics graphics, Font font, int screenWidth, int screenHeight, int mouseX, int mouseY, PhotographComponent photograph) {
		final List<Component> lines = PhotographDetails.buildTooltipLines(photograph);
		if (lines.isEmpty()) return;

		int tooltipWidth = 0;
		for (Component line : lines) {
			tooltipWidth = Math.max(tooltipWidth, font.width(line.getString()));
		}

		final int tooltipHeight = (lines.size() * font.lineHeight) + (TOOLTIP_PADDING * 2);
		int tooltipX = mouseX + 12;
		int tooltipY = mouseY - 12;
		if (tooltipX + tooltipWidth + (TOOLTIP_PADDING * 2) > screenWidth - TOOLTIP_MARGIN) {
			tooltipX = mouseX - 12 - tooltipWidth - (TOOLTIP_PADDING * 2);
		}
		if (tooltipY + tooltipHeight > screenHeight - TOOLTIP_MARGIN) {
			tooltipY = screenHeight - tooltipHeight - TOOLTIP_MARGIN;
		}
		if (tooltipY < TOOLTIP_MARGIN) {
			tooltipY = TOOLTIP_MARGIN;
		}

		final int x0 = tooltipX - TOOLTIP_PADDING;
		final int y0 = tooltipY - TOOLTIP_PADDING;
		final int x1 = tooltipX + tooltipWidth + TOOLTIP_PADDING;
		final int y1 = tooltipY + tooltipHeight - TOOLTIP_PADDING;

		graphics.pose().pushMatrix();

		drawVanillaTooltipBox(graphics, x0, y0, x1, y1);

		int textY = tooltipY;
		for (Component line : lines) {
			graphics.drawString(font, line.getString(), tooltipX, textY, 0xFFFFFFFF, false);
			textY += font.lineHeight;
		}

		graphics.pose().popMatrix();
	}

	private static void drawVanillaTooltipBox(GuiGraphics graphics, int x0, int y0, int x1, int y1) {
		graphics.fillGradient(x0 - 3, y0 - 4, x1 + 3, y0 - 3, BACKGROUND_COLOR, BACKGROUND_COLOR);
		graphics.fillGradient(x0 - 3, y1 + 3, x1 + 3, y1 + 4, BACKGROUND_COLOR, BACKGROUND_COLOR);
		graphics.fillGradient(x0 - 3, y0 - 3, x1 + 3, y1 + 3, BACKGROUND_COLOR, BACKGROUND_COLOR);
		graphics.fillGradient(x0 - 4, y0 - 3, x0 - 3, y1 + 3, BACKGROUND_COLOR, BACKGROUND_COLOR);
		graphics.fillGradient(x1 + 3, y0 - 3, x1 + 4, y1 + 3, BACKGROUND_COLOR, BACKGROUND_COLOR);

		graphics.fillGradient(x0 - 3, y0 - 3 + 1, x0 - 2, y1 + 3 - 1, BORDER_TOP_COLOR, BORDER_BOTTOM_COLOR);
		graphics.fillGradient(x1 + 2, y0 - 3 + 1, x1 + 3, y1 + 3 - 1, BORDER_TOP_COLOR, BORDER_BOTTOM_COLOR);
		graphics.fillGradient(x0 - 3, y0 - 3, x1 + 3, y0 - 2, BORDER_TOP_COLOR, BORDER_TOP_COLOR);
		graphics.fillGradient(x0 - 3, y1 + 2, x1 + 3, y1 + 3, BORDER_BOTTOM_COLOR, BORDER_BOTTOM_COLOR);
	}
}
