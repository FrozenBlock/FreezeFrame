package net.lunade.camera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.lunade.camera.client.model.CameraModel;
import net.lunade.camera.client.model.DiscCameraModel;
import net.lunade.camera.client.renderer.entity.CameraRenderer;
import net.lunade.camera.client.renderer.entity.DiscCameraRenderer;
import net.lunade.camera.networking.CameraClientNetworking;
import net.lunade.camera.registry.CameraPortEntityTypes;
import net.lunade.camera.registry.CameraPortScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;

@Environment(EnvType.CLIENT)
public class CameraPortClient implements ClientModInitializer {
	public static ModelLayerLocation CAMERA_MODEL_LAYER = new ModelLayerLocation(CameraPortConstants.id("camera"), "main");
	public static ModelLayerLocation DISC_CAMERA_MODEL_LAYER = new ModelLayerLocation(CameraPortConstants.id("disc_camera"), "main");

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(CameraPortEntityTypes.CAMERA, CameraRenderer::new);
		ModelLayerRegistry.registerModelLayer(CAMERA_MODEL_LAYER, CameraModel::createBodyLayer);
		EntityRenderers.register(CameraPortEntityTypes.DISC_CAMERA, DiscCameraRenderer::new);
		ModelLayerRegistry.registerModelLayer(DISC_CAMERA_MODEL_LAYER, DiscCameraModel::getTexturedModelData);

		CameraPortScreens.init();

		CameraClientNetworking.init();
	}
}
