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

package net.frozenblock.freezeframe.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FreezeFrameClient;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.client.model.object.camera.DiscCameraModel;
import net.frozenblock.freezeframe.client.renderer.entity.state.TripodCameraRenderState;
import net.frozenblock.freezeframe.entity.DiscCamera;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class DiscCameraRenderer extends MobRenderer<DiscCamera, TripodCameraRenderState, DiscCameraModel> {
	private static final Identifier TEXTURE = FFConstants.id("textures/entity/camera.png");

	public DiscCameraRenderer(EntityRendererProvider.Context context) {
		super(context, new DiscCameraModel(context.bakeLayer(FreezeFrameClient.DISC_CAMERA_MODEL_LAYER)), 0.5F);
	}

	@Override
	public TripodCameraRenderState createRenderState() {
		return new TripodCameraRenderState();
	}

	@Override
	public void extractRenderState(DiscCamera entity, TripodCameraRenderState renderState, float partialTicks) {
		super.extractRenderState(entity, renderState, partialTicks);
		renderState.trackedHeight = entity.getTrackedHeight();
		renderState.lerpedTimer = entity.getLerpedTimer(partialTicks);
	}

	@Override
	protected float getWhiteOverlayProgress(TripodCameraRenderState renderState) {
		float timer = renderState.lerpedTimer;
		float timedTimer = (timer * (float) Math.PI) * 0.1F;
		float sin = (float) (Math.sin(timedTimer - (float) Math.PI * 0.5F) + 1F) * 0.5F;
		return sin;
	}

	@Override
	public Identifier getTextureLocation(TripodCameraRenderState renderState) {
		return TEXTURE;
	}
}
