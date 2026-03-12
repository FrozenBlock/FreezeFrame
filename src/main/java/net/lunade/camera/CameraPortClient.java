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

package net.lunade.camera;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.lunade.camera.client.CameraUseController;
import net.lunade.camera.client.model.object.camera.CameraModel;
import net.lunade.camera.client.model.object.camera.DiscCameraModel;
import net.lunade.camera.client.renderer.entity.TripodCameraRenderer;
import net.lunade.camera.client.renderer.entity.DiscCameraRenderer;
import net.lunade.camera.networking.CameraPortClientNetworking;
import net.lunade.camera.registry.CameraPortEntityTypes;
import net.lunade.camera.registry.CameraPortScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;

@Environment(EnvType.CLIENT)
public class CameraPortClient implements ClientModInitializer {
	public static final ModelLayerLocation CAMERA_MODEL_LAYER = new ModelLayerLocation(CameraPortConstants.id("camera"), "main");
	public static final ModelLayerLocation DISC_CAMERA_MODEL_LAYER = new ModelLayerLocation(CameraPortConstants.id("disc_camera"), "main");

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(CameraPortEntityTypes.CAMERA, TripodCameraRenderer::new);
		ModelLayerRegistry.registerModelLayer(CAMERA_MODEL_LAYER, CameraModel::createBodyLayer);
		EntityRenderers.register(CameraPortEntityTypes.DISC_CAMERA, DiscCameraRenderer::new);
		ModelLayerRegistry.registerModelLayer(DISC_CAMERA_MODEL_LAYER, DiscCameraModel::getTexturedModelData);

		CameraPortScreens.init();
		CameraUseController.init();

		CameraPortClientNetworking.init();
	}
}
