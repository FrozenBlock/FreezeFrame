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

package net.frozenblock.freezeframe.networking.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record SaveFilmChangesPacket(InteractionHand hand, FilmContents contents, boolean hasModifiedPhotographs) implements CustomPacketPayload {
	public static final Type<SaveFilmChangesPacket> PACKET_TYPE = CustomPacketPayload.createType(FFConstants.safeString("save_film_changes"));
	public static final StreamCodec<FriendlyByteBuf, SaveFilmChangesPacket> CODEC = StreamCodec.ofMember(SaveFilmChangesPacket::write, SaveFilmChangesPacket::new);

	public SaveFilmChangesPacket(FriendlyByteBuf buf) {
		this(buf.readEnum(InteractionHand.class), FilmContents.STREAM_CODEC.decode(buf), buf.readBoolean());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(this.hand);
		FilmContents.STREAM_CODEC.encode(buf, this.contents);
		buf.writeBoolean(this.hasModifiedPhotographs);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(SaveFilmChangesPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;

		final InteractionHand hand = packet.hand;
		final ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(FFItems.FILM)) return;

		final FilmContents existingContents = stack.getOrDefault(FFDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		final int maxPhotographs = FilmItem.getMaxPhotographs(stack);
		if (packet.contents.size() > maxPhotographs) return;

		final int removedPhotographs = Math.max(0, existingContents.size() - packet.contents.size());
		final int reducedMaxPhotographs = Math.max(packet.contents.size(), maxPhotographs - removedPhotographs);
		if (reducedMaxPhotographs <= 0) {
			player.setItemInHand(packet.hand, ItemStack.EMPTY);
			player.onEquippedItemBroken(stack.getItem(), hand.asEquipmentSlot());
			return;
		} else {
			final Level level = player.level();
			if (packet.hasModifiedPhotographs) {
				level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					FFSounds.FILM_RENAME,
					player.getSoundSource(),
					1F,
					0.8F + (player.getRandom().nextFloat() * 0.4F)
				);
			}
			if (removedPhotographs > 0) {
				level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					FFSounds.FILM_TEAR_FINISH,
					player.getSoundSource(),
					1F,
					0.9F + (player.getRandom().nextFloat() * 0.35F)
				);
			}
		}

		stack.set(FFDataComponents.FILM_CONTENTS, packet.contents);
		stack.set(FFDataComponents.FILM_MAX_PHOTOGRAPHS, FilmItem.normalizeMaxPhotographs(reducedMaxPhotographs));
		FilmItem.refreshStackingState(stack);
	}
}
