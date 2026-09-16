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
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record SetCreativeModeCarriedItemPacket(ItemStack stack) implements CustomPacketPayload {
	public static final Type<SetCreativeModeCarriedItemPacket> TYPE = new Type<>(FFConstants.id("set_creative_mode_carried_item"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetCreativeModeCarriedItemPacket> CODEC = StreamCodec.composite(
		ItemStack.OPTIONAL_STREAM_CODEC, SetCreativeModeCarriedItemPacket::stack,
		SetCreativeModeCarriedItemPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(SetCreativeModeCarriedItemPacket packet, MinecraftServer server, ServerPlayer player) {
		if (player == null) return;
		if (!packet.stack.isEmpty() && player.hasInfiniteMaterials()) {
			FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.set(player, packet.stack);
			if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("Creative carried stack set to: " + packet.stack.getItemName(), true);
		} else {
			FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.remove(player);
			if (FFConstants.UNSTABLE_LOGGING)  FFConstants.log("Creative carried stack emptied!", true);
		}
	}
}
