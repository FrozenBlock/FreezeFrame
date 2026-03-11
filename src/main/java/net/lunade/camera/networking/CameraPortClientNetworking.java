package net.lunade.camera.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.lunade.camera.networking.packet.CameraTakeScreenshotPacket;
import net.minecraft.world.entity.Entity;

public class CameraPortClientNetworking {

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(CameraTakeScreenshotPacket.PACKET_TYPE, (packet, ctx) -> {
			final Entity entity = packet.entityId().isPresent() ? ctx.player().level().getEntity(packet.entityId().getAsInt()) : null;
			CameraScreenshotManager.executeScreenshot(entity, false, packet.fileName());
		});
	}
}
