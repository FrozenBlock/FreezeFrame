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

package net.frozenblock.freezeframe.client.model.object.camera;

import java.util.Arrays;
import net.frozenblock.freezeframe.client.renderer.entity.state.TripodCameraRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class TripodCameraModel extends AbstractCameraModel {
	private static final float HEIGHT_INCREMENT = 1.75F;
	private static final float HEIGHT_SCALE = 15F / HEIGHT_INCREMENT;
	private static final float LEG_ANGLE_MULTIPLIER = 6.7F;
	private static final float ROOT_ADJUSTMENT_BY_HEIGHT = LEG_ANGLE_MULTIPLIER / 3.575F;
	private static final int LEG_COUNT = 3;
	private final ModelPart[] legs = new ModelPart[3];

	public TripodCameraModel(ModelPart root) {
		super(root);
		Arrays.setAll(this.legs, i -> root.getChild(createLegName(i)));
	}

	public static LayerDefinition createBodyLayer() {
		final MeshDefinition mesh = new MeshDefinition();
		final PartDefinition root = mesh.getRoot();

		createHead(root, 2F);

		final CubeListBuilder leg = CubeListBuilder.create().texOffs(36, 0).addBox(-0.5F, 0F, -0.5F, 1F, 25F, 1F);
		for (int i = 0; i < LEG_COUNT; i++) {
			final float rot = (i - 0.25F) * Mth.TWO_PI / LEG_COUNT;
			root.addOrReplaceChild(
				createLegName(i),
				leg,
				PartPose.offsetAndRotation(
					Mth.cos(rot),
					0F,
					Mth.sin(rot),
					0F,
					(i - 0.25F) * -Mth.TWO_PI / LEG_COUNT + Mth.HALF_PI,
					0F
				)
			);
		}

		return LayerDefinition.create(mesh, 40, 40);
	}

	protected static String createLegName(int number) {
		return "leg" + number;
	}

	@Override
	public void setupAnim(TripodCameraRenderState renderState) {
		super.setupAnim(renderState);

		float inverseHeight = (HEIGHT_INCREMENT - renderState.trackedHeight) * HEIGHT_SCALE;
		this.root.y += inverseHeight * ROOT_ADJUSTMENT_BY_HEIGHT;

		final float legAngle = (15F + (inverseHeight * LEG_ANGLE_MULTIPLIER)) * Mth.DEG_TO_RAD;
		Arrays.stream(this.legs).forEach(leg -> leg.xRot = legAngle);
	}
}
