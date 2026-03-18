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

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.photograph.PhotographHoverTooltipRenderer;
import net.lunade.camera.client.photograph.PhotographLoader;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.item.FilmItem;
import net.lunade.camera.menu.DevelopingTableMenu;
import net.lunade.camera.networking.packet.DevelopingTableSyncSelectPhotographIndexPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class DevelopingTableScreen extends AbstractContainerScreen<DevelopingTableMenu> {
	private static final int FILM_PHOTOGRAPH_SIZE = 52;
	private static final int FILM_PHOTOGRAPH_Y = 39;
	private static final int FILM_LEFT_PHOTOGRAPH_X = 5;
	private static final int FILM_MIDDLE_PHOTOGRAPH_X = 62;
	private static final int FILM_RIGHT_PHOTOGRAPH_X = 119;
	private static final int FILM_PHOTOGRAPH_BLOCKER_OFFSET = -1;
	private static final int FILM_PHOTOGRAPH_BLOCKER_SIZE = FILM_PHOTOGRAPH_SIZE + 2;
	private static final int SCROLLER_WIDTH = 17;
	private static final int SCROLLER_HEIGHT = 8;
	private static final int SCROLLER_TRACK_X = FILM_LEFT_PHOTOGRAPH_X - 1;
	private static final int SCROLLER_TRACK_Y = 93;
	private static final int SCROLLER_TRACK_WIDTH = FILM_RIGHT_PHOTOGRAPH_X + FILM_PHOTOGRAPH_SIZE - FILM_LEFT_PHOTOGRAPH_X + 2;
	private static final int COPY_PHOTOGRAPH_SIZE = 67;
	private static final int COPY_PHOTOGRAPH_Y = 31;
	private static final int COPY_PHOTOGRAPH_X = 55;
	private static final int RESULT_SLOT_X = 116;
	private static final int RESULT_SLOT_Y = 113;
	private static final int RESULT_SLOT_SIZE = 16;
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/container/developing_table.png");
	private static final Identifier TEXTURE_FILM = CameraPortConstants.id("textures/gui/container/developing_table_film.png");
	private static final Identifier SCROLLER = CameraPortConstants.id("container/developing_table/scroller");
	private static final Identifier SCROLLER_DISABLED = CameraPortConstants.id("container/developing_table/scroller_disabled");
	private static final Identifier FILM_PHOTOGRAPH_BLOCKER = CameraPortConstants.id("container/developing_table/film_photograph_blocker");
	private static final Identifier FILM_PHOTOGRAPH_HIGHLIGHT = CameraPortConstants.id("container/developing_table/film_photograph_highlight");
	private static final List<Identifier> SOURCE_SLOT_ICONS = List.of(
		CameraPortConstants.id("container/slot/film"),
		CameraPortConstants.id("container/slot/photograph")
	);
	private static final List<Identifier> PAPER_SLOT_ICONS = List.of(CameraPortConstants.id("container/slot/paper"));
	private final ScrollWheelHandler scrollWheelHandler;
	private final CyclingSlotBackground sourceSlotBackground = new CyclingSlotBackground(DevelopingTableMenu.SOURCE_SLOT);
	private final CyclingSlotBackground paperSlotBackground = new CyclingSlotBackground(DevelopingTableMenu.PAPER_SLOT);
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
	private boolean draggingScroller = false;
	private int scrollerX = SCROLLER_TRACK_X;
	private int filmMaxPhotographs = FilmContents.BASE_MAX_PHOTOGRAPHS;
	private ItemStack lastSourceItem = ItemStack.EMPTY;
	private Identifier photographCopyId;

	public DevelopingTableScreen(DevelopingTableMenu menu, Inventory inventory, Component title) {
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
		this.photographIndex = photographIndex;
		ClientPlayNetworking.send(new DevelopingTableSyncSelectPhotographIndexPacket(photographIndex));
		this.menu.setupDataAndResultSlot(photographIndex);
		setupOrClearFilmPhotographDisplays();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		final Identifier bgTexture = this.displayFilm ? TEXTURE_FILM : TEXTURE;
		graphics.blit(RenderPipelines.GUI_TEXTURED, bgTexture, this.leftPos, this.topPos, 0F, 0F, this.imageWidth, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
		this.sourceSlotBackground.extractRenderState(this.menu, graphics, partialTicks, this.leftPos, this.topPos);
		this.paperSlotBackground.extractRenderState(this.menu, graphics, partialTicks, this.leftPos, this.topPos);

		if (this.isHovering(SCROLLER_TRACK_X, SCROLLER_TRACK_Y, SCROLLER_TRACK_WIDTH, SCROLLER_HEIGHT, mouseX, mouseY) && this.hasMultipleFilmPhotographs()) {
			graphics.requestCursor(this.draggingScroller ? CursorTypes.RESIZE_EW : CursorTypes.POINTING_HAND);
		} else if (this.isHovering(SCROLLER_TRACK_X, SCROLLER_TRACK_Y, SCROLLER_TRACK_WIDTH, SCROLLER_HEIGHT, mouseX, mouseY) && this.displayFilm) {
			graphics.requestCursor(CursorTypes.NOT_ALLOWED);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		final int hoveredOffset = this.getHoveredFilmPhotographOffset(mouseX, mouseY);
		Photograph hoveredPhotograph = hoveredOffset == Integer.MIN_VALUE ? null : this.getFilmPhotographComponent(this.photographIndex + hoveredOffset);
		final boolean hoveringResultSlot = this.menu.getSlot(DevelopingTableMenu.RESULT_SLOT).hasItem()
			&& this.isHovering(RESULT_SLOT_X, RESULT_SLOT_Y, RESULT_SLOT_SIZE, RESULT_SLOT_SIZE, mouseX, mouseY);

		if (this.displayFilm) {
			if (this.isAtBeginning()) {
				this.extractFilmPhotographBlocker(graphics, FILM_LEFT_PHOTOGRAPH_X);
			}

			if (this.isAtEnd() && this.isFilmFull()) {
				this.extractFilmPhotographBlocker(graphics, FILM_RIGHT_PHOTOGRAPH_X);
			}

			if (this.middlePhotograph != null) {
				PhotographRenderer.blit(
					this.leftPos,
					this.topPos,
					FILM_MIDDLE_PHOTOGRAPH_X,
					FILM_PHOTOGRAPH_Y,
					graphics,
					this.middlePhotograph,
					FILM_PHOTOGRAPH_SIZE,
					PhotographRenderer.FrameType.NONE
				);
				if (hoveredOffset == 0 || hoveringResultSlot) {
					graphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						FILM_PHOTOGRAPH_HIGHLIGHT,
						this.leftPos + FILM_MIDDLE_PHOTOGRAPH_X,
						this.topPos + FILM_PHOTOGRAPH_Y,
						FILM_PHOTOGRAPH_SIZE,
						FILM_PHOTOGRAPH_SIZE
					);
				}
				if (hoveredOffset == 0) {
					graphics.requestCursor(CursorTypes.POINTING_HAND);
				}
			}

			if (this.hasMultipleFilmPhotographs()) {
				if (this.rightPhotograph != null) {
					PhotographRenderer.blit(
						this.leftPos,
						this.topPos,
						FILM_RIGHT_PHOTOGRAPH_X,
						FILM_PHOTOGRAPH_Y,
						graphics,
						this.rightPhotograph,
						FILM_PHOTOGRAPH_SIZE,
						PhotographRenderer.FrameType.NONE
					);
					if (hoveredOffset == 1) {
						graphics.blitSprite(
							RenderPipelines.GUI_TEXTURED,
							FILM_PHOTOGRAPH_HIGHLIGHT,
							this.leftPos + FILM_RIGHT_PHOTOGRAPH_X,
							this.topPos + FILM_PHOTOGRAPH_Y,
							FILM_PHOTOGRAPH_SIZE,
							FILM_PHOTOGRAPH_SIZE
						);
						graphics.requestCursor(CursorTypes.POINTING_HAND);
					}
				}

				if (this.leftPhotograph != null) {
					PhotographRenderer.blit(
						this.leftPos,
						this.topPos,
						FILM_LEFT_PHOTOGRAPH_X,
						FILM_PHOTOGRAPH_Y,
						graphics,
						this.leftPhotograph,
						FILM_PHOTOGRAPH_SIZE,
						PhotographRenderer.FrameType.NONE
					);
					if (hoveredOffset == -1) {
						graphics.blitSprite(
							RenderPipelines.GUI_TEXTURED,
							FILM_PHOTOGRAPH_HIGHLIGHT,
							this.leftPos + FILM_LEFT_PHOTOGRAPH_X,
							this.topPos + FILM_PHOTOGRAPH_Y,
							FILM_PHOTOGRAPH_SIZE,
							FILM_PHOTOGRAPH_SIZE
						);
						graphics.requestCursor(CursorTypes.POINTING_HAND);
					}
				}

				graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					SCROLLER,
					this.leftPos + this.getScrollerX(),
					this.topPos + SCROLLER_TRACK_Y,
					SCROLLER_WIDTH,
					SCROLLER_HEIGHT
				);
			} else {
				graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					SCROLLER_DISABLED,
					this.leftPos + SCROLLER_TRACK_X,
					this.topPos + SCROLLER_TRACK_Y,
					SCROLLER_WIDTH,
					SCROLLER_HEIGHT
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

		if (hoveredPhotograph != null) {
			PhotographHoverTooltipRenderer.extractRenderState(graphics, this.font, this.width, this.height, mouseX, mouseY, hoveredPhotograph);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!this.displayFilm) return super.mouseClicked(event, doubleClick);

		final int mouseX = (int) event.x();
		final int mouseY = (int) event.y();

		if (this.hasMultipleFilmPhotographs()) {
			if (this.isHovering(SCROLLER_TRACK_X, SCROLLER_TRACK_Y, SCROLLER_TRACK_WIDTH, SCROLLER_HEIGHT, mouseX, mouseY)
				|| this.isHovering(this.getScrollerX(), SCROLLER_TRACK_Y, SCROLLER_WIDTH, SCROLLER_HEIGHT, mouseX, mouseY)) {
				this.draggingScroller = true;
				this.updatePhotographIndexFromScroller(mouseX);
				return true;
			}
		}

		// Middle photograph clicked
		if (this.isHovering(FILM_MIDDLE_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			return true;
		}

		// Right photograph clicked
		if (this.rightPhotograph != null && this.isHovering(FILM_RIGHT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(1);
			return true;
		}

		// Left photograph clicked
		if (this.leftPhotograph != null && this.isHovering(FILM_LEFT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(-1);
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.draggingScroller && event.button() == GLFW.GLFW_RELEASE) {
			this.draggingScroller = false;
			return true;
		}

		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.draggingScroller && event.button() == 0 && this.hasMultipleFilmPhotographs()) {
			this.updatePhotographIndexFromScroller((int) event.x());
			return true;
		}

		return super.mouseDragged(event, dx, dy);
	}

	private void incrementPhotographIndex(int amount) {
		if (this.filmContents == null || this.filmContents.isEmpty()) return;

		final int updatedIndex = amount == 0 ? this.photographIndex : Math.max(0, Math.min(this.photographIndex + amount, this.getMaxPhotographIndex()));
		if (updatedIndex != this.photographIndex) {
			this.photographIndex = updatedIndex;
			this.setupDataAndResultSlot(this.photographIndex);
		}
		this.photographIndex = updatedIndex;
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CameraPortSounds.FILM_ROLL, (float) (0.8F + (Math.random() * 0.4F))));
		this.updateScrollerXFromPhotographIndex();
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (super.mouseScrolled(x, y, scrollX, scrollY)) return true;
		if (this.filmContents == null || !this.displayFilm || this.filmContents.isEmpty()) return false;

		final Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		final int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
		if (wheel != 0) {
			final int currentIndex = this.photographIndex;
			final int updatedIndex = Math.max(0, Math.min(currentIndex - wheel, this.getMaxPhotographIndex()));
			if (currentIndex != updatedIndex) {
				this.photographIndex = updatedIndex;
				this.setupDataAndResultSlot(this.photographIndex);
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CameraPortSounds.FILM_ROLL, (float) (0.8F + (Math.random() * 0.4F))));
				this.updateScrollerXFromPhotographIndex();
			}
		}

		return true;
	}

	@Nullable
	private Identifier getFilmPhotograph(int index) {
		if (this.filmContents == null || this.filmContents.isEmpty()) return null;
		if (index < 0 || index >= this.filmContents.size()) return null;
		return this.filmContents.getPhotographAtIndex(index).identifier();
	}

	@Nullable
	private Photograph getFilmPhotographComponent(int index) {
		if (this.filmContents == null || this.filmContents.isEmpty()) return null;
		if (index < 0 || index >= this.filmContents.size()) return null;
		return this.filmContents.getPhotographAtIndex(index);
	}

	private boolean hasMultipleFilmPhotographs() {
		return this.displayFilm && this.filmContents != null && this.filmContents.size() > 1;
	}

	private int getMaxPhotographIndex() {
		if (this.filmContents == null || this.filmContents.isEmpty()) return 0;
		return this.filmContents.size() - 1;
	}

	private int getScrollerX() {
		if (!this.hasMultipleFilmPhotographs()) return SCROLLER_TRACK_X;

		final int travel = SCROLLER_TRACK_WIDTH - SCROLLER_WIDTH;
		if (travel <= 0) return SCROLLER_TRACK_X;

		return this.scrollerX;
	}

	private void updatePhotographIndexFromScroller(int mouseX) {
		if (!this.hasMultipleFilmPhotographs()) return;

		final int travel = SCROLLER_TRACK_WIDTH - SCROLLER_WIDTH;
		if (travel <= 0) return;

		final int relative = Math.max(0, Math.min(mouseX - this.leftPos - SCROLLER_TRACK_X - (SCROLLER_WIDTH / 2), travel));
		final int updatedIndex = Math.round((relative / (float) travel) * this.getMaxPhotographIndex());
		this.scrollerX = SCROLLER_TRACK_X + relative;
		if (updatedIndex != this.photographIndex) {
			this.photographIndex = updatedIndex;
			this.setupDataAndResultSlot(this.photographIndex);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(CameraPortSounds.FILM_ROLL, (float) (0.8F + (Math.random() * 0.4F))));
		}
	}

	private void updateScrollerXFromPhotographIndex() {
		final int travel = SCROLLER_TRACK_WIDTH - SCROLLER_WIDTH;
		if (!this.hasMultipleFilmPhotographs() || travel <= 0) {
			this.scrollerX = SCROLLER_TRACK_X;
			return;
		}

		final float progress = this.photographIndex / (float) this.getMaxPhotographIndex();
		this.scrollerX = SCROLLER_TRACK_X + Math.round(progress * travel);
	}

	private void extractFilmPhotographBlocker(GuiGraphicsExtractor graphics, int slotX) {
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			FILM_PHOTOGRAPH_BLOCKER,
			this.leftPos + slotX + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
			this.topPos + FILM_PHOTOGRAPH_Y + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
			FILM_PHOTOGRAPH_BLOCKER_SIZE,
			FILM_PHOTOGRAPH_BLOCKER_SIZE
		);
	}

	private int getHoveredFilmPhotographOffset(double mouseX, double mouseY) {
		if (!this.displayFilm || this.filmContents == null || this.filmContents.isEmpty()) return Integer.MIN_VALUE;

		final int top = this.topPos + FILM_PHOTOGRAPH_Y;
		final int bottom = top + FILM_PHOTOGRAPH_SIZE;
		if (mouseY < top || mouseY >= bottom) return Integer.MIN_VALUE;

		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int closestOffset = Integer.MIN_VALUE;
		double closestDistance = Double.MAX_VALUE;

		if (this.leftPhotograph != null) {
			final int left = this.leftPos + FILM_LEFT_PHOTOGRAPH_X;
			final int right = left + FILM_PHOTOGRAPH_SIZE;
			minX = Math.min(minX, left);
			maxX = Math.max(maxX, right);
			final double distance = Math.abs(mouseX - (left + (FILM_PHOTOGRAPH_SIZE / 2.0)));
			if (distance < closestDistance) {
				closestDistance = distance;
				closestOffset = -1;
			}
		}

		if (this.middlePhotograph != null) {
			final int left = this.leftPos + FILM_MIDDLE_PHOTOGRAPH_X;
			final int right = left + FILM_PHOTOGRAPH_SIZE;
			minX = Math.min(minX, left);
			maxX = Math.max(maxX, right);
			final double distance = Math.abs(mouseX - (left + (FILM_PHOTOGRAPH_SIZE / 2.0)));
			if (distance < closestDistance) {
				closestDistance = distance;
				closestOffset = 0;
			}
		}

		if (this.rightPhotograph != null) {
			final int left = this.leftPos + FILM_RIGHT_PHOTOGRAPH_X;
			final int right = left + FILM_PHOTOGRAPH_SIZE;
			minX = Math.min(minX, left);
			maxX = Math.max(maxX, right);
			final double distance = Math.abs(mouseX - (left + (FILM_PHOTOGRAPH_SIZE / 2.0)));
			if (distance < closestDistance) {
				closestDistance = distance;
				closestOffset = 1;
			}
		}

		if (closestOffset == Integer.MIN_VALUE) return Integer.MIN_VALUE;
		if (mouseX < minX || mouseX >= maxX) return Integer.MIN_VALUE;
		return closestOffset;
	}

	private boolean isAtBeginning() {
		return this.displayFilm && this.filmContents != null && !this.filmContents.isEmpty() && this.photographIndex <= 0;
	}

	private boolean isAtEnd() {
		return this.displayFilm && this.filmContents != null && !this.filmContents.isEmpty() && this.photographIndex >= this.getMaxPhotographIndex();
	}

	private boolean isFilmFull() {
		return this.filmContents != null && this.filmContents.size() >= this.filmMaxPhotographs;
	}

	private void containerChanged() {
		if (!this.menu.hasSourceItem()) {
			this.filmContents = null;
			this.leftPhotograph = null;
			this.middlePhotograph = null;
			this.rightPhotograph = null;
			this.photographIndex = 0;
			this.displayFilm = false;
			this.draggingScroller = false;
			this.scrollerX = SCROLLER_TRACK_X;
			this.photographCopyId = null;
			this.lastSourceItem = ItemStack.EMPTY;
			return;
		}

		final ItemStack sourceItem = this.menu.getSourceItem();
		final boolean sourceChanged = !ItemStack.isSameItemSameComponents(sourceItem, this.lastSourceItem);
		if (sourceChanged) {
			this.photographIndex = 0;
			this.draggingScroller = false;
			this.scrollerX = SCROLLER_TRACK_X;
			this.setupDataAndResultSlot(0);
		}

		this.setupOrClearFilmPhotographDisplays();

		if (sourceItem.is(CameraPortItems.PHOTOGRAPH)) {
			final Photograph photograph = sourceItem.get(CameraPortDataComponents.PHOTOGRAPH);
			this.photographCopyId = (photograph == null || !photograph.canCopy()) ? null : photograph.identifier();
		} else {
			this.photographCopyId = null;
		}

		this.lastSourceItem = sourceItem.copyWithCount(1);
	}

	private void setupOrClearFilmPhotographDisplays() {
		final ItemStack sourceItem = this.menu.getSourceItem();
		this.displayFilm = sourceItem.is(CameraPortItems.FILM) && sourceItem.has(CameraPortDataComponents.FILM_CONTENTS);
		if (!this.displayFilm) {
			this.filmContents = null;
			this.filmMaxPhotographs = FilmContents.BASE_MAX_PHOTOGRAPHS;
			this.leftPhotograph = null;
			this.middlePhotograph = null;
			this.rightPhotograph = null;
			this.photographIndex = 0;
			this.draggingScroller = false;
			this.scrollerX = SCROLLER_TRACK_X;
		} else {
			this.filmContents = sourceItem.get(CameraPortDataComponents.FILM_CONTENTS);
			this.filmMaxPhotographs = FilmItem.getMaxPhotographs(sourceItem);
			this.photographIndex = Math.max(0, Math.min(this.photographIndex, this.getMaxPhotographIndex()));
			this.middlePhotograph = this.getFilmPhotograph(this.photographIndex);
			this.rightPhotograph = this.getFilmPhotograph(this.photographIndex + 1);
			this.leftPhotograph = this.getFilmPhotograph(this.photographIndex - 1);
			if (this.filmContents != null) this.filmContents.photographs().forEach(photograph -> PhotographLoader.getAndLoadPhotograph(photograph.identifier()));
		}
	}
}
