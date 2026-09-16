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

public record SelectFilmPhotographPacket(int slotId, int selectedPhotographIndex) implements CustomPacketPayload {
	public static final Type<SelectFilmPhotographPacket> TYPE = new Type<>(FFConstants.id("select_film_photograph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SelectFilmPhotographPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SelectFilmPhotographPacket::slotId,
		ByteBufCodecs.VAR_INT, SelectFilmPhotographPacket::selectedPhotographIndex,
		SelectFilmPhotographPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(SelectFilmPhotographPacket packet, MinecraftServer server, ServerPlayer player) {
		if (player != null) player.containerMenu.freezeFrame$setSelectedFilmPhotographIndex(packet.slotId, packet.selectedPhotographIndex);
	}
}
