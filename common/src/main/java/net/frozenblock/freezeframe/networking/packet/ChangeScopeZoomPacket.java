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
import net.frozenblock.freezeframe.component.ScopeZoomConfig;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record ChangeScopeZoomPacket(InteractionHand hand, float zoom) implements CustomPacketPayload {
	public static final Type<ChangeScopeZoomPacket> TYPE = new Type<>(FFConstants.id("change_scope_zoom"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ChangeScopeZoomPacket> CODEC = StreamCodec.composite(
		InteractionHand.STREAM_CODEC, ChangeScopeZoomPacket::hand,
		ByteBufCodecs.FLOAT, ChangeScopeZoomPacket::zoom,
		ChangeScopeZoomPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(ChangeScopeZoomPacket packet, MinecraftServer server, ServerPlayer player) {
		if (player == null) return;

		final InteractionHand hand = packet.hand;
		final ItemStack stack = player.getItemInHand(hand);
		final ScopeZoomConfig zoomConfig = stack.get(FFDataComponents.SCOPE_ZOOM_CONFIG.get());
		if (zoomConfig == null) return;

		final float clampedZoom = Math.clamp(packet.zoom, zoomConfig.minZoom(), zoomConfig.maxZoom());
		stack.set(FFDataComponents.SCOPE_ZOOM.get(), clampedZoom);
	}
}
