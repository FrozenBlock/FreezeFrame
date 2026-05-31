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

import java.util.OptionalInt;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.entity.TripodCamera;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record CameraTakeScreenshotPacket(OptionalInt entityId, boolean handheldCapture, float zoom, String fileName, FilmFilter filter) implements CustomPacketPayload {
	public static final Type<CameraTakeScreenshotPacket> PACKET_TYPE = CustomPacketPayload.createType(FFConstants.safeString("camera_take_screenshot"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CameraTakeScreenshotPacket> CODEC = StreamCodec.ofMember(CameraTakeScreenshotPacket::write, CameraTakeScreenshotPacket::new);

	public CameraTakeScreenshotPacket(RegistryFriendlyByteBuf buf) {
		this(ByteBufCodecs.OPTIONAL_VAR_INT.decode(buf), buf.readBoolean(), buf.readFloat(), buf.readUtf(), FilmFilter.STREAM_CODEC.decode(buf));
	}

	public static void sendToAsCamera(ServerPlayer player, int entityId, String fileName, FilmFilter filter) {
		ServerPlayNetworking.send(
			player,
			new CameraTakeScreenshotPacket(OptionalInt.of(entityId), false, 0.04F, fileName, filter)
		);
	}

	public static void sendToAsHandheld(ServerPlayer player, String fileName, float zoom, FilmFilter filter) {
		ServerPlayNetworking.send(
			player,
			new CameraTakeScreenshotPacket(OptionalInt.empty(), true, zoom, fileName, filter)
		);
	}

	public static void sendTo(ServerPlayer player, TripodCamera tripodCamera, String fileName, FilmFilter filter) {
		sendToAsCamera(player, tripodCamera.getId(), fileName, filter);
	}

	public void write(RegistryFriendlyByteBuf buf) {
		ByteBufCodecs.OPTIONAL_VAR_INT.encode(buf, this.entityId);
		buf.writeBoolean(this.handheldCapture);
		buf.writeFloat(this.zoom);
		buf.writeUtf(this.fileName);
		FilmFilter.STREAM_CODEC.encode(buf, this.filter);
	}

	@Override
	public Type<?> type() {
		return PACKET_TYPE;
	}
}
