package net.lunade.camera.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortClient;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.client.model.object.camera.CameraModel;
import net.lunade.camera.client.renderer.entity.state.TripodCameraRenderState;
import net.lunade.camera.entity.TripodCamera;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class TripodCameraRenderer extends MobRenderer<TripodCamera, TripodCameraRenderState, CameraModel> {
	private static final Identifier TEXTURE = CameraPortConstants.id("textures/entity/camera.png");

	public TripodCameraRenderer(EntityRendererProvider.Context context) {
		super(context, new CameraModel(context.bakeLayer(CameraPortClient.CAMERA_MODEL_LAYER)), 0.5F);
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
