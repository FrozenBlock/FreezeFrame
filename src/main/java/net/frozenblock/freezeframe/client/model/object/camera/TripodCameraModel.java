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

import net.frozenblock.freezeframe.client.renderer.entity.state.TripodCameraRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class TripodCameraModel extends EntityModel<TripodCameraRenderState> {
	private static final float HEIGHT_INCREMENT = 1.75F;
	private static final float HEIGHT_SCALE = 15F / HEIGHT_INCREMENT;
	private static final float LEG_ANGLE_MULTIPLIER = 6.7F;
	private static final float ROOT_ADJUSTMENT_BY_HEIGHT = LEG_ANGLE_MULTIPLIER / 3.575F;
	private final ModelPart head;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart leg4;

	public TripodCameraModel(ModelPart root) {
		super(root);
		this.leg1 = root.getChild("leg1");
		this.leg2 = root.getChild("leg2");
		this.leg3 = root.getChild("leg3");
		this.leg4 = root.getChild("leg4");
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		final MeshDefinition mesh = new MeshDefinition();
		final PartDefinition root = mesh.getRoot();

		createHead(root, 2F);

		final CubeListBuilder legCube = createLegCube();
		createLeg(root, 1, legCube, 0F, 1F);
		createLeg(root, 2, legCube, 0F, -1F);
		createLeg(root, 3, legCube, 1F, 0F);
		createLeg(root, 4, legCube, -1F, 0F);

		return LayerDefinition.create(mesh, 40, 40);
	}

	public static PartDefinition createHead(PartDefinition root, float verticalOffset) {
		return root.addOrReplaceChild(
			"head",
			CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-4F, -8F, -5F, 8F, 8F, 10F),
			PartPose.offset(0F, verticalOffset, 0F)
		);
	}

	public static PartDefinition createLeg(PartDefinition root, int index, CubeListBuilder cubeListBuilder, float xOffset, float zOffset) {
		return root.addOrReplaceChild("leg" + index, cubeListBuilder, PartPose.offset(xOffset, 0F, zOffset));
	}

	public static CubeListBuilder createLegCube() {
		return CubeListBuilder.create().texOffs(36, 0).addBox(-0.5F, 0F, -0.5F, 1F, 25F, 1F);
	}

	@Override
	public void setupAnim(TripodCameraRenderState renderState) {
		super.setupAnim(renderState);

		float inverseHeight = (HEIGHT_INCREMENT - renderState.trackedHeight) * HEIGHT_SCALE;
		this.setRootYOffset(inverseHeight * ROOT_ADJUSTMENT_BY_HEIGHT);

		final float legAngle = (15F + (inverseHeight * LEG_ANGLE_MULTIPLIER)) * Mth.DEG_TO_RAD;
		this.leg1.xRot = legAngle;
		this.leg2.xRot = -legAngle;
		this.leg3.zRot = -legAngle;
		this.leg4.zRot = legAngle;

		this.head.yRot = renderState.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD;
	}

	protected void setRootYOffset(float offset) {
		this.root.y += offset;
	}
}
