package net.lunade.camera.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.menu.PrinterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record PrinterAskForSlotsPacket(int count, String id) implements CustomPacketPayload {
	public static final Type<PrinterAskForSlotsPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("printer_ask_for_slots"));
	public static final StreamCodec<FriendlyByteBuf, PrinterAskForSlotsPacket> CODEC = StreamCodec.ofMember(PrinterAskForSlotsPacket::write, PrinterAskForSlotsPacket::new);

	public PrinterAskForSlotsPacket(FriendlyByteBuf buf) {
		this(buf.readInt(), buf.readUtf());
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeInt(count);
		buf.writeUtf(id);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_TYPE;
	}

	public static void handle(PrinterAskForSlotsPacket packet, ServerPlayNetworking.Context context) {
		final ServerPlayer player = context.player();
		if (player == null) return;
		if (player.containerMenu instanceof PrinterMenu printer) printer.setupData(player, packet.count, packet.id);
	}
}
