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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.photograph.PhotographDetails;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.component.tooltip.PhotographTooltip;
import net.lunade.camera.config.CameraPortConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

@Environment(EnvType.CLIENT)
public class ClientPhotographTooltip implements ClientTooltipComponent {
	private static final int PHOTOGRAPH_RENDER_SIZE = 32;
	private static final int PHOTOGRAPH_RENDER_OFFSET_X = 3;
	private static final int BELOW_PHOTOGRAPH_SPACING = 6;
	private static final int TOOLTIP_HEIGHT = PHOTOGRAPH_RENDER_SIZE + BELOW_PHOTOGRAPH_SPACING;
	private static final int TOOLTIP_WIDTH = PHOTOGRAPH_RENDER_SIZE + (PHOTOGRAPH_RENDER_OFFSET_X * 2);
	public static final Component ORIGINAL_COMPONENT = Component.translatable("photograph.original").withStyle(ChatFormatting.GRAY);
	public static final Component COPY_COMPONENT = Component.translatable("photograph.copy").withStyle(ChatFormatting.GRAY);
	public static final Component COPY_OF_COPY_COMPONENT = Component.translatable("photograph.copy_of_copy").withStyle(ChatFormatting.GRAY);
	private final Identifier photographId;
	@Nullable
	private final Component name;
	@Nullable
	private final Component photographer;
	@Nullable
	private final Component dateAndTime;
	@Nullable
	private final Component generationLabel;
	private final boolean previewHidden;

	public ClientPhotographTooltip(PhotographTooltip component) {
		this.photographId = component.identifier();
		final Photograph photograph = new Photograph(component.identifier(), component.photographer(), component.name(), component.generation());
		this.name = null;
		this.photographer = PhotographDetails.getPhotographerLine(photograph);
		this.dateAndTime = PhotographDetails.getDateLine(photograph);

		this.generationLabel = switch (component.generation()) {
			case 0 -> ORIGINAL_COMPONENT;
			case 1 -> COPY_COMPONENT;
			default -> COPY_OF_COPY_COMPONENT;
		};

		this.previewHidden = CameraPortConfig.HIDE_PHOTOGRAPH_PREVIEW.get();
	}

	@Override
	public int getHeight(Font font) {
		int extension = 0;
		if (this.name != null) extension += font.lineHeight;
		if (this.photographer != null) extension += font.lineHeight;
		if (this.dateAndTime != null) extension += font.lineHeight;
		if (this.generationLabel != null) extension += font.lineHeight;
		return (this.previewHidden ? 0 : TOOLTIP_HEIGHT) + extension;
	}

	@Override
	public int getWidth(Font font) {
		final int textWidth = Math.max(
			Math.max(
				this.name != null ? font.width(this.name) : 0,
				Math.max(
				this.photographer != null ? font.width(this.photographer) : 0,
				this.dateAndTime != null ? font.width(this.dateAndTime) : 0
				)
			),
			this.generationLabel != null ? font.width(this.generationLabel) : 0
		);
		return this.previewHidden ? textWidth : Math.max(TOOLTIP_WIDTH, textWidth);
	}

	@Override
	public void extractImage(Font font, int x, int y, int k, int l, GuiGraphicsExtractor graphics) {
		if (this.previewHidden) return;
		PhotographRenderer.blit(x, y, PHOTOGRAPH_RENDER_OFFSET_X, 0, graphics, this.photographId, PHOTOGRAPH_RENDER_SIZE, PhotographRenderer.FrameType.FRAME);
	}

	@Override
	public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		if (!this.previewHidden) y += TOOLTIP_HEIGHT;

		if (this.name != null) {
			graphics.text(font, this.name, x, y, -1, true);
			y += font.lineHeight;
		}

		if (this.photographer != null) {
			graphics.text(font, this.photographer, x, y, -1, true);
			y += font.lineHeight;
		}

		if (this.dateAndTime != null) {
			graphics.text(font, this.dateAndTime, x, y, -1, true);
			y += font.lineHeight;
		}

		if (this.generationLabel != null) {
			graphics.text(font, this.generationLabel, x, y, -1, true);
			y += font.lineHeight;
		}
	}
}
