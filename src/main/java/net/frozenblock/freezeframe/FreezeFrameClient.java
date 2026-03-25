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

package net.frozenblock.freezeframe;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.frozenblock.freezeframe.client.ScopeAndCameraUseController;
import net.frozenblock.freezeframe.client.model.object.camera.TripodCameraModel;
import net.frozenblock.freezeframe.client.model.object.camera.DiscCameraModel;
import net.frozenblock.freezeframe.client.renderer.entity.DiscCameraRenderer;
import net.frozenblock.freezeframe.client.renderer.entity.TripodCameraRenderer;
import net.frozenblock.freezeframe.component.tooltip.CameraTooltip;
import net.frozenblock.freezeframe.component.tooltip.FilmTooltip;
import net.frozenblock.freezeframe.component.tooltip.PhotographTooltip;
import net.frozenblock.freezeframe.component.tooltip.client.ClientCameraTooltip;
import net.frozenblock.freezeframe.component.tooltip.client.ClientFilmTooltip;
import net.frozenblock.freezeframe.component.tooltip.client.ClientPhotographTooltip;
import net.frozenblock.freezeframe.networking.FFClientNetworking;
import net.frozenblock.freezeframe.registry.FFEntityTypes;
import net.frozenblock.freezeframe.registry.FFScreens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;

@Environment(EnvType.CLIENT)
public class FreezeFrameClient implements ClientModInitializer {
	public static final ModelLayerLocation CAMERA_MODEL_LAYER = new ModelLayerLocation(FFConstants.id("camera"), "main");
	public static final ModelLayerLocation DISC_CAMERA_MODEL_LAYER = new ModelLayerLocation(FFConstants.id("disc_camera"), "main");
	public static final KeyMapping RESET_SCOPE_ZOOM = new KeyMapping("key.resetScopeZoom", InputConstants.Type.MOUSE, 2, KeyMapping.Category.GAMEPLAY, 1);

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(FFEntityTypes.CAMERA, TripodCameraRenderer::new);
		ModelLayerRegistry.registerModelLayer(CAMERA_MODEL_LAYER, TripodCameraModel::createBodyLayer);
		EntityRenderers.register(FFEntityTypes.DISC_CAMERA, DiscCameraRenderer::new);
		ModelLayerRegistry.registerModelLayer(DISC_CAMERA_MODEL_LAYER, DiscCameraModel::createBodyLayer);

		KeyMappingHelper.registerKeyMapping(RESET_SCOPE_ZOOM);

		FFScreens.init();
		ScopeAndCameraUseController.init();

		FFClientNetworking.init();

		ClientTooltipComponentCallback.EVENT.register(component -> {
			if (component instanceof PhotographTooltip tooltip) return new ClientPhotographTooltip(tooltip);
			if (component instanceof FilmTooltip tooltip) return new ClientFilmTooltip(tooltip.contents(), tooltip.maxPhotographs());
			if (component instanceof CameraTooltip tooltip) return new ClientCameraTooltip(tooltip.contents());
			return null;
		});
	}
}
