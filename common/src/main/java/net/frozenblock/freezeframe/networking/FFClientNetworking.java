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

import net.frozenblock.freezeframe.client.gui.screens.inventory.FilmScreen;
import net.frozenblock.freezeframe.client.screenshot.PhotographScreenshotter;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.frozenblock.freezeframe.networking.packet.CameraTakeScreenshotPacket;
import net.frozenblock.freezeframe.networking.packet.DeletePhotographPacket;
import net.frozenblock.freezeframe.networking.packet.OpenFilmScreenPacket;
import net.frozenblock.lib.networking.api.ClientNetworkingHelper;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.world.entity.Entity;

@ClientOnly
public final class FFClientNetworking {

	public static void setup() {
		ClientNetworkingHelper.registerGlobalClientReceiver(OpenFilmScreenPacket.TYPE, (packet, minecraft, player) -> {
			minecraft.execute(() -> {
				if (player == null) return;
				minecraft.gui.setScreen(new FilmScreen(player, packet.hand()));
			});
		});

		ClientNetworkingHelper.registerGlobalClientReceiver(CameraTakeScreenshotPacket.TYPE, (packet, minecraft, player) -> {
			final Entity entity = packet.entityId().isPresent() ? player.level().getEntity(packet.entityId().getAsInt()) : null;
			PhotographScreenshotter.executeScreenshot(entity, packet.handheldCapture(), packet.wasScoping(), packet.fileName(), packet.zoom(), packet.filter());
		});

		ClientNetworkingHelper.registerGlobalClientReceiver(DeletePhotographPacket.TYPE, (packet, minecraft, player) -> {
			PhotographTracker.deletePhotographs(minecraft.gameDirectory.toPath(), packet.photographNames());
		});
	}

	private FFClientNetworking() {}
}
