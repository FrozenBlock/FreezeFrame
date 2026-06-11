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
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record DeleteItemStackPacket(ItemStack stack) implements CustomPacketPayload {
	public static final Type<DeleteItemStackPacket> PACKET_TYPE = CustomPacketPayload.createType(FFConstants.safeString("delete_item_stack"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DeleteItemStackPacket> CODEC = StreamCodec.ofMember(DeleteItemStackPacket::write, DeleteItemStackPacket::new);

	public DeleteItemStackPacket(RegistryFriendlyByteBuf buf) {
		this(ItemStack.STREAM_CODEC.decode(buf));
	}

	public void write(RegistryFriendlyByteBuf buf) {
		ItemStack.STREAM_CODEC.encode(buf, this.stack);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(DeleteItemStackPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;
		PhotographTracker.incrementOnItemStackDeletion(player.level(), packet.stack);
	}
}
