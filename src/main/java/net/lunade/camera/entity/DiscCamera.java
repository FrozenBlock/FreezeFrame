package net.lunade.camera.entity;

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
