package net.lunade.camera.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.lib.file.transfer.FileTransferFilter;
import net.lunade.camera.networking.packet.CameraPossessPacket;
import net.lunade.camera.networking.packet.PrinterAskForSlotsPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CameraPortNetworking {

	public static void init() {
		PayloadTypeRegistry<RegistryFriendlyByteBuf> registry = PayloadTypeRegistry.clientboundPlay();
		PayloadTypeRegistry<RegistryFriendlyByteBuf> c2sRegistry = PayloadTypeRegistry.serverboundPlay();

		registry.register(CameraPossessPacket.PACKET_TYPE, CameraPossessPacket.CODEC);
		c2sRegistry.register(PrinterAskForSlotsPacket.PACKET_TYPE, PrinterAskForSlotsPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PrinterAskForSlotsPacket.PACKET_TYPE, PrinterAskForSlotsPacket::handle);

		FileTransferFilter.whitelistDestinationPath("photographs", false);
		FileTransferFilter.whitelistDestinationPath("photographs", true);

		FileTransferFilter.whitelistRequestPath("photographs", false);
		FileTransferFilter.whitelistRequestPath("photographs", true);
	}
}
