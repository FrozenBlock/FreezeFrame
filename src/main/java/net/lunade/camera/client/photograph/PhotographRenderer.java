package net.lunade.camera.client.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class PhotographRenderer {
	private static final Identifier FRAME = CameraPortConstants.id("textures/gui/sprites/photograph/frame.png");
	private static final RenderType FRAME_RENDER_TYPE = RenderTypes.text(FRAME);
	private static final Identifier GUI_FRAME = CameraPortConstants.id("photograph/frame");

	public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Identifier photographId, int lightCoords, boolean renderFrame) {
		poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
		poseStack.translate(-0.5F, -0.5F, 0F);

		if (renderFrame) {
			collector.submitCustomGeometry(
				poseStack,
				FRAME_RENDER_TYPE,
				(pose, buffer) -> {
					buffer.addVertex(pose, -0.0625F, 1.0625F, 0F).setColor(-1).setUv(0F, 1F).setLight(lightCoords);
					buffer.addVertex(pose, 1.0625F, 1.0625F, 0F).setColor(-1).setUv(1F, 1F).setLight(lightCoords);
					buffer.addVertex(pose, 1.0625F, -0.0625F, 0F).setColor(-1).setUv(1F, 0F).setLight(lightCoords);
					buffer.addVertex(pose, -0.0625F, -0.0625F, 0F).setColor(-1).setUv(0F, 0F).setLight(lightCoords);
				}
			);
		}

		final Identifier loadedPhotoLocation = PhotographLoader.getAndLoadPhotograph(photographId, false);
		collector.submitCustomGeometry(
			poseStack,
			RenderTypes.text(loadedPhotoLocation),
			(pose, buffer) -> {
				buffer.addVertex(pose, 0F, 1F, -0.00007812F).setColor(-1).setUv(0F, 1F).setLight(lightCoords);
				buffer.addVertex(pose, 1F, 1F, -0.00007812F).setColor(-1).setUv(1F, 1F).setLight(lightCoords);
				buffer.addVertex(pose, 1F, 0F, -0.00007812F).setColor(-1).setUv(1F, 0F).setLight(lightCoords);
				buffer.addVertex(pose, 0F, 0F, -0.00007812F).setColor(-1).setUv(0F, 0F).setLight(lightCoords);
			}
		);
	}

	public static void blit(int x, int y, int xOffset, int yOffset, GuiGraphics graphics, Identifier photoLocation, int renderSize, boolean renderFrame) {
		final Identifier loadedPhotoId = PhotographLoader.getAndLoadPhotograph(photoLocation, false);
		final int renderX = x + xOffset;
		final int renderY = y + yOffset;
		if (renderFrame) {
			final double frameOffsetScale = renderSize / 80D;
			final int posOffset = (int) (5 * frameOffsetScale);
			final int sizeOffset = (int) (10 * frameOffsetScale);
			final int frameRenderSize = renderSize + sizeOffset;
			graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				GUI_FRAME,
				renderX - posOffset,
				renderY - posOffset,
				frameRenderSize, frameRenderSize
			);
		}
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			loadedPhotoId,
			renderX, renderY,
			0F,
			0F,
			renderSize, renderSize, renderSize, renderSize
		);
	}
}
