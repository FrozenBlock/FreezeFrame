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
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.entity.TripodCamera;
import net.frozenblock.lib.networking.api.NetworkingHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record CameraTakeScreenshotPacket(
	OptionalInt entityId,
	boolean handheldCapture,
	boolean wasScoping,
	float zoom,
	String fileName,
	FilmFilter filter
) implements CustomPacketPayload {
	public static final Type<CameraTakeScreenshotPacket> TYPE = new Type<>(FFConstants.id("camera_take_screenshot"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CameraTakeScreenshotPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.OPTIONAL_VAR_INT, CameraTakeScreenshotPacket::entityId,
		ByteBufCodecs.BOOL, CameraTakeScreenshotPacket::handheldCapture,
		ByteBufCodecs.BOOL, CameraTakeScreenshotPacket::wasScoping,
		ByteBufCodecs.FLOAT, CameraTakeScreenshotPacket::zoom,
		ByteBufCodecs.STRING_UTF8, CameraTakeScreenshotPacket::fileName,
		FilmFilter.STREAM_CODEC, CameraTakeScreenshotPacket::filter,
		CameraTakeScreenshotPacket::new
	);

	public static void sendToAsCamera(ServerPlayer player, int entityId, String fileName, FilmFilter filter) {
		NetworkingHelper.sendToPlayer(
			player,
			new CameraTakeScreenshotPacket(OptionalInt.of(entityId), false, false, 0.04F, fileName, filter)
		);
	}

	public static void sendToAsHandheld(ServerPlayer player, boolean wasScoping, String fileName, float zoom, FilmFilter filter) {
		NetworkingHelper.sendToPlayer(
			player,
			new CameraTakeScreenshotPacket(OptionalInt.empty(), wasScoping, true, zoom, fileName, filter)
		);
	}

	public static void sendTo(ServerPlayer player, TripodCamera tripodCamera, String fileName, FilmFilter filter) {
		sendToAsCamera(player, tripodCamera.getId(), fileName, filter);
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
