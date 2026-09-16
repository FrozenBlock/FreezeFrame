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

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.menu.DevelopingTableMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record DevelopingTableSyncSelectPhotographIndexPacket(int selectedPhotographIndex) implements CustomPacketPayload {
	public static final Type<DevelopingTableSyncSelectPhotographIndexPacket> TYPE = new Type<>(FFConstants.id("developing_table_sync_selected_photograph_index"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DevelopingTableSyncSelectPhotographIndexPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, DevelopingTableSyncSelectPhotographIndexPacket::selectedPhotographIndex,
		DevelopingTableSyncSelectPhotographIndexPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(DevelopingTableSyncSelectPhotographIndexPacket packet, MinecraftServer server, ServerPlayer player) {
		if (player == null) return;
		if (player.containerMenu instanceof DevelopingTableMenu developingTable) developingTable.setupDataAndResultSlot(packet.selectedPhotographIndex);
	}
}
