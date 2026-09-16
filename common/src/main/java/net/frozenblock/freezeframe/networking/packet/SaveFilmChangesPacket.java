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
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFSounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record SaveFilmChangesPacket(InteractionHand hand, FilmContents contents, boolean hasModifiedPhotographs) implements CustomPacketPayload {
	public static final Type<SaveFilmChangesPacket> TYPE = new Type<>(FFConstants.id("save_film_changes"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SaveFilmChangesPacket> CODEC = StreamCodec.composite(
		InteractionHand.STREAM_CODEC, SaveFilmChangesPacket::hand,
		FilmContents.STREAM_CODEC, SaveFilmChangesPacket::contents,
		ByteBufCodecs.BOOL, SaveFilmChangesPacket::hasModifiedPhotographs,
		SaveFilmChangesPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(SaveFilmChangesPacket packet, MinecraftServer server, ServerPlayer player) {
		if (player == null) return;

		final InteractionHand hand = packet.hand;
		final ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(FFItems.FILM.get())) return;

		final FilmContents existingContents = stack.getOrDefault(FFDataComponents.FILM_CONTENTS.get(), FilmContents.EMPTY);
		final int maxPhotographs = FilmItem.getMaxPhotographs(stack);
		if (packet.contents.size() > maxPhotographs) return;

		final int removedPhotographs = Math.max(0, existingContents.size() - packet.contents.size());
		final int reducedMaxPhotographs = Math.max(packet.contents.size(), maxPhotographs - removedPhotographs);
		if (reducedMaxPhotographs <= 0) {
			player.setItemInHand(packet.hand, ItemStack.EMPTY);
			player.onEquippedItemBroken(stack.getItem(), hand.asEquipmentSlot());
			existingContents.photographs().forEach(photograph -> {
				PhotographTracker.incrementPhotographCountAndDeleteIfEmpty(player.level(), photograph.identifier().getPath(), -1);
			});
			return;
		} else {
			final Level level = player.level();
			if (packet.hasModifiedPhotographs) {
				level.playSound(
					null,
					player.getX(),
					player.getY(),
					player.getZ(),
					FFSounds.FILM_RENAME.get(),
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
					FFSounds.FILM_TEAR_FINISH.get(),
					player.getSoundSource(),
					1F,
					0.9F + (player.getRandom().nextFloat() * 0.35F)
				);
			}
		}

		stack.set(FFDataComponents.FILM_CONTENTS.get(), packet.contents);
		stack.set(FFDataComponents.FILM_MAX_PHOTOGRAPHS.get(), FilmItem.normalizeMaxPhotographs(reducedMaxPhotographs));
		FilmItem.refreshStackingState(stack);
		existingContents.photographs().stream()
			.filter(photograph -> !packet.contents.photographs().contains(photograph))
			.map(photograph -> photograph.identifier().getPath())
			.toList()
			.forEach(photographName -> PhotographTracker.incrementPhotographCountAndDeleteIfEmpty(player.level(), photographName, -1));
	}
}
