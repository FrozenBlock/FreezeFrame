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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public record OpenFilmScreenPacket(InteractionHand hand) implements CustomPacketPayload {
	public static final Type<OpenFilmScreenPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("open_film_screen"));
	public static final StreamCodec<FriendlyByteBuf, OpenFilmScreenPacket> CODEC = StreamCodec.ofMember(OpenFilmScreenPacket::write, OpenFilmScreenPacket::new);

	public OpenFilmScreenPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class));
	}

	public static void sendTo(ServerPlayer player, InteractionHand hand) {
		ServerPlayNetworking.send(player, new OpenFilmScreenPacket(hand));
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}
}
