package net.frozenblock.freezeframe.client.gui.components.inventory;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.photograph.PhotographRenderer;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@ClientOnly
public class FilmPhotographButton extends Button {
	protected static final Identifier FILM_PHOTOGRAPH_HIGHLIGHT_SPRITE = FFConstants.id("container/film/film_photograph_highlight");
	protected static final Identifier FILM_PHOTOGRAPH_BLOCKER_SPRITE = FFConstants.id("container/film/film_photograph_blocker");
	protected static final Identifier FILM_PHOTOGRAPH_SAVE_SPRITE = FFConstants.id("container/film/film_photograph_save");
	protected static final Identifier FILM_PHOTOGRAPH_DELETE_SPRITE = FFConstants.id("container/film/film_photograph_delete");

	private final @Nullable Identifier photographId;
	private final Supplier<Tooltip> tooltip;
	private final BooleanSupplier selected;
	private final BooleanSupplier toBeSaved;
	private final BooleanSupplier toBeDeleted;

	public FilmPhotographButton(
		int size,
		// TODO: what on earth is the message gonna be
		Component message,
		Button.OnPress onPress,
		Button.CreateNarration createNarration,
		@Nullable Identifier photographId,
		Supplier<Tooltip> tooltip,
		BooleanSupplier selected,
		BooleanSupplier toBeSaved,
		BooleanSupplier toBeDeleted
	) {
		super(0, 0, size, size, message, onPress, createNarration);
		this.photographId = photographId;
		this.tooltip = tooltip;
		this.selected = selected;
		this.toBeSaved = toBeSaved;
		this.toBeDeleted = toBeDeleted;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics) {
		final boolean blocker = this.photographId == null;
		final Identifier overlaySprite = blocker
			? FILM_PHOTOGRAPH_BLOCKER_SPRITE
			: this.toBeSaved.getAsBoolean() ? FILM_PHOTOGRAPH_SAVE_SPRITE
			: this.toBeDeleted.getAsBoolean() ? FILM_PHOTOGRAPH_DELETE_SPRITE
			: this.selected.getAsBoolean() ? FILM_PHOTOGRAPH_HIGHLIGHT_SPRITE
			: null;

		if (!blocker) {
			PhotographRenderer.blit(
				this.getX(),
				this.getY(),
				0,
				0,
				graphics,
				photographId,
				this.width,
				PhotographRenderer.FrameType.NONE
			);
		}

		if (overlaySprite != null) graphics.blitSprite(GuiPipeline.TEXTURED, overlaySprite, this.getX(), this.getY(), this.width, this.height);
	}

	@Override
	// TODO: test
	// TODO: update tooltips as player edits the name?
	protected void extractTooltipForNextRenderPass(GuiGraphicsExtractor graphics) {
		if (this.isHovered() || (this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard())) this.setTooltip(this.tooltip.get());

		super.extractTooltipForNextRenderPass(graphics);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		if (!this.selected.getAsBoolean()) super.playDownSound(soundManager);
	}

	@Override
	// TODO: test
	protected MutableComponent createNarrationMessage() {
		final MutableComponent narration = super.createNarrationMessage();
		return this.selected.getAsBoolean() ? Component.translatable("narrator.select", narration) : narration;
	}

	@Override
	// TODO: update tooltips as player edits the name?
	protected void updateTooltipNarration(NarrationElementOutput output) {
	}

}
