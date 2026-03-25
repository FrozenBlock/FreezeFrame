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

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class DiscCameraModel extends TripodCameraModel {
	final ModelPart head;
	final ModelPart disc;

	public DiscCameraModel(ModelPart root) {
		super(root);
		this.disc = root.getChild("disc");
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		final MeshDefinition mesh = new MeshDefinition();
		final PartDefinition root = mesh.getRoot();

		TripodCameraModel.createHead(root, 15F);

		root.addOrReplaceChild(
			"disc",
			CubeListBuilder.create()
				.texOffs(0, 18)
				.addBox(-7.5F, 0F, 0F, 15F, 10F, 0F)
				.texOffs(0, 3)
				.addBox(0F, 0F, -7.5F, 0F, 10F, 15F)
				.mirror(),
			PartPose.offsetAndRotation(0F, 16.5F, 0F, 0F, Mth.DEG_TO_RAD * 45F, 0F)
				.scaled(1.3F, 0.9F, 1.3F)
		);

		createLeg(root, 1, CubeListBuilder.create(), 0F, 0F);
		createLeg(root, 2, CubeListBuilder.create(), 0F, 0F);
		createLeg(root, 3, CubeListBuilder.create(), 0F, 0F);
		createLeg(root, 4, CubeListBuilder.create(), 0F, 0F);

		return LayerDefinition.create(mesh, 40, 40);
	}

	@Override
	protected void setRootYOffset(float offset) {
	}
}
