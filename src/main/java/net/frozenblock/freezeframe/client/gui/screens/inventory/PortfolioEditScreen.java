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

package net.frozenblock.freezeframe.client.gui.screens.inventory;

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
