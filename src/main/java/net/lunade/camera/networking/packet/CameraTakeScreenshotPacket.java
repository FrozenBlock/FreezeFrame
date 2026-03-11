package net.lunade.camera.networking.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.entity.TripodCamera;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import java.util.OptionalInt;

public record CameraTakeScreenshotPacket(OptionalInt entityId, boolean isHandheld, String fileName) implements CustomPacketPayload {
	public static final Type<CameraTakeScreenshotPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("camera_take_screenshot"));
	public static final StreamCodec<FriendlyByteBuf, CameraTakeScreenshotPacket> CODEC = StreamCodec.ofMember(CameraTakeScreenshotPacket::write, CameraTakeScreenshotPacket::new);

	public CameraTakeScreenshotPacket(FriendlyByteBuf buf) {
		this(ByteBufCodecs.OPTIONAL_VAR_INT.decode(buf), buf.readBoolean(), buf.readUtf());
	}

	public static void sendToAsCamera(ServerPlayer player, int entityId, String fileName) {
		ServerPlayNetworking.send(
			player,
			new CameraTakeScreenshotPacket(OptionalInt.of(entityId), false, fileName)
		);
	}

	public static void sendToAsHandheld(ServerPlayer player, String fileName) {
		ServerPlayNetworking.send(
			player,
			new CameraTakeScreenshotPacket(OptionalInt.empty(), true, fileName)
		);
	}

	public static void sendTo(ServerPlayer player, TripodCamera tripodCamera, String fileName) {
		sendToAsCamera(player, tripodCamera.getId(), fileName);
	}

	public void write(FriendlyByteBuf buf) {
		ByteBufCodecs.OPTIONAL_VAR_INT.encode(buf, this.entityId);
		buf.writeBoolean(this.isHandheld);
		buf.writeUtf(this.fileName);
	}

	@Override
	public Type<?> type() {
		return PACKET_TYPE;
	}
}
