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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.lib.networking.api.NetworkingHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record DeletePhotographPacket(List<String> photographNames) implements CustomPacketPayload {
	public static final Type<DeletePhotographPacket> TYPE = new Type<>(FFConstants.id("delete_photograph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DeletePhotographPacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DeletePhotographPacket::photographNames,
		DeletePhotographPacket::new
	);

	public static void sendDeletedPhotographsAndHandleTimestamp(ServerPlayer player, long gameTime, Map<String, Long> deletedPhotographs) {
		final Optional<Long> lastSyncTimestamp = FFAttachmentTypes.PHOTOGRAPH_TRACKER_LAST_SYNC_TIMESTAMP.getOptional(player);

		final List<String> relevantPhotographNames = new ArrayList<>();
		lastSyncTimestamp.ifPresent(timestamp -> {
			deletedPhotographs.forEach((name, deletionTime) -> {
				if (deletionTime >= timestamp) relevantPhotographNames.add(name);
			});
		});

		if (!relevantPhotographNames.isEmpty()) NetworkingHelper.sendToPlayer(player, new DeletePhotographPacket(relevantPhotographNames));

		FFAttachmentTypes.PHOTOGRAPH_TRACKER_LAST_SYNC_TIMESTAMP.set(player, gameTime);
	}

	@Override
	public Type<?> type() {
		return TYPE;
	}
}
