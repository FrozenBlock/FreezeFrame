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

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.lib.file.transfer.FileTransferFilter;
import net.frozenblock.freezeframe.networking.packet.CameraTakeScreenshotPacket;
import net.frozenblock.freezeframe.networking.packet.ChangeScopeZoomPacket;
import net.frozenblock.freezeframe.networking.packet.DevelopingTableSyncSelectPhotographIndexPacket;
import net.frozenblock.freezeframe.networking.packet.OpenFilmScreenPacket;
import net.frozenblock.freezeframe.networking.packet.QuickCameraPhotographPacket;
import net.frozenblock.freezeframe.networking.packet.SaveFilmChangesPacket;
import net.frozenblock.freezeframe.networking.packet.SelectCameraFilmPacket;
import net.frozenblock.freezeframe.networking.packet.SelectFilmPhotographPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class FFNetworking {

	public static void init() {
		PayloadTypeRegistry<RegistryFriendlyByteBuf> registry = PayloadTypeRegistry.clientboundPlay();
		PayloadTypeRegistry<RegistryFriendlyByteBuf> c2sRegistry = PayloadTypeRegistry.serverboundPlay();

		registry.register(CameraTakeScreenshotPacket.PACKET_TYPE, CameraTakeScreenshotPacket.CODEC);
		registry.register(OpenFilmScreenPacket.PACKET_TYPE, OpenFilmScreenPacket.CODEC);

		c2sRegistry.register(DevelopingTableSyncSelectPhotographIndexPacket.PACKET_TYPE, DevelopingTableSyncSelectPhotographIndexPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DevelopingTableSyncSelectPhotographIndexPacket.PACKET_TYPE, DevelopingTableSyncSelectPhotographIndexPacket::handle);

		c2sRegistry.register(SelectFilmPhotographPacket.PACKET_TYPE, SelectFilmPhotographPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectFilmPhotographPacket.PACKET_TYPE, SelectFilmPhotographPacket::handle);

		c2sRegistry.register(SelectCameraFilmPacket.PACKET_TYPE, SelectCameraFilmPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectCameraFilmPacket.PACKET_TYPE, SelectCameraFilmPacket::handle);

		c2sRegistry.register(QuickCameraPhotographPacket.PACKET_TYPE, QuickCameraPhotographPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(QuickCameraPhotographPacket.PACKET_TYPE, QuickCameraPhotographPacket::handle);

		c2sRegistry.register(SaveFilmChangesPacket.PACKET_TYPE, SaveFilmChangesPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SaveFilmChangesPacket.PACKET_TYPE, SaveFilmChangesPacket::handle);

		c2sRegistry.register(ChangeScopeZoomPacket.PACKET_TYPE, ChangeScopeZoomPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ChangeScopeZoomPacket.PACKET_TYPE, ChangeScopeZoomPacket::handle);

		FileTransferFilter.whitelistDestinationPath("photographs", false);
		FileTransferFilter.whitelistDestinationPath("photographs", true);

		FileTransferFilter.whitelistRequestPath("photographs", false);
		FileTransferFilter.whitelistRequestPath("photographs", true);
	}
}
