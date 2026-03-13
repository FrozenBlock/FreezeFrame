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

package net.lunade.camera.item;

import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.tooltip.FilmTooltip;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public class FilmItem extends Item {
	private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1F, 1F, 0.33F, 0.33F);
	private static final int BAR_COLOR = 0xFFB8895F;

	public FilmItem(Properties properties) {
		super(properties);
	}

	public static Fraction getWeightSafe(FilmContents contents) {
		return switch (contents.weight()) {
			case DataResult.Success<Fraction> success -> success.value();
			case DataResult.Error<?> error -> Fraction.ONE;
		};
	}

	public static float getFullnessDisplay(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return getWeightSafe(contents).floatValue();
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return getWeightSafe(contents).compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return Math.min(1 + Mth.mulAndTruncate(getWeightSafe(contents), 12), 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return getWeightSafe(contents).compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
	}

	public static void toggleSelectedPhotograph(ItemStack stack, int selectedPhotograph) {
		final FilmContents initialContents = stack.get(CameraPortDataComponents.FILM_CONTENTS);
		if (initialContents == null) return;

		final FilmContents.Mutable contents = new FilmContents.Mutable(initialContents);
		contents.toggleSelectedPhotograph(selectedPhotograph);
		stack.set(CameraPortDataComponents.FILM_CONTENTS, contents.toImmutable());
	}

	public static int getSelectedPhotographIndex(ItemStack stack) {
		return stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY).getSelectedPhotographIndex();
	}

	@Nullable
	public static PhotographComponent getSelectedPhotograph(ItemStack stack) {
		return stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY).getSelectedPhotograph();
	}

	public static int getNumberOfPhotographs(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return contents.size();
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		final TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		if (!tooltipDisplay.shows(CameraPortDataComponents.FILM_CONTENTS)) return Optional.empty();

		final FilmContents contents = stack.get(CameraPortDataComponents.FILM_CONTENTS);
		if (contents != null) return Optional.of(new FilmTooltip(contents));
		return Optional.empty();
	}

}
