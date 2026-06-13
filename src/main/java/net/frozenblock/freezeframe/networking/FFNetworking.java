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
import net.frozenblock.freezeframe.networking.packet.CameraTakeScreenshotPacket;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.frozenblock.freezeframe.networking.packet.ChangeScopeZoomPacket;
import net.frozenblock.freezeframe.networking.packet.DeletePhotographPacket;
import net.frozenblock.freezeframe.networking.packet.DevelopingTableSyncSelectPhotographIndexPacket;
import net.frozenblock.freezeframe.networking.packet.OpenBookPagePhotographInventoryPacket;
import net.frozenblock.freezeframe.networking.packet.OpenFilmScreenPacket;
import net.frozenblock.freezeframe.networking.packet.QuickCameraPhotographPacket;
import net.frozenblock.freezeframe.networking.packet.SaveFilmChangesPacket;
import net.frozenblock.freezeframe.networking.packet.SelectCameraFilmPacket;
import net.frozenblock.freezeframe.networking.packet.SelectFilmPhotographPacket;
import net.frozenblock.freezeframe.networking.packet.SetBookPagePhotographPacket;
import net.frozenblock.freezeframe.networking.packet.SetCreativeModeCarriedItemPacket;
import net.frozenblock.lib.file.transfer.FileTransferFilter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

public class FFNetworking {

	public static void init() {
		final PayloadTypeRegistry<RegistryFriendlyByteBuf> registry = PayloadTypeRegistry.clientboundPlay();
		final PayloadTypeRegistry<RegistryFriendlyByteBuf> c2sRegistry = PayloadTypeRegistry.serverboundPlay();

		registry.register(CameraTakeScreenshotPacket.TYPE, CameraTakeScreenshotPacket.CODEC);
		registry.register(OpenFilmScreenPacket.TYPE, OpenFilmScreenPacket.CODEC);
		registry.registerLarge(DeletePhotographPacket.TYPE, DeletePhotographPacket.CODEC, ClientboundCustomPayloadPacket.MAX_PAYLOAD_SIZE);

		c2sRegistry.register(DevelopingTableSyncSelectPhotographIndexPacket.TYPE, DevelopingTableSyncSelectPhotographIndexPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DevelopingTableSyncSelectPhotographIndexPacket.TYPE, DevelopingTableSyncSelectPhotographIndexPacket::handle);

		c2sRegistry.register(SelectFilmPhotographPacket.TYPE, SelectFilmPhotographPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectFilmPhotographPacket.TYPE, SelectFilmPhotographPacket::handle);

		c2sRegistry.register(SelectCameraFilmPacket.TYPE, SelectCameraFilmPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SelectCameraFilmPacket.TYPE, SelectCameraFilmPacket::handle);

		c2sRegistry.register(QuickCameraPhotographPacket.TYPE, QuickCameraPhotographPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(QuickCameraPhotographPacket.TYPE, QuickCameraPhotographPacket::handle);

		c2sRegistry.register(SaveFilmChangesPacket.TYPE, SaveFilmChangesPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SaveFilmChangesPacket.TYPE, SaveFilmChangesPacket::handle);

		c2sRegistry.register(ChangeScopeZoomPacket.TYPE, ChangeScopeZoomPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ChangeScopeZoomPacket.TYPE, ChangeScopeZoomPacket::handle);

		c2sRegistry.register(SetBookPagePhotographPacket.TYPE, SetBookPagePhotographPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetBookPagePhotographPacket.TYPE, SetBookPagePhotographPacket::handle);

		c2sRegistry.register(OpenBookPagePhotographInventoryPacket.TYPE, OpenBookPagePhotographInventoryPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenBookPagePhotographInventoryPacket.TYPE, OpenBookPagePhotographInventoryPacket::handle);

		c2sRegistry.register(ChangeItemStackSizePacket.TYPE, ChangeItemStackSizePacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ChangeItemStackSizePacket.TYPE, ChangeItemStackSizePacket::handle);

		c2sRegistry.register(SetCreativeModeCarriedItemPacket.TYPE, SetCreativeModeCarriedItemPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetCreativeModeCarriedItemPacket.TYPE, SetCreativeModeCarriedItemPacket::handle);

		FileTransferFilter.whitelistDestinationPath("photographs", false);
		FileTransferFilter.whitelistDestinationPath("photographs", true);

		FileTransferFilter.whitelistRequestPath("photographs", false);
		FileTransferFilter.whitelistRequestPath("photographs", true);
	}
}
