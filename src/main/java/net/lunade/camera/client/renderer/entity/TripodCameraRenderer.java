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

package net.lunade.camera.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortClient;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.model.object.camera.TripodCameraModel;
import net.lunade.camera.client.renderer.entity.state.TripodCameraRenderState;
import net.lunade.camera.entity.TripodCamera;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TripodCameraRenderer extends MobRenderer<TripodCamera, TripodCameraRenderState, TripodCameraModel> {
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/entity/camera.png");

	public TripodCameraRenderer(EntityRendererProvider.Context context) {
		super(context, new TripodCameraModel(context.bakeLayer(CameraPortClient.CAMERA_MODEL_LAYER)), 0.5F);
	}

	@Override
	public TripodCameraRenderState createRenderState() {
		return new TripodCameraRenderState();
	}

	@Override
	public void extractRenderState(TripodCamera entity, TripodCameraRenderState renderState, float partialTicks) {
		super.extractRenderState(entity, renderState, partialTicks);
		renderState.trackedHeight = entity.getTrackedHeight();
		renderState.lerpedTimer = entity.getLerpedTimer(partialTicks);
		renderState.wiggle = (float)(entity.level().getGameTime() - entity.lastHit) + partialTicks;
	}

	@Override
	protected float getWhiteOverlayProgress(TripodCameraRenderState renderState) {
		final float timer = renderState.lerpedTimer;
		final float timedTimer = (timer * (float) Math.PI) * 0.1F;
		final float sin = (float) (Math.sin(timedTimer - (float) Math.PI * 0.5F) + 1F) * 0.5F;
		return sin;
	}

	@Override
	protected void setupRotations(TripodCameraRenderState renderState, PoseStack poseStack, float bodyRot, float entityScale) {
		super.setupRotations(renderState, poseStack, bodyRot, entityScale);
		if (renderState.wiggle < 5F) poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(renderState.wiggle / 1.5F * Mth.PI) * 3F));
	}

	@Override
	public Identifier getTextureLocation(TripodCameraRenderState renderState) {
		return TEXTURE;
	}
}
