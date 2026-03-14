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

package net.lunade.camera.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.lib.file.transfer.FileTransferFilter;
import net.lunade.camera.networking.packet.CameraTakeScreenshotPacket;
import net.lunade.camera.networking.packet.DevelopingTableSyncSelectPhotographIndexPacket;
import net.lunade.camera.networking.packet.OpenFilmScreenPacket;
import net.lunade.camera.networking.packet.QuickCameraPhotographPacket;
import net.lunade.camera.networking.packet.SaveFilmChangesPacket;
import net.lunade.camera.networking.packet.SelectCameraFilmPacket;
import net.lunade.camera.networking.packet.SelectFilmPhotographPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CameraPortNetworking {

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

		FileTransferFilter.whitelistDestinationPath("photographs", false);
		FileTransferFilter.whitelistDestinationPath("photographs", true);

		FileTransferFilter.whitelistRequestPath("photographs", false);
		FileTransferFilter.whitelistRequestPath("photographs", true);
	}
}
