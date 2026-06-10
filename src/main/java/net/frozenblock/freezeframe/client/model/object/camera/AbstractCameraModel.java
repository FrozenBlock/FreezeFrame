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
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public abstract class AbstractCameraModel extends EntityModel<TripodCameraRenderState> {
	protected final ModelPart head;

	public AbstractCameraModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
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

	@Override
	public void setupAnim(TripodCameraRenderState renderState) {
		super.setupAnim(renderState);

		this.head.yRot = renderState.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD;
	}
}
