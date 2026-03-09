package net.lunade.camera.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.entity.CameraEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record CameraPossessPacket(int entityId) implements CustomPacketPayload {
	public static final Type<CameraPossessPacket> PACKET_TYPE = CustomPacketPayload.createType(CameraPortConstants.safeString("camera_possess"));
	public static final StreamCodec<FriendlyByteBuf, CameraPossessPacket> CODEC = ByteBufCodecs.VAR_INT
		.map(CameraPossessPacket::new, CameraPossessPacket::entityId)
		.cast();

	public CameraPossessPacket(FriendlyByteBuf buf) {
		this(buf.readVarInt());
	}

	public static void sendTo(ServerPlayer serverPlayer, int entityId) {
		ServerPlayNetworking.send(serverPlayer, new CameraPossessPacket(entityId));
	}

	public static void sendTo(ServerPlayer serverPlayer, CameraEntity cameraEntity) {
		CameraPossessPacket cameraPossessPacket = new CameraPossessPacket(cameraEntity.getId());
		ServerPlayNetworking.send(serverPlayer, cameraPossessPacket);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.entityId());
	}

	@Override
	public Type<?> type() {
		return PACKET_TYPE;
	}
}
