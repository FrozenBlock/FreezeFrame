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

package net.frozenblock.freezeframe.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.FreezeFrameClient;
import net.frozenblock.freezeframe.client.model.object.camera.TripodCameraModel;
import net.frozenblock.freezeframe.client.renderer.entity.state.TripodCameraRenderState;
import net.frozenblock.freezeframe.entity.TripodCamera;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class TripodCameraRenderer extends AbstractCameraRenderer<TripodCamera, TripodCameraModel> {
	private static final Identifier TEXTURE = FFConstants.id("textures/entity/camera.png");

	public TripodCameraRenderer(EntityRendererProvider.Context context) {
		super(context, new TripodCameraModel(context.bakeLayer(FreezeFrameClient.CAMERA_MODEL_LAYER)), 0.5F);
	}

	@Override
	public Identifier getTextureLocation(TripodCameraRenderState renderState) {
		return TEXTURE;
	}
}
