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
import net.lunade.camera.component.ScopeZoomConfig;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record ChangeScopeZoomPacket(InteractionHand hand, float zoom) implements CustomPacketPayload {
	public static final Type<ChangeScopeZoomPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("change_scope_zoom"));
	public static final StreamCodec<FriendlyByteBuf, ChangeScopeZoomPacket> CODEC = StreamCodec.ofMember(ChangeScopeZoomPacket::write, ChangeScopeZoomPacket::new);

	public ChangeScopeZoomPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), buf.readFloat());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		buf.writeFloat(this.zoom);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(ChangeScopeZoomPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;

		final InteractionHand hand = packet.hand;
		final ItemStack stack = player.getItemInHand(hand);
		final ScopeZoomConfig zoomConfig = stack.get(CameraPortDataComponents.SCOPE_ZOOM_CONFIG);
		if (zoomConfig == null) return;

		final float clampedZoom = Math.clamp(packet.zoom, zoomConfig.minZoom(), zoomConfig.maxZoom());
		stack.set(CameraPortDataComponents.SCOPE_ZOOM, clampedZoom);
	}
}
