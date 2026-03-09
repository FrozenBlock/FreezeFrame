package net.lunade.camera.component.tooltip.client;

import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.tooltip.PhotographTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;

public class ClientPhotographTooltipComponent implements ClientTooltipComponent {
	private static final int PHOTOGRAPH_RENDER_SIZE = 32;
	private static final int PHOTOGRAPH_RENDER_OFFSET_X = 3;
	private static final int TOOLTIP_WIDTH = PHOTOGRAPH_RENDER_SIZE + (PHOTOGRAPH_RENDER_OFFSET_X * 2);
	private final Identifier photographId;

	@Contract(pure = true)
	public ClientPhotographTooltipComponent(PhotographTooltipComponent component) {
		this.photographId = component.id();
	}

	@Override
	public int getHeight(Font font) {
		return PHOTOGRAPH_RENDER_SIZE + 6;
	}

	@Override
	public int getWidth(Font font) {
		return TOOLTIP_WIDTH;
	}

	@Override
	public void renderImage(Font font, int x, int y, int k, int l, GuiGraphics guiGraphics) {
		PhotographRenderer.render(x, y, PHOTOGRAPH_RENDER_OFFSET_X, 0, guiGraphics, this.photographId, PHOTOGRAPH_RENDER_SIZE, true);
	}

	@Override
	public void renderText(GuiGraphics guiGraphics, Font font, int i, int j) {
		//guiGraphics.drawString(font, this.text, i, j, -1, true);
	}
}
