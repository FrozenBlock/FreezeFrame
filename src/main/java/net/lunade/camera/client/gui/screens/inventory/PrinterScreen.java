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

import java.util.List;

@Environment(EnvType.CLIENT)
public class PrinterScreen extends AbstractContainerScreen<PrinterMenu> {
	private static final int ARROW_BOX_SIZE = 52;
	private static final int FILM_PHOTOGRAPH_SIZE = 52;
	private static final int FILM_PHOTOGRAPH_Y = 39;
	private static final int FILM_LEFT_PHOTOGRAPH_X = 5;
	private static final int FILM_MIDDLE_PHOTOGRAPH_X = 62;
	private static final int FILM_RIGHT_PHOTOGRAPH_X = 119;
	private static final int ARROW_WIDTH = 12;
	private static final int ARROW_HEIGHT = 17;
	private static final int ARROW_X_OFFSET = (ARROW_BOX_SIZE - ARROW_WIDTH) / 2;
	private static final int ARROW_Y_OFFSET = (ARROW_BOX_SIZE - ARROW_HEIGHT) / 2;
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/gui/container/printer.png");
	private static final Identifier TEXTURE_FILM = CameraPortConstants.id("textures/gui/container/printer_film.png");
	private static final Identifier MOVE_RIGHT = CameraPortConstants.id("container/printer/move_right");
	private static final Identifier MOVE_RIGHT_SELECTED = CameraPortConstants.id("container/printer/move_right_highlighted");
	private static final Identifier MOVE_LEFT = CameraPortConstants.id("container/printer/move_left");
	private static final Identifier MOVE_LEFT_SELECTED = CameraPortConstants.id("container/printer/move_left_highlighted");
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
		super(menu, inventory, title, DEFAULT_IMAGE_WIDTH, 224);
		this.scrollWheelHandler = new ScrollWheelHandler();
		menu.registerUpdateListener(this::containerChanged);
		--this.titleLabelY;
		this.titleLabelX += 78;
		this.inventoryLabelY += 2;
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
		final int leftPos = this.leftPos;
		final int topPos = this.topPos;

		final Identifier bgTexture = this.displayFilm ? TEXTURE_FILM : TEXTURE;
		graphics.blit(RenderPipelines.GUI_TEXTURED, bgTexture, leftPos, topPos, 0F, 0F, this.imageWidth, this.imageHeight, 256, 256);
		this.sourceSlotBackground.render(this.menu, graphics, delta, leftPos, topPos);
		this.paperSlotBackground.render(this.menu, graphics, delta, leftPos, topPos);

		renderFilmPhotographs: {
			if (!this.displayFilm) break renderFilmPhotographs;

			if (this.middlePhotograph != null) {
				PhotographRenderer.blit(leftPos, topPos, FILM_MIDDLE_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, graphics, this.middlePhotograph, FILM_PHOTOGRAPH_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
			}

			if (this.filmContents.size() == 1) return;

			if (this.rightPhotograph != null) {
				// Render right photograph
				PhotographRenderer.blit(leftPos, topPos, FILM_RIGHT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, graphics, this.rightPhotograph, FILM_PHOTOGRAPH_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
				// Render right arrow
				boolean selected = checkButtonClicked(leftPos + FILM_RIGHT_PHOTOGRAPH_X, topPos + FILM_PHOTOGRAPH_Y, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_RIGHT_SELECTED : MOVE_RIGHT, leftPos + FILM_RIGHT_PHOTOGRAPH_X + ARROW_X_OFFSET, topPos + FILM_PHOTOGRAPH_Y + ARROW_Y_OFFSET, ARROW_WIDTH, ARROW_HEIGHT);
			}

			if (this.leftPhotograph != null) {
				// Render left photograph
				PhotographRenderer.blit(leftPos, topPos, FILM_LEFT_PHOTOGRAPH_X, FILM_PHOTOGRAPH_Y, graphics, this.leftPhotograph, FILM_PHOTOGRAPH_SIZE, PhotographRenderer.FrameType.FILM_EMBED);
				// Render left arrow
				boolean selected = checkButtonClicked(leftPos + FILM_LEFT_PHOTOGRAPH_X, topPos + FILM_PHOTOGRAPH_Y, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, selected ? MOVE_LEFT_SELECTED : MOVE_LEFT, leftPos + FILM_LEFT_PHOTOGRAPH_X + ARROW_X_OFFSET, topPos + FILM_PHOTOGRAPH_Y + ARROW_Y_OFFSET, ARROW_WIDTH, ARROW_HEIGHT);
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
		if (this.rightPhotograph != null && checkButtonClicked(this.leftPos + FILM_RIGHT_PHOTOGRAPH_X, this.topPos + FILM_PHOTOGRAPH_Y, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(false);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		// Left arrow clicked
		if (this.leftPhotograph != null && checkButtonClicked(this.leftPos + FILM_LEFT_PHOTOGRAPH_X, this.topPos + FILM_PHOTOGRAPH_Y, ARROW_BOX_SIZE, ARROW_BOX_SIZE, mouseX, mouseY)) {
			this.incrementPhotographIndex(true);
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private void incrementPhotographIndex(boolean left) {
		if (this.photographIndex == 0) {
			this.photographIndex = this.filmContents.size() - 1;
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

	private static boolean checkButtonClicked(int minX, int minY, int buttonWidth, int buttonHeight, int mouseX, int mouseY) {
		return mouseX >= minX && mouseX <= minX + buttonWidth && mouseY >= minY && mouseY <= minY + buttonHeight;
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
