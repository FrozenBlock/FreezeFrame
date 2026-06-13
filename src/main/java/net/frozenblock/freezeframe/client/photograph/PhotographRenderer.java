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

package net.frozenblock.freezeframe.client.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
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
	private static final float PHOTO_TO_FRAME_SCALE = 80F / 70F;
	private static final int BOOK_PHOTO_SIZE = 84;
	private static final int BOOK_PHOTOGRAPH_HOLDER_SIZE = 98;
	private static final int BOOK_PHOTOGRAPH_HOLDER_X_OFFSET = -7;
	private static final int BOOK_PHOTOGRAPH_HOLDER_Y_OFFSET = -7;
	private static final Identifier BOOK_PHOTOGRAPH_HOLDER_BACK = FFConstants.id("container/book/photograph_holder_back");
	private static final Identifier BOOK_PHOTOGRAPH_HOLDER_FRONT = FFConstants.id("container/book/photograph_holder_front");

	public static void submit(PoseStack poseStack, SubmitNodeCollector collector, Identifier id, int lightCoords, FrameType frameType, FrameType frameBackType) {
		poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
		poseStack.translate(-0.5F, -0.5F, 0F);

		final RenderType frameRenderType = frameType.renderType;
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

		final RenderType frameBackRenderType = frameBackType.renderType;
		if (frameBackRenderType != null) {
			poseStack.pushPose();
			poseStack.translate(0F, 1F, 0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(180F));
			collector.submitCustomGeometry(
				poseStack,
				frameBackRenderType,
				(pose, buffer) -> {
					buffer.addVertex(pose, -IN_WORLD_FRAME_MIN, -IN_WORLD_FRAME_MAX, 0F).setColor(-1).setUv(0F, 0F).setLight(lightCoords);
					buffer.addVertex(pose, -IN_WORLD_FRAME_MAX, -IN_WORLD_FRAME_MAX, 0F).setColor(-1).setUv(1F, 0F).setLight(lightCoords);
					buffer.addVertex(pose, -IN_WORLD_FRAME_MAX, -IN_WORLD_FRAME_MIN, 0F).setColor(-1).setUv(1F, 1F).setLight(lightCoords);
					buffer.addVertex(pose, -IN_WORLD_FRAME_MIN, -IN_WORLD_FRAME_MIN, 0F).setColor(-1).setUv(0F, 1F).setLight(lightCoords);
				}
			);
			poseStack.popPose();
		}

		collector.submitCustomGeometry(
			poseStack,
			RenderTypes.text(PhotographLoader.getAndLoadPhotograph(id)),
			(pose, buffer) -> {
				buffer.addVertex(pose, 0F, 1F, -0.00007812F).setColor(-1).setUv(0F, 1F).setLight(lightCoords);
				buffer.addVertex(pose, 1F, 1F, -0.00007812F).setColor(-1).setUv(1F, 1F).setLight(lightCoords);
				buffer.addVertex(pose, 1F, 0F, -0.00007812F).setColor(-1).setUv(1F, 0F).setLight(lightCoords);
				buffer.addVertex(pose, 0F, 0F, -0.00007812F).setColor(-1).setUv(0F, 0F).setLight(lightCoords);
			}
		);
	}

	public static void blit(int x, int y, int xOffset, int yOffset, GuiGraphicsExtractor graphics, Identifier id, int renderSize, FrameType frameType) {
		final int renderX = x + xOffset;
		final int renderY = y + yOffset;

		final Identifier frameSprite = frameType.guiSprite;
		if (frameSprite != null) {
			// This calculates the size of one pixel in the Frame texture. All Frame textures are 80x80.
			final float relativeScale = renderSize / 80F;

			graphics.pose().pushMatrix();
			graphics.pose().translate(renderX, renderY);
			graphics.pose().pushMatrix();
			graphics.pose().scale(PHOTO_TO_FRAME_SCALE);
			graphics.pose().translate(-relativeScale * 5F, -relativeScale * 5F);
			graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				frameSprite,
				0, 0,
				renderSize, renderSize
			);
			graphics.pose().popMatrix();
			graphics.pose().popMatrix();
		}
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			PhotographLoader.getAndLoadPhotograph(id),
			renderX, renderY,
			0F,
			0F,
			renderSize, renderSize,
			renderSize, renderSize
		);
	}

	public static void blitForBook(int xOffset, int yOffset, GuiGraphicsExtractor graphics, Identifier id) {
		final int holderX = xOffset + BOOK_PHOTOGRAPH_HOLDER_X_OFFSET;
		final int holderY = yOffset + BOOK_PHOTOGRAPH_HOLDER_Y_OFFSET;
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			BOOK_PHOTOGRAPH_HOLDER_BACK,
			holderX,
			holderY,
			BOOK_PHOTOGRAPH_HOLDER_SIZE,
			BOOK_PHOTOGRAPH_HOLDER_SIZE
		);
		blit(0, 0, xOffset, yOffset, graphics, id, BOOK_PHOTO_SIZE, PhotographRenderer.FrameType.FRAME);
		graphics.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			BOOK_PHOTOGRAPH_HOLDER_FRONT,
			holderX,
			holderY,
			BOOK_PHOTOGRAPH_HOLDER_SIZE,
			BOOK_PHOTOGRAPH_HOLDER_SIZE
		);
	}

	public enum FrameType {
		NONE(null,  null),
		FRAME("frame"),
		FRAME_BACK("frame_back"),
		FRAME_FULL("frame_full"),
		FILM_EMBED("film_embed");
		private final RenderType renderType;
		private final Identifier guiSprite;

		FrameType(RenderType renderType, Identifier guiSprite) {
			this.renderType = renderType;
			this.guiSprite = guiSprite;
		}

		FrameType(String name) {
			this(createRenderType(FFConstants.id(name)), createGuiSprite(FFConstants.id(name)));
		}

		public RenderType renderType() {
			return this.renderType;
		}

		public Identifier guiSprite() {
			return this.guiSprite;
		}

		public static RenderType createRenderType(Identifier id) {
			return RenderTypes.text(id.withPath(path -> "textures/gui/spries/photograph/" + path + ".png"));
		}

		public static Identifier createGuiSprite(Identifier id) {
			return id.withPath(path -> "photograph/" + path);
		}
	}
}
