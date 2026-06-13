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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.List;

public record DeletePhotographPacket(List<String> photographNames) implements CustomPacketPayload {
	public static final Type<DeletePhotographPacket> PACKET_TYPE = CustomPacketPayload.createType(FFConstants.safeString("delete_photograph"));
	public static final StreamCodec<FriendlyByteBuf, DeletePhotographPacket> CODEC = StreamCodec.ofMember(DeletePhotographPacket::write, DeletePhotographPacket::new);

	public DeletePhotographPacket(FriendlyByteBuf buf) {
		this(buf.readList(FriendlyByteBuf::readUtf));
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeCollection(this.photographNames, FriendlyByteBuf::writeUtf);
	}

	@Override
	public Type<?> type() {
		return PACKET_TYPE;
	}
}
