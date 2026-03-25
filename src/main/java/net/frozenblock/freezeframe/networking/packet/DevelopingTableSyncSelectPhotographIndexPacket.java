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

package net.frozenblock.freezeframe.networking.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.menu.DevelopingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record DevelopingTableSyncSelectPhotographIndexPacket(int selectedPhotographIndex) implements CustomPacketPayload {
	public static final Type<DevelopingTableSyncSelectPhotographIndexPacket> PACKET_TYPE = CustomPacketPayload.createType(FFConstants.safeString("developing_table_sync_selected_photograph_index"));
	public static final StreamCodec<FriendlyByteBuf, DevelopingTableSyncSelectPhotographIndexPacket> CODEC = StreamCodec.ofMember(DevelopingTableSyncSelectPhotographIndexPacket::write, DevelopingTableSyncSelectPhotographIndexPacket::new);

	public DevelopingTableSyncSelectPhotographIndexPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.selectedPhotographIndex);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(DevelopingTableSyncSelectPhotographIndexPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;
		if (player.containerMenu instanceof DevelopingTableMenu developingTable) developingTable.setupDataAndResultSlot(packet.selectedPhotographIndex);
	}
}
