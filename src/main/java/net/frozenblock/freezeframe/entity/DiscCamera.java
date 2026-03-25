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

package net.frozenblock.freezeframe.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DiscCamera extends TripodCamera {

	public DiscCamera(EntityType<? extends DiscCamera> type, Level level) {
		super(type, level);
		this.setTrackedHeight(0.9F);
	}

	@Override
	public float getMaxHeight() {
		return 0.9F;
	}

	@Override
	public float getMinHeight() {
		return 0.9F;
	}

	@Override
	public float getBoundingBoxRadius() {
		return 0.275F;
	}

	@Override
	public boolean canBeAdjusted() {
		return false;
	}

}
