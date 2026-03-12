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
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.component.FilmContents;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.tooltip.CameraTooltip;
import net.lunade.camera.networking.packet.CameraTakeScreenshotPacket;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortSounds;
import net.lunade.camera.util.CameraScreenshotHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public class CameraItem extends SpawnEggItem {
	private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1F, 1F, 0.33F, 0.33F);
	private static final int BAR_COLOR = ARGB.colorFromFloat(1F, 0.44F, 0.53F, 1F);

	public CameraItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		final InteractionResult interactionResult = super.use(level, player, hand);
		if (interactionResult.consumesAction()) return interactionResult;

		final ItemStack stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack)) return interactionResult;

		final CameraContents initialContents = stack.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialContents == null) return InteractionResult.PASS;

		if (!initialContents.hasSpaceForPhotograph()) {
			// TODO: Fail sound
			return InteractionResult.FAIL;
		}

		player.getCooldowns().addCooldown(stack, 20);
		if (player instanceof ServerPlayer serverPlayer) {
			final String fileName = CameraScreenshotHelper.makeFileName(serverPlayer);
			CameraTakeScreenshotPacket.sendToAsHandheld(serverPlayer, fileName);
			this.addPhotograph(stack, player, fileName);
		}

		level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), CameraPortSounds.CAMERA_SNAP, SoundSource.PLAYERS, 0.5F, 1F);
		return InteractionResult.SUCCESS;
	}

	private static Fraction getWeightSafe(CameraContents contents) {
		return switch (contents.weight()) {
			case DataResult.Success<Fraction> success -> success.value();
			case DataResult.Error<?> error -> Fraction.ONE;
		};
	}

	public static float getFullnessDisplay(ItemStack stack) {
		final CameraContents contents = stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY);
		return getWeightSafe(contents).floatValue();
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
		final CameraContents initialContents = self.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialContents == null) return false;

		final ItemStack other = slot.getItem();
		final CameraContents.Mutable contents = new CameraContents.Mutable(initialContents);
		if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
			if (contents.tryTransfer(slot, player) > 0) {
				playInsertSound(player);
			} else {
				playInsertFailSound(player);
			}

			self.set(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
			this.broadcastChangesOnContainerMenu(player);
			return true;
		} else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
			final ItemStack removed = contents.removeOne();
			if (removed != null) {
				final ItemStack remainder = slot.safeInsert(removed);
				if (remainder.getCount() > 0) {
					contents.tryInsert(remainder);
				} else {
					playRemoveOneSound(player);
				}
			}

			self.set(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
			this.broadcastChangesOnContainerMenu(player);
			return true;
		}

		return false;
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
		if (clickAction == ClickAction.PRIMARY && other.isEmpty()) {
			toggleSelectedItem(self, -1);
			return false;
		}

		final CameraContents initialContents = self.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialContents == null) return false;

		final CameraContents.Mutable contents = new CameraContents.Mutable(initialContents);
		if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
			if (slot.allowModification(player) && contents.tryInsert(other) > 0) {
				playInsertSound(player);
			} else {
				playInsertFailSound(player);
			}

			self.set(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
			this.broadcastChangesOnContainerMenu(player);
			return true;
		} else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
			if (slot.allowModification(player)) {
				final ItemStack removed = contents.removeOne();
				if (removed != null) {
					playRemoveOneSound(player);
					carriedItem.set(removed);
				}
			}

			self.set(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
			this.broadcastChangesOnContainerMenu(player);
			return true;
		}

		toggleSelectedItem(self, -1);
		return false;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		final CameraContents contents = stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY);
		return getWeightSafe(contents).compareTo(Fraction.ZERO) > 0;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		final CameraContents contents = stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY);
		return Math.min(1 + Mth.mulAndTruncate(getWeightSafe(contents), 12), 12 + 1);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		final CameraContents contents = stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY);
		return getWeightSafe(contents).compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
	}

	public static void toggleSelectedItem(ItemStack stack, int selectedItem) {
		final CameraContents initialContents = stack.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialContents == null) return;

		final CameraContents.Mutable contents = new CameraContents.Mutable(initialContents);
		contents.toggleSelectedItem(selectedItem);
		stack.set(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
	}

	public static int getSelectedItemIndex(final ItemStack stack) {
		return stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY).getSelectedItemIndex();
	}

	@Nullable
	public static ItemStackTemplate getSelectedItem(final ItemStack stack) {
		return stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY).getSelectedItem();
	}

	public static int getNumberOfItemsToShow(final ItemStack stack) {
		final CameraContents contents = stack.getOrDefault(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY);
		return contents.getNumberOfItemsToShow();
	}

	private static Optional<ItemStack> removeOneItemFromCamera(ItemStack self, Player player, CameraContents initialContents) {
		final CameraContents.Mutable contents = new CameraContents.Mutable(initialContents);
		final ItemStack removed = contents.removeOne();
		if (removed == null) return Optional.empty();

		playRemoveOneSound(player);
		self.set(CameraPortDataComponents.CAMERA_CONTENTS, contents.toImmutable());
		return Optional.of(removed);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack camera) {
		final TooltipDisplay display = camera.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		return !display.shows(CameraPortDataComponents.CAMERA_CONTENTS)
			? Optional.empty()
			: Optional.ofNullable(camera.get(CameraPortDataComponents.CAMERA_CONTENTS)).map(CameraTooltip::new);
	}

	@Override
	public void onDestroyed(ItemEntity entity) {
		final CameraContents contents = entity.getItem().get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (contents == null) return;
		entity.getItem().set(CameraPortDataComponents.CAMERA_CONTENTS, CameraContents.EMPTY);
		ItemUtils.onContainerDestroyed(entity, contents.itemCopyStream());
	}

	public void addPhotograph(ItemStack stack, Player player, String fileName) {
		final CameraContents initialCameraContents = stack.get(CameraPortDataComponents.CAMERA_CONTENTS);
		if (initialCameraContents == null) return;

		final CameraContents.Mutable cameraContents = new CameraContents.Mutable(initialCameraContents);
		final Optional<ItemStack> potentialFilm = cameraContents.findFirstWithSpaceForPhotograph();
		if (potentialFilm.isEmpty()) return;

		final ItemStack film = potentialFilm.get();
		final FilmContents.Mutable filmContents = new FilmContents.Mutable(film.getOrDefault(CameraPortDataComponents.FILM_CONTENTS, FilmContents.EMPTY));
		final PhotographComponent photograph = new PhotographComponent(CameraPortConstants.id(fileName), player.getPlainTextName());
		if (!filmContents.tryInsert(photograph)) return;

		film.set(CameraPortDataComponents.FILM_CONTENTS, filmContents.toImmutable());
		stack.set(CameraPortDataComponents.CAMERA_CONTENTS, cameraContents.toImmutable());
		this.broadcastChangesOnContainerMenu(player);
	}

	// TODO: Sounds
	private static void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private static void playInsertFailSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1F, 1F);
	}

	private void broadcastChangesOnContainerMenu(Player player) {
		final AbstractContainerMenu containerMenu = player.containerMenu;
		if (containerMenu != null) containerMenu.slotsChanged(player.getInventory());
	}

}
