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

package net.lunade.camera.client.model.object.camera;

import net.lunade.camera.client.renderer.entity.state.TripodCameraRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class DiscCameraModel extends EntityModel<TripodCameraRenderState> {
	final ModelPart head;
	final ModelPart disc;
	final ModelPart disc2;

	public DiscCameraModel(ModelPart root) {
		super(root);
		this.disc = root.getChild("disc1");
		this.disc2 = root.getChild("disc2");
		this.head = root.getChild("head");
	}

	public static LayerDefinition getTexturedModelData() {
		final MeshDefinition mesh = new MeshDefinition();
		final PartDefinition root = mesh.getRoot();

		CameraModel.createHead(root, 15F);

		CubeListBuilder discCube = createDiscCube();
		createDisc(root, 1, discCube, 45F * Mth.DEG_TO_RAD);
		createDisc(root, 2, discCube, -45F * Mth.DEG_TO_RAD);

		return LayerDefinition.create(mesh, 64, 32);
	}

	public static CubeListBuilder createDiscCube() {
		return CubeListBuilder.create().texOffs(0, 18).addBox(-7.5F, 0F, 0F, 15F, 10F, 0F);
	}

	public static PartDefinition createDisc(PartDefinition root, int index, CubeListBuilder cubeListBuilder, float yRot) {
		return root.addOrReplaceChild("disc" + index, cubeListBuilder, PartPose.offsetAndRotation(0F, 16.5F, 0F, 0F, yRot, 0F).scaled(1.3F, 0.9F, 1.3F));
	}

	@Override
	public void setupAnim(TripodCameraRenderState renderState) {
		super.setupAnim(renderState);
		this.disc.yRot = 45F * Mth.DEG_TO_RAD;
		this.disc2.yRot = -45F * Mth.DEG_TO_RAD;

		this.head.yRot = renderState.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD;
	}
}
