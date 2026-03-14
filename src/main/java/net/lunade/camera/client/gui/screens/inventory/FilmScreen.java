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
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.photograph.PhotographHoverTooltipRenderer;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.Photograph;
import net.lunade.camera.item.FilmItem;
import net.lunade.camera.networking.packet.SaveFilmChangesPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Environment(EnvType.CLIENT)
public class FilmScreen extends Screen {
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/container/film.png");
	private static final Identifier SCROLLER = CameraPortConstants.id("container/film/scroller");
	private static final Identifier SCROLLER_DISABLED = CameraPortConstants.id("container/film/scroller_disabled");
	private static final Identifier FILM_PHOTOGRAPH_BLOCKER = CameraPortConstants.id("container/film/film_photograph_blocker");
	private static final Identifier FILM_PHOTOGRAPH_HIGHLIGHT = CameraPortConstants.id("container/film/film_photograph_highlight");
	private static final Identifier DELETE_BUTTON = CameraPortConstants.id("container/film/delete_button");
	private static final Identifier DELETE_BUTTON_HOVER = CameraPortConstants.id("container/film/delete_button_hover");
	private static final int TEXTURE_WIDTH = 256;
	private static final int TEXTURE_HEIGHT = 256;
	private static final int BACKGROUND_WIDTH = 256;
	private static final int BACKGROUND_HEIGHT = 128;
	private static final int CONTENT_HEIGHT = 160;
	private static final int FILM_PHOTOGRAPH_SIZE = 78;
	private static final int FILM_PHOTOGRAPH_HIGHLIGHT_SIZE = 78;
	private static final int FILM_PHOTOGRAPH_Y = 20;
	private static final int FILM_LEFT_PHOTOGRAPH_X = 9;
	private static final int FILM_MIDDLE_PHOTOGRAPH_X = 90;
	private static final int FILM_RIGHT_PHOTOGRAPH_X = 171;
	private static final int FILM_PHOTOGRAPH_BLOCKER_OFFSET = -1;
	private static final int SCROLLER_WIDTH = 19;
	private static final int SCROLLER_HEIGHT = 10;
	private static final int SCROLLER_TRACK_X = FILM_LEFT_PHOTOGRAPH_X - 1;
	private static final int SCROLLER_TRACK_Y = 96;
	private static final int SCROLLER_TRACK_WIDTH = FILM_RIGHT_PHOTOGRAPH_X + FILM_PHOTOGRAPH_SIZE - FILM_LEFT_PHOTOGRAPH_X + 2;
	private static final int NAME_LABEL_X = 17;
	private static final int NAME_LABEL_Y = 111;
	private static final int NAME_BOX_X = 17;
	private static final int NAME_BOX_Y = 102;
	private static final int NAME_BOX_WIDTH = 204;
	private static final int NAME_BOX_HEIGHT = 20;
	private static final int DELETE_BUTTON_X = 226;
	private static final int DELETE_BUTTON_Y = 105;
	private static final int DELETE_BUTTON_WIDTH = 13;
	private static final int DELETE_BUTTON_HEIGHT = 14;
	private static final int ACTION_BUTTON_Y = BACKGROUND_HEIGHT + 8;
	private static final int ACTION_BUTTON_WIDTH = 98;
	private static final int ACTION_BUTTON_HEIGHT = 20;
	private static final int SAVE_BUTTON_X = 17;
	private static final int CANCEL_BUTTON_X = 141;
	private static final int MAX_NAME_LENGTH = 64;
	private final Player owner;
	private final InteractionHand hand;
	private final ScrollWheelHandler scrollWheelHandler = new ScrollWheelHandler();
	private final List<Photograph> photographs = new ArrayList<>();
	private int leftPos;
	private int topPos;
	private int selectedPhotographIndex;
	private int filmMaxPhotographs;
	private int scrollerX = SCROLLER_TRACK_X;
	private boolean draggingScroller;
	private EditBox nameEditBox;
	private Button saveButton;
	private Button cancelButton;

	@Nullable
	private Identifier leftPhotograph;
	@Nullable
	private Identifier middlePhotograph;
	@Nullable
	private Identifier rightPhotograph;

