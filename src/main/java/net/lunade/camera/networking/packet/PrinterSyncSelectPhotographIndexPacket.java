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

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.menu.PrinterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record PrinterSyncSelectPhotographIndexPacket(int selectedPhotographIndex) implements CustomPacketPayload {
	public static final Type<PrinterSyncSelectPhotographIndexPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("printer_sync_selected_photograph_index"));
	public static final StreamCodec<FriendlyByteBuf, PrinterSyncSelectPhotographIndexPacket> CODEC = StreamCodec.ofMember(PrinterSyncSelectPhotographIndexPacket::write, PrinterSyncSelectPhotographIndexPacket::new);

	public PrinterSyncSelectPhotographIndexPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.selectedPhotographIndex);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(PrinterSyncSelectPhotographIndexPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;
		if (player.containerMenu instanceof PrinterMenu printer) printer.setupDataAndResultSlot(packet.selectedPhotographIndex);
	}
}
