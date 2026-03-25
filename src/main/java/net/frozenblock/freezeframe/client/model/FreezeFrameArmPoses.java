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

package net.frozenblock.freezeframe.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FreezeFrameArmPoses {
	public static final float HIGHEST_LOOK_ROT = Mth.HALF_PI * 0.75F;
	public static HumanoidModel.ArmPose CAMERA;
	public static HumanoidModel.ArmPose CAMERA_ONE_ARM;

	static {
		HumanoidModel.ArmPose.values();
	}
}
