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

package net.frozenblock.freezeframe.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.client.gui.screens.inventory.FilmScreen;
import net.frozenblock.freezeframe.networking.packet.CameraTakeScreenshotPacket;
import net.frozenblock.freezeframe.networking.packet.OpenFilmScreenPacket;
import net.frozenblock.freezeframe.util.client.CameraScreenshotManager;
import net.minecraft.world.entity.Entity;

public class FFClientNetworking {

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(OpenFilmScreenPacket.PACKET_TYPE, (packet, ctx) -> {
			ctx.client().execute(() -> {
				if (ctx.player() == null) return;
				ctx.client().setScreen(new FilmScreen(ctx.player(), packet.hand()));
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(CameraTakeScreenshotPacket.PACKET_TYPE, (packet, ctx) -> {
			final Entity entity = packet.entityId().isPresent() ? ctx.player().level().getEntity(packet.entityId().getAsInt()) : null;
			CameraScreenshotManager.executeScreenshot(entity, packet.handheldCapture(), packet.fileName(), packet.zoom());
		});
	}
}