	public FilmScreen(Player owner, InteractionHand hand) {
		super(Component.translatable("screen.camera_port.film.title"));
		this.owner = owner;
		this.hand = hand;
		this.reloadFromHeldFilm();
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
		this.topPos = (this.height - CONTENT_HEIGHT) / 2;

		this.nameEditBox = new EditBox(
			this.font,
			this.leftPos + NAME_BOX_X,
			this.topPos + NAME_BOX_Y,
			NAME_BOX_WIDTH,
			NAME_BOX_HEIGHT,
			Component.translatable("screen.camera_port.film.name")
		);
		this.nameEditBox.setMaxLength(MAX_NAME_LENGTH);
		this.nameEditBox.setCanLoseFocus(false);
		this.nameEditBox.setBordered(true);
		this.nameEditBox.setTextColor(0xFF303030);
		this.nameEditBox.setTextColorUneditable(0xFF303030);
		this.nameEditBox.setResponder(this::onNameChanged);
		this.addRenderableWidget(this.nameEditBox);

		this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.camera_port.film.save"), button -> this.saveChangesAndClose())
			.bounds(this.leftPos + SAVE_BUTTON_X, this.topPos + ACTION_BUTTON_Y, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
			.build());
		this.cancelButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.camera_port.film.cancel"), button -> this.cancelChangesAndClose())
			.bounds(this.leftPos + CANCEL_BUTTON_X, this.topPos + ACTION_BUTTON_Y, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
			.build());

		this.syncNameEditBox();
		this.setupOrClearFilmPhotographDisplays();
		this.updateButtons();
		this.setInitialFocus(this.nameEditBox);
		this.nameEditBox.setFocused(true);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.nameEditBox.isFocused()) this.nameEditBox.setFocused(true);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		this.cancelChangesAndClose();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0F, 0F, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.extractTransparentBackground(graphics);
		this.extractBackground(graphics, mouseX, mouseY, delta);
		Photograph hoveredPhotograph = null;

		if (this.isAtBeginning()) {
			this.extractFilmPhotographBlocker(graphics, FILM_LEFT_PHOTOGRAPH_X);
		}

		if (this.isAtEnd() && this.isFilmFull()) {
			this.extractFilmPhotographBlocker(graphics, FILM_RIGHT_PHOTOGRAPH_X);
		}

