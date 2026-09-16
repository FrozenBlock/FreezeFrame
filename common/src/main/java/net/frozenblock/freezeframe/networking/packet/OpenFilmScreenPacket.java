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
import net.frozenblock.lib.networking.api.NetworkingHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public record OpenFilmScreenPacket(InteractionHand hand) implements CustomPacketPayload {
	public static final Type<OpenFilmScreenPacket> TYPE = new Type<>(FFConstants.id("open_film_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenFilmScreenPacket> CODEC = StreamCodec.composite(
		InteractionHand.STREAM_CODEC, OpenFilmScreenPacket::hand,
		OpenFilmScreenPacket::new
	);

	public static void sendTo(ServerPlayer player, InteractionHand hand) {
		NetworkingHelper.sendToPlayer(player, new OpenFilmScreenPacket(hand));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
