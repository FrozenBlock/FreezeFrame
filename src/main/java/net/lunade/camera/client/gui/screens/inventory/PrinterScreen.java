package net.lunade.camera.client.gui.screens.inventory;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PrinterScreen extends AbstractContainerScreen<PrinterMenu> {
	private static final int ARROW_BOX_SIZE = 32;
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/container/printer.png");
	private static final Identifier TEXTURE_FILM = CameraPortConstants.id("textures/gui/container/printer_film.png");
	private static final Identifier MOVE_RIGHT = CameraPortConstants.id("container/printer/move_right");
	private static final Identifier MOVE_RIGHT_SELECTED = CameraPortConstants.id("container/printer/move_right_highlighted");
	private static final Identifier MOVE_LEFT = CameraPortConstants.id("container/printer/move_left");
	private static final Identifier MOVE_LEFT_SELECTED = CameraPortConstants.id("container/printer/move_left_highlighted");
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
		super(menu, inventory, title, DEFAULT_IMAGE_WIDTH, 222);
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

	private void setupDataAndResultSlot(int photographIndex) {
		ClientPlayNetworking.send(new PrinterSyncSelectPhotographIndexPacket(photographIndex));
		this.menu.setupDataAndResultSlot(photographIndex);
		setupOrClearFilmPhotographDisplays();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		final int leftPos = this.leftPos;
		final int topPos = this.topPos;

		final Identifier bgTexture = this.photographCopyId == null ? TEXTURE_FILM : TEXTURE;
		graphics.blit(RenderPipelines.GUI_TEXTURED, bgTexture, leftPos, topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);

		renderFilmPhotographs: {
			if (!this.displayFilm) break renderFilmPhotographs;

			if (this.middlePhotograph != null) PhotographRenderer.blit(leftPos, topPos, 65, 43, graphics, this.middlePhotograph, 48, PhotographRenderer.FrameType.FILM_EMBED);

			if (this.filmContents.size() == 1) return;

			if (this.rightPhotograph != null) {
				// Render right photograph
				PhotographRenderer.blit(leftPos, topPos, 132, 51, graphics, this.rightPhotograph, ARROW_BOX_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
				// Render right arrow
				boolean selected = checkButtonClicked(leftPos + 132, topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_RIGHT_SELECTED : MOVE_RIGHT, leftPos + 132 + 12, topPos + 51 + 8, 12, 17);
			}

			if (this.leftPhotograph != null) {
				// Render left photograph
				PhotographRenderer.blit(leftPos, topPos, 14, 51, graphics, this.leftPhotograph, ARROW_BOX_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
				// Render left arrow
				boolean selected = checkButtonClicked(leftPos + 14, topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_LEFT_SELECTED : MOVE_LEFT, leftPos + 14 + 8, topPos + 51 + 8, 12, 17);
			}
		}

		renderPhotographCopy: {
			if (this.photographCopyId == null) break renderPhotographCopy;
			PhotographRenderer.blit(leftPos, topPos, 52, 20, graphics, this.photographCopyId, 72, PhotographRenderer.FrameType.FRAME);
		}
	}

	@Nullable
	private Identifier getInfiniteFilmPhotograph(int index) {
		if (this.filmContents == null || this.filmContents.isEmpty()) return null;
		int size = this.filmContents.size();
		int adjustedIndex = ((index % size) + size) % size;
		return this.filmContents.getPhotographAtIndex(adjustedIndex).identifier();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!this.displayFilm) return super.mouseClicked(event, doubleClick);

		final int mouseX = (int) event.x();
		final int mouseY = (int) event.y();

		// Right arrow clicked
		if (this.rightPhotograph != null && checkButtonClicked(this.leftPos + 132, this.topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY)) {
			if (this.photographIndex == this.filmContents.size() - 1) {
				this.photographIndex = 0;
			} else {
				this.photographIndex++;
			}

			this.setupDataAndResultSlot(this.photographIndex);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		// Left arrow clicked
		if (this.leftPhotograph != null && checkButtonClicked(this.leftPos + 14, this.topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY)) {
			if (this.photographIndex == 0) {
				this.photographIndex = this.filmContents.size() - 1;
			} else {
				this.photographIndex--;
			}

			this.setupDataAndResultSlot(this.photographIndex);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private static boolean checkButtonClicked(int minX, int minY, int buttonWidth, int buttonHeight, int mouseX, int mouseY) {
		return mouseX >= minX && mouseX <= minX + buttonWidth && mouseY >= minY && mouseY <= minY + buttonHeight;
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