		boolean alreadyHovering = false;
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
			if (this.isHovering(FILM_MIDDLE_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
				alreadyHovering = true;
				hoveredPhotograph = this.getFilmPhotographComponent(this.selectedPhotographIndex);
				graphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					FILM_PHOTOGRAPH_HIGHLIGHT,
					this.leftPos + FILM_MIDDLE_PHOTOGRAPH_X + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
					this.topPos + FILM_PHOTOGRAPH_Y + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
					FILM_PHOTOGRAPH_HIGHLIGHT_SIZE,
					FILM_PHOTOGRAPH_HIGHLIGHT_SIZE
				);
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
					PhotographRenderer.FrameType.FILM_EMBED
				);
				if (!alreadyHovering && this.isHovering(FILM_RIGHT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
					alreadyHovering = true;
					hoveredPhotograph = this.getFilmPhotographComponent(this.selectedPhotographIndex + 1);
					graphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						FILM_PHOTOGRAPH_HIGHLIGHT,
						this.leftPos + FILM_RIGHT_PHOTOGRAPH_X + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
						this.topPos + FILM_PHOTOGRAPH_Y + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
						FILM_PHOTOGRAPH_HIGHLIGHT_SIZE,
						FILM_PHOTOGRAPH_HIGHLIGHT_SIZE
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
					PhotographRenderer.FrameType.FILM_EMBED
				);
				if (!alreadyHovering && this.isHovering(FILM_LEFT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
					alreadyHovering = true;
					hoveredPhotograph = this.getFilmPhotographComponent(this.selectedPhotographIndex - 1);
					graphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						FILM_PHOTOGRAPH_HIGHLIGHT,
						this.leftPos + FILM_LEFT_PHOTOGRAPH_X + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
						this.topPos + FILM_PHOTOGRAPH_Y + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
						FILM_PHOTOGRAPH_HIGHLIGHT_SIZE,
						FILM_PHOTOGRAPH_HIGHLIGHT_SIZE
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

		final boolean hoveringDeleteButton = this.isHovering(DELETE_BUTTON_X, DELETE_BUTTON_Y, DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT, mouseX, mouseY) && this.hasPhotographs();
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			hoveringDeleteButton ? DELETE_BUTTON_HOVER : DELETE_BUTTON,
			this.leftPos + DELETE_BUTTON_X,
			this.topPos + DELETE_BUTTON_Y,
			DELETE_BUTTON_WIDTH,
			DELETE_BUTTON_HEIGHT
		);
		if (hoveringDeleteButton) graphics.requestCursor(CursorTypes.POINTING_HAND);

		graphics.text(this.font, Component.translatable("screen.camera_port.film.name"), this.leftPos + NAME_LABEL_X, this.topPos + NAME_LABEL_Y, 0x3f3f3f, false);
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		if (hoveredPhotograph != null) {
			PhotographHoverTooltipRenderer.extractRenderState(graphics, this.font, this.width, this.height, mouseX, mouseY, hoveredPhotograph);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		final double mouseX = event.x();
		final double mouseY = event.y();
		final int button = event.button();

		if (button == 0 && this.hasPhotographs() && this.isHovering(DELETE_BUTTON_X, DELETE_BUTTON_Y, DELETE_BUTTON_WIDTH, DELETE_BUTTON_HEIGHT, mouseX, mouseY)) {
			this.deleteSelectedPhotograph();
			return true;
		}

		if (button == 0 && this.hasMultipleFilmPhotographs()) {
			if (this.isHovering(SCROLLER_TRACK_X, SCROLLER_TRACK_Y, SCROLLER_TRACK_WIDTH, SCROLLER_HEIGHT, mouseX, mouseY)
				|| this.isHovering(this.getScrollerX(), SCROLLER_TRACK_Y, SCROLLER_WIDTH, SCROLLER_HEIGHT, mouseX, mouseY)) {
				this.draggingScroller = true;
				this.updatePhotographIndexFromScroller((int) mouseX);
				return true;
			}
		}

		if (button == 0 && this.isHovering(FILM_MIDDLE_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(0);
			return true;
		}

		if (button == 0 && this.rightPhotograph != null && this.isHovering(FILM_RIGHT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(1);
			return true;
		}

		if (button == 0 && this.leftPhotograph != null && this.isHovering(FILM_LEFT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, FILM_PHOTOGRAPH_SIZE, FILM_PHOTOGRAPH_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(-1);
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.draggingScroller && event.button() == 0) {
			this.draggingScroller = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (this.draggingScroller && event.button() == 0 && this.hasMultipleFilmPhotographs()) {
			this.updatePhotographIndexFromScroller((int) event.x());
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		if (!this.hasPhotographs()) return false;

		final Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		final int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
		if (wheel != 0) {
			final int currentIndex = this.selectedPhotographIndex;
			final int updatedIndex = Mth.clamp(currentIndex - wheel, 0, this.getMaxPhotographIndex());
			if (currentIndex != updatedIndex) {
				this.selectedPhotographIndex = updatedIndex;
				this.onSelectedPhotographChanged();
			}
		}

		return true;
	}

	private void saveChangesAndClose() {
		if (this.minecraft == null) return;
		ClientPlayNetworking.send(new SaveFilmChangesPacket(this.hand, this.toFilmContents()));
		this.minecraft.setScreen(null);
	}

	private void cancelChangesAndClose() {
		if (this.minecraft == null) return;
		this.minecraft.setScreen(null);
	}

	private FilmContents toFilmContents() {
		final FilmContents.Mutable mutable = new FilmContents.Mutable(new FilmContents(this.photographs), this.filmMaxPhotographs);
		if (!this.photographs.isEmpty()) mutable.toggleSelectedPhotograph(this.selectedPhotographIndex);
		return mutable.toImmutable();
	}

	private void deleteSelectedPhotograph() {
		if (!this.hasPhotographs()) return;

		this.photographs.remove(this.selectedPhotographIndex);
		if (this.photographs.isEmpty()) {
			this.selectedPhotographIndex = 0;
		} else {
			this.selectedPhotographIndex = Math.min(this.selectedPhotographIndex, this.photographs.size() - 1);
		}

		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
		this.onSelectedPhotographChanged();
	}

	private void onNameChanged(String updatedName) {
		if (!this.hasPhotographs()) return;
		final Photograph current = this.photographs.get(this.selectedPhotographIndex);
		this.photographs.set(this.selectedPhotographIndex, current.withName(updatedName));
	}

	private void incrementPhotographIndex(int amount) {
		if (!this.hasPhotographs()) return;
		final int updatedIndex = amount == 0 ? this.selectedPhotographIndex : Mth.clamp(this.selectedPhotographIndex + amount, 0, this.getMaxPhotographIndex());
		if (updatedIndex == this.selectedPhotographIndex) return;
		this.selectedPhotographIndex = updatedIndex;
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
		this.onSelectedPhotographChanged();
	}

	private void onSelectedPhotographChanged() {
		this.setupOrClearFilmPhotographDisplays();
		this.syncNameEditBox();
		this.updateScrollerXFromPhotographIndex();
		this.updateButtons();
	}

	private void syncNameEditBox() {
		if (this.nameEditBox == null) return;
		if (!this.hasPhotographs()) {
			this.nameEditBox.setEditable(false);
			this.nameEditBox.setValue("");
			return;
		}

		this.nameEditBox.setEditable(true);
		this.nameEditBox.setValue(this.photographs.get(this.selectedPhotographIndex).name());
		this.nameEditBox.setFocused(true);
	}

	private void updateButtons() {
		final boolean canEdit = this.hasPhotographs();
		if (this.saveButton != null) this.saveButton.active = true;
		if (this.cancelButton != null) this.cancelButton.active = true;
		if (this.nameEditBox != null) this.nameEditBox.setEditable(canEdit);
	}

	private void reloadFromHeldFilm() {
		final ItemStack filmStack = this.owner.getItemInHand(this.hand);
		if (!filmStack.isEmpty()) {
			final FilmContents contents = filmStack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
			this.photographs.clear();
			this.photographs.addAll(contents.photographs());
			this.selectedPhotographIndex = Mth.clamp(contents.getSelectedPhotographIndex(), 0, Math.max(0, this.photographs.size() - 1));
			this.filmMaxPhotographs = FilmItem.getMaxPhotographs(filmStack);
		} else {
			this.photographs.clear();
			this.selectedPhotographIndex = 0;
			this.filmMaxPhotographs = FilmContents.BASE_MAX_PHOTOGRAPHS;
		}
	}

	private void setupOrClearFilmPhotographDisplays() {
		if (!this.hasPhotographs()) {
			this.leftPhotograph = null;
			this.middlePhotograph = null;
			this.rightPhotograph = null;
			this.draggingScroller = false;
			this.scrollerX = SCROLLER_TRACK_X;
			return;
		}

		this.selectedPhotographIndex = Mth.clamp(this.selectedPhotographIndex, 0, this.getMaxPhotographIndex());
		this.middlePhotograph = this.getFilmPhotograph(this.selectedPhotographIndex);
		this.rightPhotograph = this.getFilmPhotograph(this.selectedPhotographIndex + 1);
		this.leftPhotograph = this.getFilmPhotograph(this.selectedPhotographIndex - 1);
	}

	@Nullable
	private Identifier getFilmPhotograph(int index) {
		if (index < 0 || index >= this.photographs.size()) return null;
		return this.photographs.get(index).identifier();
	}

	@Nullable
	private Photograph getFilmPhotographComponent(int index) {
		if (index < 0 || index >= this.photographs.size()) return null;
		return this.photographs.get(index);
	}

	private boolean hasPhotographs() {
		return !this.photographs.isEmpty();
	}

	private boolean hasMultipleFilmPhotographs() {
		return this.photographs.size() > 1;
	}

	private int getMaxPhotographIndex() {
		if (!this.hasPhotographs()) return 0;
		return this.photographs.size() - 1;
	}

	private int getScrollerX() {
		if (!this.hasMultipleFilmPhotographs()) return SCROLLER_TRACK_X;
		return this.scrollerX;
	}

	private void updatePhotographIndexFromScroller(int mouseX) {
		if (!this.hasMultipleFilmPhotographs()) return;

		final int travel = SCROLLER_TRACK_WIDTH - SCROLLER_WIDTH;
		final int relative = Mth.clamp(mouseX - this.leftPos - SCROLLER_TRACK_X - (SCROLLER_WIDTH / 2), 0, travel);
		final int updatedIndex = Math.round((relative / (float) travel) * this.getMaxPhotographIndex());
		this.scrollerX = SCROLLER_TRACK_X + relative;
		if (updatedIndex != this.selectedPhotographIndex) {
			this.selectedPhotographIndex = updatedIndex;
			this.onSelectedPhotographChanged();
		}
	}

	private void updateScrollerXFromPhotographIndex() {
		final int travel = SCROLLER_TRACK_WIDTH - SCROLLER_WIDTH;
		if (!this.hasMultipleFilmPhotographs() || travel <= 0) {
			this.scrollerX = SCROLLER_TRACK_X;
			return;
		}

		final float progress = this.selectedPhotographIndex / (float) this.getMaxPhotographIndex();
		this.scrollerX = SCROLLER_TRACK_X + Math.round(progress * travel);
	}

	private void extractFilmPhotographBlocker(GuiGraphicsExtractor graphics, int slotX) {
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			FILM_PHOTOGRAPH_BLOCKER,
			this.leftPos + slotX + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
			this.topPos + FILM_PHOTOGRAPH_Y + FILM_PHOTOGRAPH_BLOCKER_OFFSET,
			FILM_PHOTOGRAPH_HIGHLIGHT_SIZE,
			FILM_PHOTOGRAPH_HIGHLIGHT_SIZE
		);
	}

	private boolean isAtBeginning() {
		return this.hasPhotographs() && this.selectedPhotographIndex <= 0;
	}

	private boolean isAtEnd() {
		return this.hasPhotographs() && this.selectedPhotographIndex >= this.getMaxPhotographIndex();
	}

	private boolean isFilmFull() {
		return this.photographs.size() >= this.filmMaxPhotographs;
	}

	private boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
		return mouseX >= this.leftPos + x && mouseX < this.leftPos + x + width && mouseY >= this.topPos + y && mouseY < this.topPos + y + height;
	}
}
