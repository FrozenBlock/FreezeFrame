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

package net.lunade.camera.networking.packet;

import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.util.BookPagePhotographHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;

public record SetBookPagePhotographPacket(InteractionHand hand, int pageIndex, int sourceInventorySlot, boolean removePhoto) implements CustomPacketPayload {
	public static final Type<SetBookPagePhotographPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("set_book_page_photograph"));
	public static final StreamCodec<FriendlyByteBuf, SetBookPagePhotographPacket> CODEC = StreamCodec.ofMember(
		SetBookPagePhotographPacket::write,
		SetBookPagePhotographPacket::new
	);

	public SetBookPagePhotographPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		buf.writeVarInt(this.pageIndex);
		buf.writeVarInt(this.sourceInventorySlot);
		buf.writeBoolean(this.removePhoto);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(SetBookPagePhotographPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null || packet.pageIndex < 0 || packet.pageIndex >= WritableBookContent.MAX_PAGES) return;

		final ItemStack heldBook = player.getItemInHand(packet.hand);
		if (!heldBook.is(Items.WRITABLE_BOOK)) return;

		final WritableBookContent bookContent = heldBook.get(DataComponents.WRITABLE_BOOK_CONTENT);
		if (bookContent == null) return;

		final List<String> pageTexts = bookContent.getPages(false).toList();
		if (packet.pageIndex >= pageTexts.size()) return;

		final ItemStack currentPhoto = BookPagePhotographHelper.getPhoto(heldBook, packet.pageIndex).copy();
		if (packet.removePhoto) {
			if (currentPhoto.isEmpty()) return;
			BookPagePhotographHelper.clearPhoto(heldBook, packet.pageIndex);
			player.getInventory().placeItemBackInInventory(currentPhoto);
			return;
		}

		final int inventorySlot = packet.sourceInventorySlot;
		if (inventorySlot < 0 || inventorySlot >= Inventory.INVENTORY_SIZE) return;

		final ItemStack inventoryStack = player.getInventory().getItem(inventorySlot);
		if (!inventoryStack.is(CameraPortItems.PHOTOGRAPH)) return;

		final ItemStack usedPhoto = inventoryStack.copyWithCount(1);
		inventoryStack.shrink(1);
		if (inventoryStack.isEmpty()) player.getInventory().setItem(inventorySlot, ItemStack.EMPTY);

		BookPagePhotographHelper.setPhoto(heldBook, packet.pageIndex, usedPhoto);
		if (!currentPhoto.isEmpty()) {
			player.getInventory().placeItemBackInInventory(currentPhoto);
		}
	}
}
