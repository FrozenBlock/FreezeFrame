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

import java.util.Optional;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.tooltip.FilmTooltip;
import net.lunade.camera.networking.packet.OpenFilmScreenPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemInstance;
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

	public static int normalizeMaxPhotographs(int maxPhotographs) {
		return Mth.clamp(maxPhotographs, FilmContents.BASE_MAX_PHOTOGRAPHS, FilmContents.ABSOLUTE_MAX_PHOTOGRAPHS);
	}

	public static int getMaxPhotographs(ItemInstance filmItem) {
		final Integer maxPhotographs = filmItem.get(CameraPortDataComponents.FILM_MAX_PHOTOGRAPHS);
		if (maxPhotographs == null) return FilmContents.BASE_MAX_PHOTOGRAPHS;
		return normalizeMaxPhotographs(maxPhotographs);
	}

	public static Fraction getWeightSafe(FilmContents contents, int maxPhotographs) {
		return Fraction.getFraction(contents.size(), Math.max(1, normalizeMaxPhotographs(maxPhotographs)));
	}

	public static Fraction getWeightSafe(FilmContents contents, ItemInstance filmItem) {
		return getWeightSafe(contents, getMaxPhotographs(filmItem));
	}

	public static float getFullnessDisplay(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return getWeightSafe(contents, stack).floatValue();
	}

	@Override
	public InteractionResult use(net.minecraft.world.level.Level level, Player player, InteractionHand hand) {
		final ItemStack stack = player.getItemInHand(hand);
		if (!stack.isEmpty() && !stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY).isEmpty()) {
			if (player instanceof ServerPlayer serverPlayer) {
				OpenFilmScreenPacket.sendTo(serverPlayer, hand);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot equipmentSlot) {
		if (needsStackingRefresh(stack)) {
			refreshStackingState(stack);
		}
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return getWeightSafe(contents, stack).compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return Math.min(1 + Mth.mulAndTruncate(getWeightSafe(contents, stack), 12), 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		return getWeightSafe(contents, stack).compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
	}

	public static void toggleSelectedPhotograph(ItemStack stack, int selectedPhotograph) {
		final FilmContents initialContents = stack.get(CameraPortDataComponents.FILM_CONTENTS);
		if (initialContents == null) return;

		final FilmContents.Mutable contents = new FilmContents.Mutable(initialContents);
		contents.toggleSelectedPhotograph(selectedPhotograph);
		stack.set(CameraPortDataComponents.FILM_CONTENTS, contents.toImmutable());
		refreshStackingState(stack);
	}

	public static void refreshStackingState(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		final int expectedMaxStackSize = contents.isEmpty() ? stack.getItem().getDefaultMaxStackSize() : 1;
		stack.set(DataComponents.MAX_STACK_SIZE, expectedMaxStackSize);
	}

	private static boolean needsStackingRefresh(ItemStack stack) {
		final FilmContents contents = stack.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		final int expectedMaxStackSize = contents.isEmpty() ? stack.getItem().getDefaultMaxStackSize() : 1;
		return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, stack.getItem().getDefaultMaxStackSize()) != expectedMaxStackSize;
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
		if (contents != null) return Optional.of(new FilmTooltip(contents, getMaxPhotographs(stack)));
		return Optional.empty();
	}

}
