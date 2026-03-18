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

package net.lunade.camera.client.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.CameraPortConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class PhotographRenderer {
	private static final float IN_WORLD_FRAME_EXTENSION = 1.125F / 16F;
	private static final float IN_WORLD_FRAME_MAX = 1F + IN_WORLD_FRAME_EXTENSION;
	private static final float IN_WORLD_FRAME_MIN = -IN_WORLD_FRAME_EXTENSION;
	private static final Identifier FRAME = CameraPortConstants.id("textures/gui/sprites/photograph/frame.png");
	private static final RenderType FRAME_RENDER_TYPE = RenderTypes.text(FRAME);
	private static final Identifier GUI_FRAME = CameraPortConstants.id("photograph/frame");
	private static final Identifier FRAME_FULL = CameraPortConstants.id("textures/gui/sprites/photograph/frame_full.png");
	private static final RenderType FRAME_FULL_RENDER_TYPE = RenderTypes.text(FRAME_FULL);
	private static final Identifier GUI_FRAME_FULL = CameraPortConstants.id("photograph/frame_full");
	private static final Identifier FILM_EMBED = CameraPortConstants.id("textures/gui/sprites/photograph/film_embed.png");
	private static final RenderType FILM_EMBED_RENDER_TYPE = RenderTypes.text(FILM_EMBED);
	private static final Identifier GUI_FILM_EMBED = CameraPortConstants.id("photograph/film_embed");

	public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Identifier photographId, int lightCoords, FrameType frameType) {
		poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
		poseStack.translate(-0.5F, -0.5F, 0F);

		final RenderType frameRenderType = frameType.renderType();
		if (frameRenderType != null) {
			collector.submitCustomGeometry(
				poseStack,
				frameRenderType,
				(pose, buffer) -> {
					buffer.addVertex(pose, IN_WORLD_FRAME_MIN, IN_WORLD_FRAME_MAX, 0F).setColor(-1).setUv(0F, 1F).setLight(lightCoords);
					buffer.addVertex(pose, IN_WORLD_FRAME_MAX, IN_WORLD_FRAME_MAX, 0F).setColor(-1).setUv(1F, 1F).setLight(lightCoords);
					buffer.addVertex(pose, IN_WORLD_FRAME_MAX, IN_WORLD_FRAME_MIN, 0F).setColor(-1).setUv(1F, 0F).setLight(lightCoords);
					buffer.addVertex(pose, IN_WORLD_FRAME_MIN, IN_WORLD_FRAME_MIN, 0F).setColor(-1).setUv(0F, 0F).setLight(lightCoords);
				}
			);
		}

		final Identifier loadedPhotoLocation = PhotographLoader.getAndLoadPhotograph(photographId);
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

	public static void blit(int x, int y, int xOffset, int yOffset, GuiGraphicsExtractor graphics, Identifier photoLocation, int renderSize, FrameType frameType) {
		final Identifier loadedPhotoId = PhotographLoader.getAndLoadPhotograph(photoLocation);
		final int renderX = x + xOffset;
		final int renderY = y + yOffset;

		final Identifier frameSprite = frameType.guiSprite;
		if (frameSprite != null) {
			final double frameOffsetScale = renderSize / 80D;
			final int posOffset = (int) (5 * frameOffsetScale);
			final int sizeOffset = (int) (10 * frameOffsetScale);
			final int frameRenderSize = renderSize + sizeOffset;
			graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				frameSprite,
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

	public static enum FrameType {
		NONE(null,  null),
		FRAME(FRAME_RENDER_TYPE, GUI_FRAME),
		FRAME_FULL(FRAME_FULL_RENDER_TYPE, GUI_FRAME_FULL),
		FILM_EMBED(FILM_EMBED_RENDER_TYPE, GUI_FILM_EMBED);
		private final RenderType renderType;
		private final Identifier guiSprite;

		FrameType(RenderType renderType, Identifier guiSprite) {
			this.renderType = renderType;
			this.guiSprite = guiSprite;
		}

		public RenderType renderType() {
			return this.renderType;
		}

		public Identifier guiSprite() {
			return this.guiSprite;
		}
	}
}
