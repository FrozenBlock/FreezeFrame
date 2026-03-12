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
import net.lunade.camera.networking.packet.PrinterSyncSelectPhotographIndexPacket;
import net.lunade.camera.networking.packet.SelectCameraFilmPacket;
import net.lunade.camera.networking.packet.SelectFilmPhotographPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CameraPortNetworking {

	public static void init() {
		PayloadTypeRegistry<RegistryFriendlyByteBuf> registry = PayloadTypeRegistry.clientboundPlay();
		PayloadTypeRegistry<RegistryFriendlyByteBuf> c2sRegistry = PayloadTypeRegistry.serverboundPlay();

		registry.register(CameraTakeScreenshotPacket.PACKET_TYPE, CameraTakeScreenshotPacket.CODEC);

		c2sRegistry.register(PrinterSyncSelectPhotographIndexPacket.PACKET_TYPE, PrinterSyncSelectPhotographIndexPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PrinterSyncSelectPhotographIndexPacket.PACKET_TYPE, PrinterSyncSelectPhotographIndexPacket::handle);

		c2sRegistry.register(SelectFilmPhotographPacket.PACKET_TYPE, SelectFilmPhotographPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectFilmPhotographPacket.PACKET_TYPE, SelectFilmPhotographPacket::handle);

		c2sRegistry.register(SelectCameraFilmPacket.PACKET_TYPE, SelectCameraFilmPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectCameraFilmPacket.PACKET_TYPE, SelectCameraFilmPacket::handle);

		FileTransferFilter.whitelistDestinationPath("photographs", false);
		FileTransferFilter.whitelistDestinationPath("photographs", true);

		FileTransferFilter.whitelistRequestPath("photographs", false);
		FileTransferFilter.whitelistRequestPath("photographs", true);
	}
}
