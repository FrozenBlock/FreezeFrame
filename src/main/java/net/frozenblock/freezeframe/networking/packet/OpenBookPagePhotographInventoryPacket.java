/*
 * Copyright 2026 FrozenBlock
 * This file is part of Freeze Frame.
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

package net.frozenblock.freezeframe.networking.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.menu.BookPagePhotographMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;

public record OpenBookPagePhotographInventoryPacket(InteractionHand hand, int pageIndex) implements CustomPacketPayload {
	public static final Type<OpenBookPagePhotographInventoryPacket> PACKET_TYPE = CustomPacketPayload.createType(FFConstants.safeString("open_book_page_photograph_inventory"));
	public static final StreamCodec<FriendlyByteBuf, OpenBookPagePhotographInventoryPacket> CODEC = StreamCodec.ofMember(
		OpenBookPagePhotographInventoryPacket::write,
		OpenBookPagePhotographInventoryPacket::new
	);

	public OpenBookPagePhotographInventoryPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), buf.readVarInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		buf.writeVarInt(this.pageIndex);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(OpenBookPagePhotographInventoryPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null || packet.pageIndex < 0 || packet.pageIndex >= WritableBookContent.MAX_PAGES) return;

		final ItemStack heldBook = player.getItemInHand(packet.hand);
		if (!heldBook.is(Items.WRITABLE_BOOK)) return;

		final WritableBookContent bookContent = heldBook.get(DataComponents.WRITABLE_BOOK_CONTENT);
		if (bookContent == null) return;

		player.openMenu(
			new SimpleMenuProvider(
				(id, inventory, menuPlayer) -> new BookPagePhotographMenu(id, inventory, packet.hand, packet.pageIndex),
				Component.translatable("screen.freezeframe.book_photograph.inventory")
			)
		);
	}
}
