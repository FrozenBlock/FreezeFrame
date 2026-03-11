package net.lunade.camera.client.gui.screens.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PortfolioEditScreen extends Screen {

	// TODO: [Liuk, Treetrain1] please implement

	public PortfolioEditScreen(Player player, ItemStack portfolio, InteractionHand hand) {
		super(GameNarrator.NO_TITLE);
        /*
        this.owner = player;
        this.portfolio = portfolio;
        this.hand = hand;
        WritablePortfolioContent writablePortfolioContent = portfolio.get(CameraPortItems.WRITABLE_PORTFOLIO_CONTENT);
        if (writablePortfolioContent != null) {
            writablePortfolioContent.pages().forEach(this.pages::add);
        }

        if (this.pages.isEmpty()) {
            this.pages.add(ItemStack.EMPTY);
        }

        this.ownerText = Component.translatable("book.byAuthor", player.getName()).withStyle(ChatFormatting.DARK_GRAY);
         */
	}
}
