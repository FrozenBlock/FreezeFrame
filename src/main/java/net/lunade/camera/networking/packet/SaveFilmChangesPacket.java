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
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.item.FilmItem;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record SaveFilmChangesPacket(InteractionHand hand, FilmContents contents) implements CustomPacketPayload {
	public static final Type<SaveFilmChangesPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("save_film_changes"));
	public static final StreamCodec<FriendlyByteBuf, SaveFilmChangesPacket> CODEC = StreamCodec.ofMember(SaveFilmChangesPacket::write, SaveFilmChangesPacket::new);

	public SaveFilmChangesPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), FilmContents.STREAM_CODEC.decode(buf));
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		FilmContents.STREAM_CODEC.encode(buf, this.contents);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(SaveFilmChangesPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;

		final ItemStack stack = player.getItemInHand(packet.hand);
		if (!stack.is(CameraPortItems.FILM)) return;

		final int maxPhotographs = FilmItem.getMaxPhotographs(stack);
		if (packet.contents.size() > maxPhotographs) return;

		stack.set(CameraPortDataComponents.FILM_CONTENTS, packet.contents);
		FilmItem.refreshStackingState(stack);
	}
}
