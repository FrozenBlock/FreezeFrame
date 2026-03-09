package net.lunade.camera.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.photograph.PhotographLoader;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.menu.PrinterMenu;
import net.lunade.camera.networking.PrinterAskForSlotsPacket;
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
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class PrinterScreen extends AbstractContainerScreen<PrinterMenu> {
	int index = 0;
	private boolean displayRecipes = false;
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/printer.png");
	private static final Identifier MOVE_RIGHT = CameraPortConstants.id("printer/move_right");
	private static final Identifier MOVE_RIGHT_SELECTED = CameraPortConstants.id("printer/move_right_highlighted");
	private static final Identifier MOVE_LEFT = CameraPortConstants.id("printer/move_left");
	private static final Identifier MOVE_LEFT_SELECTED = CameraPortConstants.id("printer/move_left_highlighted");

	public PrinterScreen(PrinterMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		final int size = PhotographLoader.loadLocalPhotographs();
		if (PhotographLoader.hasAnyLocalPhotographs()) {
			final String selected = PhotographLoader.getPhotograph(0).getPath();
			this.send(size, selected);
		} else {
			this.send(0, "");
		}
		menu.registerUpdateListener(this::containerChanged);
		--this.titleLabelY;
		this.inventoryLabelY += 56;
		this.imageHeight = 222;
	}

	private void send(int size, String selected) {
		ClientPlayNetworking.send(new PrinterAskForSlotsPacket(size, selected));
		this.menu.onClient(selected);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);
		if (!this.displayRecipes) return;

		final int size = PhotographLoader.getSize();
		Identifier middle = PhotographLoader.getInfiniteLocalPhotograph(this.index);
		if (middle != null) PhotographRenderer.render(i, j, 64, 53, graphics, middle, 48, true);

		if (size == 1) return;

		final Identifier right = PhotographLoader.getInfiniteLocalPhotograph(this.index + 1);
		if (right != null) {
			// Render right photograph
			PhotographRenderer.render(i, j, 119, 61, graphics, right, 32, true);
			// Render right arrow
			boolean selected = isIn(i + 119, j + 61, 32, 32, mouseX, mouseY);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_RIGHT_SELECTED : MOVE_RIGHT, i + 119, j + 61, 32, 32);
		}

		final Identifier left = PhotographLoader.getInfiniteLocalPhotograph(this.index - 1);
		if (left != null) {
			// Render left photograph
			PhotographRenderer.render(i, j, 25, 61, graphics, left, 32, true);
			// Render left arrow
			boolean selected = isIn(i + 25, j + 61, 32, 32, mouseX, mouseY);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_LEFT_SELECTED : MOVE_LEFT, i + 25, j + 61, 32, 32);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		final int mouseX = (int) event.x();
		final int mouseY = (int) event.y();

		if (isIn(this.leftPos + 119, this.topPos + 61, 32, 32, mouseX, mouseY)) {
			if (this.index == PhotographLoader.getSize() - 1) {
				this.index = 0;
			} else {
				this.index++;
			}

			this.send(PhotographLoader.getSize(), PhotographLoader.getPhotograph(this.index).getPath());
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		} else if (isIn(this.leftPos + 25, this.topPos + 61, 32, 32, mouseX, mouseY)) {
			if (this.index == 0) {
				this.index = PhotographLoader.getSize() - 1;
			} else {
				this.index--;
			}

			this.send(PhotographLoader.getSize(), PhotographLoader.getPhotograph(this.index).getPath());
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@SuppressWarnings("all")
	private static boolean isIn(int minX, int minY, int w, int h, int x, int y) {
		return x >= minX && x <= minX + w && y >= minY && y <= minY + h;
	}

	private void containerChanged() {
		this.displayRecipes = this.menu.hasInputItem() && this.menu.getInputItem().is(Items.PAPER);
		if (!this.displayRecipes) this.index = 0;
	}
}
