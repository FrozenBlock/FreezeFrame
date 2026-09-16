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

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.FFModelLayers;
import net.frozenblock.freezeframe.client.model.object.camera.DiscCameraModel;
import net.frozenblock.freezeframe.client.renderer.entity.state.TripodCameraRenderState;
import net.frozenblock.freezeframe.entity.DiscCamera;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

@ClientOnly
public class DiscCameraRenderer extends AbstractCameraRenderer<DiscCamera, DiscCameraModel> {
	private static final Identifier TEXTURE = FFConstants.id("textures/entity/camera.png");

	public DiscCameraRenderer(EntityRendererProvider.Context context) {
		super(context, new DiscCameraModel(context.bakeLayer(FFModelLayers.DISC_CAMERA)), 0.5F);
	}

	@Override
	public Identifier getTextureLocation(TripodCameraRenderState renderState) {
		return TEXTURE;
	}
}
