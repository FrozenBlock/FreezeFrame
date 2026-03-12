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

package net.lunade.camera.client.gui.screens.inventory;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * UNUSED SCREEN
 */

@Environment(EnvType.CLIENT)
public class PhotographScreen extends Screen {
	private final Identifier BASE_IMAGE = CameraPortConstants.id("textures/gui/picture_screen.png");
	private final List<Identifier> IMAGES = new ArrayList<>();

	private int leftPos;
	private int topPos;
	private float xMouse;
	private float yMouse;
	private boolean scrolling;
	private float scrollIndex = 0;
	private Button confirm;
	private final int rows = 3;

	public PhotographScreen() {
		super(GameNarrator.NO_TITLE); //Add a title to the screen if you want idfk if it renders or whatever
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos = (this.width - 176) / 2;
		this.topPos = (this.height - 166) / 2;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics, mouseX, mouseY, delta);
		int i = leftPos;
		int j = topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, BASE_IMAGE, i, j, 0, 0, 176, 176, 166, 166);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		int i = leftPos;
		int j = topPos;
		for (int k = 0; k < IMAGES.size(); k++) {
			final var texture = IMAGES.get(k);
			final int row = k % rows;
			final int col = k / rows;
			graphics.blit(texture, i + 7 + row * 49, j + 7 + col * 49, 0, 0, 48, 48, 48, 48);
		}
	}
}
