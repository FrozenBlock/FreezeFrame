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

package net.frozenblock.freezeframe.client;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.model.object.camera.DiscCameraModel;
import net.frozenblock.freezeframe.client.model.object.camera.TripodCameraModel;
import net.frozenblock.freezeframe.client.renderer.entity.DiscCameraRenderer;
import net.frozenblock.freezeframe.client.renderer.entity.TripodCameraRenderer;
import net.frozenblock.freezeframe.registry.FFEntityTypes;
import net.frozenblock.lib.renderer.entity.EntityRendererRegistry;
import net.frozenblock.lib.renderer.model.ModelLayerRegistry;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.model.geom.ModelLayerLocation;

@ClientOnly
public final class FFModelLayers {
	public static final ModelLayerLocation CAMERA = new ModelLayerLocation(FFConstants.id("camera"), "main");
	public static final ModelLayerLocation DISC_CAMERA = new ModelLayerLocation(FFConstants.id("disc_camera"), "main");

	public static void init() {
		ModelLayerRegistry.register(CAMERA, TripodCameraModel::createBodyLayer);
		EntityRendererRegistry.register(FFEntityTypes.CAMERA, TripodCameraRenderer::new);

		ModelLayerRegistry.register(DISC_CAMERA, DiscCameraModel::createBodyLayer);
		EntityRendererRegistry.register(FFEntityTypes.DISC_CAMERA, DiscCameraRenderer::new);
	}

	private FFModelLayers() {}
}
