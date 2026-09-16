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
import net.frozenblock.lib.networking.api.NetworkingHelper;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

public final class FFNetworking {

	public static void setup() {
		NetworkingHelper.registerS2CPayloadType(CameraTakeScreenshotPacket.TYPE, CameraTakeScreenshotPacket.CODEC);
		NetworkingHelper.registerS2CPayloadType(OpenFilmScreenPacket.TYPE, OpenFilmScreenPacket.CODEC);
		NetworkingHelper.registerS2CLargePayloadType(DeletePhotographPacket.TYPE, DeletePhotographPacket.CODEC, ClientboundCustomPayloadPacket.MAX_PAYLOAD_SIZE);

		NetworkingHelper.registerC2SPayloadType(DevelopingTableSyncSelectPhotographIndexPacket.TYPE, DevelopingTableSyncSelectPhotographIndexPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(DevelopingTableSyncSelectPhotographIndexPacket.TYPE, DevelopingTableSyncSelectPhotographIndexPacket::handle);

		NetworkingHelper.registerC2SPayloadType(SelectFilmPhotographPacket.TYPE, SelectFilmPhotographPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(SelectFilmPhotographPacket.TYPE, SelectFilmPhotographPacket::handle);

		NetworkingHelper.registerC2SPayloadType(SelectCameraFilmPacket.TYPE, SelectCameraFilmPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(SelectCameraFilmPacket.TYPE, SelectCameraFilmPacket::handle);

		NetworkingHelper.registerC2SPayloadType(QuickCameraPhotographPacket.TYPE, QuickCameraPhotographPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(QuickCameraPhotographPacket.TYPE, QuickCameraPhotographPacket::handle);

		NetworkingHelper.registerC2SPayloadType(SaveFilmChangesPacket.TYPE, SaveFilmChangesPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(SaveFilmChangesPacket.TYPE, SaveFilmChangesPacket::handle);

		NetworkingHelper.registerC2SPayloadType(ChangeScopeZoomPacket.TYPE, ChangeScopeZoomPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(ChangeScopeZoomPacket.TYPE, ChangeScopeZoomPacket::handle);

		NetworkingHelper.registerC2SPayloadType(SetBookPagePhotographPacket.TYPE, SetBookPagePhotographPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(SetBookPagePhotographPacket.TYPE, SetBookPagePhotographPacket::handle);

		NetworkingHelper.registerC2SPayloadType(OpenBookPagePhotographInventoryPacket.TYPE, OpenBookPagePhotographInventoryPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(OpenBookPagePhotographInventoryPacket.TYPE, OpenBookPagePhotographInventoryPacket::handle);

		NetworkingHelper.registerC2SPayloadType(ChangeItemStackSizePacket.TYPE, ChangeItemStackSizePacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(ChangeItemStackSizePacket.TYPE, ChangeItemStackSizePacket::handle);

		NetworkingHelper.registerC2SPayloadType(SetCreativeModeCarriedItemPacket.TYPE, SetCreativeModeCarriedItemPacket.CODEC);
		NetworkingHelper.registerGlobalServerReceiver(SetCreativeModeCarriedItemPacket.TYPE, SetCreativeModeCarriedItemPacket::handle);

		FileTransferFilter.whitelistDestinationPath("photographs", false);
		FileTransferFilter.whitelistDestinationPath("photographs", true);

		FileTransferFilter.whitelistRequestPath("photographs", false);
		FileTransferFilter.whitelistRequestPath("photographs", true);
	}

	private FFNetworking() {}
}
