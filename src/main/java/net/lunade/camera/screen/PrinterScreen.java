package net.lunade.camera.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.photograph.PhotographLoader;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.menu.PrinterMenu;
import net.lunade.camera.networking.PrinterAskForSlotsPacket;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PrinterScreen extends AbstractContainerScreen<PrinterMenu> {
	private static final int ARROW_BOX_SIZE = 32;
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/container/printer.png");
	private static final Identifier TEXTURE_FILM = CameraPortConstants.id("textures/gui/container/printer_film.png");
	private static final Identifier MOVE_RIGHT = CameraPortConstants.id("container/printer/move_right");
	private static final Identifier MOVE_RIGHT_SELECTED = CameraPortConstants.id("container/printer/move_right_highlighted");
	private static final Identifier MOVE_LEFT = CameraPortConstants.id("container/printer/move_left");
	private static final Identifier MOVE_LEFT_SELECTED = CameraPortConstants.id("container/printer/move_left_highlighted");
	private final Player player;
	int photographIndex = 0;
	private boolean displayFilm = false;
	private Identifier photographCopyId;

	public PrinterScreen(PrinterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, DEFAULT_IMAGE_WIDTH, 222);
		this.player = inventory.player;
		final int size = PhotographLoader.loadLocalPhotographs();
		if (PhotographLoader.hasAnyLocalPhotographs()) {
			final String selected = PhotographLoader.getPhotograph(0).getPath();
			this.setupDataAndResultSlot(size, selected);
		} else {
			this.setupDataAndResultSlot(0, "");
		}
		menu.registerUpdateListener(this::containerChanged);
		--this.titleLabelY;
		this.titleLabelX += 78;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	private void setupDataAndResultSlot(int size, String selected) {
		ClientPlayNetworking.send(new PrinterAskForSlotsPacket(size, selected));
		this.menu.setupDataAndResultSlot(this.player, size, selected);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		final int leftPos = this.leftPos;
		final int topPos = this.topPos;

		final Identifier bgTexture = this.photographCopyId == null ? TEXTURE_FILM : TEXTURE;
		graphics.blit(RenderPipelines.GUI_TEXTURED, bgTexture, leftPos, topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);

		renderFilmPhotographs: {
			if (!this.displayFilm) break renderFilmPhotographs;

			final int size = PhotographLoader.getSize();
			Identifier middle = PhotographLoader.getInfiniteLocalPhotograph(this.photographIndex);
			if (middle != null) PhotographRenderer.blit(leftPos, topPos, 65, 43, graphics, middle, 48, PhotographRenderer.FrameType.FILM_EMBED);

			if (size == 1) return;

			final Identifier right = PhotographLoader.getInfiniteLocalPhotograph(this.photographIndex + 1);
			if (right != null) {
				// Render right photograph
				PhotographRenderer.blit(leftPos, topPos, 132, 51, graphics, right, ARROW_BOX_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
				// Render right arrow
				boolean selected = checkButtonClicked(leftPos + 132, topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_RIGHT_SELECTED : MOVE_RIGHT, leftPos + 132 + 12, topPos + 51 + 8, 12, 17);
			}

			final Identifier left = PhotographLoader.getInfiniteLocalPhotograph(this.photographIndex - 1);
			if (left != null) {
				// Render left photograph
				PhotographRenderer.blit(leftPos, topPos, 14, 51, graphics, left, ARROW_BOX_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
				// Render left arrow
				boolean selected = checkButtonClicked(leftPos + 14, topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_LEFT_SELECTED : MOVE_LEFT, leftPos + 14 + 8, topPos + 51 + 8, 12, 17);
			}
		}

		renderPhotographCopy: {
			if (this.photographCopyId == null) break renderPhotographCopy;
			PhotographRenderer.blit(leftPos, topPos, 48, 20, graphics, this.photographCopyId, 80, PhotographRenderer.FrameType.FRAME);
		}
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
		if (checkButtonClicked(this.leftPos + 132, this.topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY)) {
			if (this.photographIndex == PhotographLoader.getSize() - 1) {
				this.photographIndex = 0;
			} else {
				this.photographIndex++;
			}

			this.setupDataAndResultSlot(PhotographLoader.getSize(), PhotographLoader.getPhotograph(this.photographIndex).getPath());
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		// Left arrow clicked
		if (checkButtonClicked(this.leftPos + 14, this.topPos + 51, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY)) {
			if (this.photographIndex == 0) {
				this.photographIndex = PhotographLoader.getSize() - 1;
			} else {
				this.photographIndex--;
			}

			this.setupDataAndResultSlot(PhotographLoader.getSize(), PhotographLoader.getPhotograph(this.photographIndex).getPath());
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
			this.displayFilm = false;
			this.photographIndex = 0;
			this.photographCopyId = null;
			return;
		}

		final ItemStack sourceItem = this.menu.getSourceItem();
		this.displayFilm = sourceItem.is(CameraPortItems.CAMERA) && this.menu.hasPhotographSlots();
		if (!this.displayFilm) this.photographIndex = 0;

		if (sourceItem.is(CameraPortItems.PHOTOGRAPH)) {
			final PhotographComponent photographComponent = sourceItem.get(CameraPortItems.PHOTO_COMPONENT);
			this.photographCopyId = (photographComponent == null || photographComponent.isCopy()) ? null : photographComponent.identifier();
		} else {
			this.photographCopyId = null;
		}
	}
}
