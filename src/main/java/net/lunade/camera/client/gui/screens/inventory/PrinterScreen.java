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

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.menu.PrinterMenu;
import net.lunade.camera.networking.packet.PrinterSyncSelectPhotographIndexPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class PrinterScreen extends AbstractContainerScreen<PrinterMenu> {
	private static final boolean DEBUG_PRINTER_INFO = false && CameraPortConstants.UNSTABLE_LOGGING;
	private static final int ARROW_BOX_SIZE = 52;
	private static final int FILM_PHOTOGRAPH_SIZE = 52;
	private static final int FILM_PHOTOGRAPH_Y = 39;
	private static final int FILM_LEFT_PHOTOGRAPH_X = 5;
	private static final int FILM_MIDDLE_PHOTOGRAPH_X = 62;
	private static final int FILM_RIGHT_PHOTOGRAPH_X = 119;
	private static final int COPY_PHOTOGRAPH_SIZE = 67;
	private static final int COPY_PHOTOGRAPH_Y = 31;
	private static final int COPY_PHOTOGRAPH_X = 55;
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/container/printer.png");
	private static final Identifier TEXTURE_FILM = CameraPortConstants.id("textures/gui/container/printer_film.png");
	private static final List<Identifier> SOURCE_SLOT_ICONS = List.of(
		CameraPortConstants.id("container/slot/film"),
		CameraPortConstants.id("container/slot/photograph")
	);
	private static final List<Identifier> PAPER_SLOT_ICONS = List.of(CameraPortConstants.id("container/slot/paper"));
	private final ScrollWheelHandler scrollWheelHandler;
	private final CyclingSlotBackground sourceSlotBackground = new CyclingSlotBackground(PrinterMenu.SOURCE_SLOT);
	private final CyclingSlotBackground paperSlotBackground = new CyclingSlotBackground(PrinterMenu.PAPER_SLOT);
	@Nullable
	private FilmContents filmContents;
	@Nullable
	private Identifier leftPhotograph = null;
	@Nullable
	private Identifier middlePhotograph = null;
	@Nullable
	private Identifier rightPhotograph = null;
	private int photographIndex = 0;
	private boolean displayFilm = false;
	private Identifier photographCopyId;

	public PrinterScreen(PrinterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, DEFAULT_IMAGE_WIDTH, 226);
		this.scrollWheelHandler = new ScrollWheelHandler();
		menu.registerUpdateListener(this::containerChanged);
		--this.titleLabelY;
		this.titleLabelX += 78;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		this.containerChanged();
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.sourceSlotBackground.tick(SOURCE_SLOT_ICONS);
		this.paperSlotBackground.tick(PAPER_SLOT_ICONS);
	}

	private void setupDataAndResultSlot(int photographIndex) {
		ClientPlayNetworking.send(new PrinterSyncSelectPhotographIndexPacket(photographIndex));
		this.menu.setupDataAndResultSlot(photographIndex);
		setupOrClearFilmPhotographDisplays();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		final Identifier bgTexture = this.displayFilm ? TEXTURE_FILM : TEXTURE;
		graphics.blit(RenderPipelines.GUI_TEXTURED, bgTexture, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
		this.sourceSlotBackground.render(this.menu, graphics, delta, this.leftPos, this.topPos);
		this.paperSlotBackground.render(this.menu, graphics, delta, this.leftPos, this.topPos);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);

		if (this.displayFilm) {
			if (this.middlePhotograph != null) {
				PhotographRenderer.blit(
					this.leftPos,
					this.topPos,
					FILM_MIDDLE_PHOTOGRAPH_X,
					FILM_PHOTOGRAPH_Y,
					graphics,
					this.middlePhotograph,
					FILM_PHOTOGRAPH_SIZE,
					PhotographRenderer.FrameType.FILM_EMBED
				);
				// Render Index
				if (DEBUG_PRINTER_INFO) {
					graphics.drawString(
						this.font,
						Component.literal("Client Index:" + this.photographIndex),
						this.leftPos + FILM_MIDDLE_PHOTOGRAPH_X,
						this.topPos + FILM_PHOTOGRAPH_Y - this.font.lineHeight,
						-1,
						false
					);
					graphics.drawString(
						this.font,
						Component.literal("Server Index:" + this.menu.photographIndex.get()),
						this.leftPos + FILM_MIDDLE_PHOTOGRAPH_X,
						this.topPos + FILM_PHOTOGRAPH_Y - this.font.lineHeight - this.font.lineHeight,
						-1,
						false
					);
				}
			}

			if (this.filmContents.size() == 1) return;

			if (this.rightPhotograph != null) {
				// Render right photograph
				PhotographRenderer.blit(
					this.leftPos,
					this.topPos,
					FILM_RIGHT_PHOTOGRAPH_X,
					FILM_PHOTOGRAPH_Y,
					graphics,
					this.rightPhotograph,
					FILM_PHOTOGRAPH_SIZE, PhotographRenderer.FrameType.FILM_EMBED
				);
			}

			if (this.leftPhotograph != null) {
				// Render left photograph
				PhotographRenderer.blit(
					this.leftPos,
					this.topPos,
					FILM_LEFT_PHOTOGRAPH_X,
					FILM_PHOTOGRAPH_Y,
					graphics,
					this.leftPhotograph,
					FILM_PHOTOGRAPH_SIZE,
					PhotographRenderer.FrameType.FILM_EMBED
				);
			}
		}

		if (this.photographCopyId != null) {
			PhotographRenderer.blit(
				this.leftPos,
				this.topPos,
				COPY_PHOTOGRAPH_X,
				COPY_PHOTOGRAPH_Y,
				graphics,
				this.photographCopyId,
				COPY_PHOTOGRAPH_SIZE,
				PhotographRenderer.FrameType.FRAME
			);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!this.displayFilm) return super.mouseClicked(event, doubleClick);

		final int mouseX = (int) event.x();
		final int mouseY = (int) event.y();

		// Right arrow clicked
		if (this.rightPhotograph != null && this.isHovering(FILM_RIGHT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(false);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		// Left arrow clicked
		if (this.leftPhotograph != null && this.isHovering(FILM_LEFT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(true);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private void incrementPhotographIndex(boolean left) {
		if (this.photographIndex == 0 && left) {
			this.photographIndex = this.filmContents.size() - 1;
		} else if (this.photographIndex == this.filmContents.size() - 1 && !left) {
			this.photographIndex = 0;
		} else {
			if (left) {
				this.photographIndex--;
			} else {
				this.photographIndex++;
			}
		}

		this.setupDataAndResultSlot(this.photographIndex);
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (super.mouseScrolled(x, y, scrollX, scrollY)) return true;
		if (this.filmContents == null || !this.displayFilm || this.filmContents.isEmpty()) return false;

		final Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		final int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
		if (wheel != 0) {
			final int currentIndex = this.photographIndex;
			final int updatedIndex = ScrollWheelHandler.getNextScrollWheelSelection(wheel, currentIndex, this.filmContents.size());
			if (currentIndex != updatedIndex) {
				this.photographIndex = updatedIndex;
				this.setupDataAndResultSlot(this.photographIndex);
				this.setupOrClearFilmPhotographDisplays();
			}
		}

		return true;
	}

	@Nullable
	private Identifier getInfiniteFilmPhotograph(int index) {
		if (this.filmContents == null || this.filmContents.isEmpty()) return null;
		int size = this.filmContents.size();
		int adjustedIndex = ((index % size) + size) % size;
		return this.filmContents.getPhotographAtIndex(adjustedIndex).identifier();
	}

	private void containerChanged() {
		if (!this.menu.hasSourceItem()) {
			this.filmContents = null;
			this.leftPhotograph = null;
			this.middlePhotograph = null;
			this.rightPhotograph = null;
			this.photographIndex = 0;
			this.displayFilm = false;
			this.photographCopyId = null;
			return;
		}

		this.setupOrClearFilmPhotographDisplays();

		final ItemStack sourceItem = this.menu.getSourceItem();
		if (sourceItem.is(CameraPortItems.PHOTOGRAPH)) {
			final PhotographComponent photographComponent = sourceItem.get(CameraPortDataComponents.PHOTOGRAPH);
			this.photographCopyId = (photographComponent == null || photographComponent.isCopy()) ? null : photographComponent.identifier();
		} else {
			this.photographCopyId = null;
		}
	}

	private void setupOrClearFilmPhotographDisplays() {
		final ItemStack sourceItem = this.menu.getSourceItem();
		this.displayFilm = sourceItem.is(CameraPortItems.FILM) && sourceItem.has(CameraPortDataComponents.FILM_CONTENTS);
		if (!this.displayFilm) {
			this.filmContents = null;
			this.leftPhotograph = null;
			this.middlePhotograph = null;
			this.rightPhotograph = null;
			this.photographIndex = 0;
		} else {
			this.filmContents = sourceItem.get(CameraPortDataComponents.FILM_CONTENTS);
			this.middlePhotograph = this.getInfiniteFilmPhotograph(this.photographIndex);
			if (this.filmContents.size() > 1) {
				this.rightPhotograph = this.getInfiniteFilmPhotograph(this.photographIndex + 1);
				this.leftPhotograph = this.getInfiniteFilmPhotograph(this.photographIndex - 1);
			}
		}
	}
}
