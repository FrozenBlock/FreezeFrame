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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record SelectCameraFilmPacket(int slotId, int selectedFilmIndex) implements CustomPacketPayload {
	public static final Type<SelectCameraFilmPacket> TYPE = new Type<>(FFConstants.id("select_camera_film"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectCameraFilmPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SelectCameraFilmPacket::slotId,
		ByteBufCodecs.VAR_INT, SelectCameraFilmPacket::selectedFilmIndex,
		SelectCameraFilmPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(SelectCameraFilmPacket packet, MinecraftServer server, ServerPlayer player) {
		if (player != null) player.containerMenu.freezeFrame$setSelectedCameraFilmIndex(packet.slotId, packet.selectedFilmIndex);
	}
}
