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

package net.lunade.camera.component;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.lunade.camera.item.FilmItem;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public final class CameraContents {
	public static final CameraContents EMPTY = new CameraContents(List.of());
	public static final Codec<CameraContents> CODEC = ItemStackTemplate.CODEC.listOf().xmap(CameraContents::new, contents -> contents.items);
	public static final StreamCodec<RegistryFriendlyByteBuf, CameraContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(CameraContents::new, contents -> contents.items);
	private static final Fraction FILM_WEIGHT = Fraction.getFraction(1, 4);
	private static final int NO_STACK_INDEX = -1;
	public static final int NO_SELECTED_ITEM_INDEX = -1;
	private final List<ItemStackTemplate> items;
	private final int selectedItem;
	private final Supplier<DataResult<Fraction>> weight;

	private CameraContents(List<ItemStackTemplate> items, int selectedItem) {
		this.items = items;
		this.selectedItem = selectedItem;
		this.weight = Suppliers.memoize(() -> computeContentWeight(this.items));
	}

	public CameraContents(List<ItemStackTemplate> items) {
		this(items, -NO_SELECTED_ITEM_INDEX);
	}

	private static DataResult<Fraction> computeContentWeight(List<? extends ItemInstance> items) {
		try {
			Fraction weight = Fraction.ZERO;
			for (ItemInstance stack : items) {
				DataResult<Fraction> itemWeight = getWeight(stack);
				if (itemWeight.isError()) return itemWeight;
				weight = weight.add(itemWeight.getOrThrow().multiplyBy(Fraction.getFraction(stack.count(), 1)));
			}
			return DataResult.success(weight);
		} catch (ArithmeticException ignored) {
			return DataResult.error(() -> "Excessive total camera weight");
		}
	}

	private static DataResult<Fraction> getWeight(ItemInstance item) {
		if (!item.is(CameraPortItems.FILM)) return DataResult.error(() -> "Item is not film");
		return DataResult.success(FILM_WEIGHT);
	}

	public static boolean canItemBeInCamera(ItemStack itemToAdd) {
		return !itemToAdd.isEmpty() && itemToAdd.is(CameraPortItems.FILM);
	}

	public int getNumberOfItemsToShow() {
		final int numberOfItemStacks = this.size();
		final int availableItemsToShow = numberOfItemStacks > 12 ? 11 : 12;
		final int itemsOnNonFullRow = numberOfItemStacks % 4;
		final int emptySpaceOnNonFullRow = itemsOnNonFullRow == 0 ? 0 : 4 - itemsOnNonFullRow;
		return Math.min(numberOfItemStacks, availableItemsToShow - emptySpaceOnNonFullRow);
	}

	public Stream<ItemStack> itemCopyStream() {
		return this.items.stream().map(ItemStackTemplate::create);
	}

	public List<ItemStackTemplate> items() {
		return this.items;
	}

	public int size() {
		return this.items.size();
	}

	public DataResult<Fraction> weight() {
		return this.weight.get();
	}

	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	public int getSelectedItemIndex() {
		return this.selectedItem;
	}

	public boolean hasSpaceForPhotograph() {
		for (ItemStackTemplate stack : this.items) {
			final FilmContents filmContents = stack.get(CameraPortDataComponents.FILM_CONTENTS);
			if (filmContents == null) continue;
			if (FilmItem.getWeightSafe(filmContents).compareTo(Fraction.ONE) < 0) return true;
		}
		return false;
	}

	@Nullable
	public ItemStackTemplate getSelectedItem() {
		return this.selectedItem == -NO_SELECTED_ITEM_INDEX ? null : this.items.get(this.selectedItem);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof CameraContents contents ? this.items.equals(contents.items) : false;
	}

	@Override
	public int hashCode() {
		return this.items.hashCode();
	}

	@Override
	public String toString() {
		return "CameraContents" + this.items;
	}

	public static class Mutable {
		private final List<ItemStack> items;
		private Fraction weight;
		private int selectedItem;

		public Mutable(CameraContents contents) {
			DataResult<Fraction> currentWeight = contents.weight.get();
			if (currentWeight.isError()) {
				this.items = new ArrayList<>();
				this.weight = Fraction.ZERO;
				this.selectedItem = -NO_SELECTED_ITEM_INDEX;
			} else {
				this.items = new ArrayList<>(contents.items.size());
				for (ItemStackTemplate item : contents.items) this.items.add(item.create());
				this.weight = currentWeight.getOrThrow();
				this.selectedItem = contents.selectedItem;
			}
		}

		public Mutable clearItems() {
			this.items.clear();
			this.weight = Fraction.ZERO;
			this.selectedItem = -NO_SELECTED_ITEM_INDEX;
			return this;
		}

		private int findStackIndex(ItemStack itemsToAdd) {
			if (!itemsToAdd.isStackable()) return -NO_STACK_INDEX;
			for (int i = 0; i < this.items.size(); i++) {
				if (ItemStack.isSameItemSameComponents(this.items.get(i), itemsToAdd)) return i;
			}
			return -NO_STACK_INDEX;
		}

		private int getMaxAmountToAdd(Fraction itemWeight) {
			final Fraction remainingWeight = Fraction.ONE.subtract(this.weight);
			return Math.max(remainingWeight.divideBy(itemWeight).intValue(), 0);
		}

		public int tryInsert(ItemStack itemsToAdd) {
			if (!canItemBeInCamera(itemsToAdd)) return 0;

			final DataResult<Fraction> maybeItemWeight = getWeight(itemsToAdd);
			if (maybeItemWeight.isError()) return 0;

			Fraction itemWeight = maybeItemWeight.getOrThrow();
			int amountToAdd = Math.min(itemsToAdd.getCount(), this.getMaxAmountToAdd(itemWeight));
			if (amountToAdd == 0) return 0;

			this.weight = this.weight.add(itemWeight.multiplyBy(Fraction.getFraction(amountToAdd, 1)));
			int stackIndex = this.findStackIndex(itemsToAdd);
			if (stackIndex != -NO_STACK_INDEX) {
				final ItemStack removedStack = this.items.remove(stackIndex);
				final ItemStack mergedStack = removedStack.copyWithCount(removedStack.getCount() + amountToAdd);
				itemsToAdd.shrink(amountToAdd);
				this.items.add(0, mergedStack);
			} else {
				this.items.add(0, itemsToAdd.split(amountToAdd));
			}

			return amountToAdd;
		}

		public int tryTransfer(Slot slot, Player player) {
			final ItemStack other = slot.getItem();
			final DataResult<Fraction> itemWeight = getWeight(other);
			if (itemWeight.isError()) return 0;

			final int maxAmount = this.getMaxAmountToAdd(itemWeight.getOrThrow());
			return canItemBeInCamera(other) ? this.tryInsert(slot.safeTake(other.getCount(), maxAmount, player)) : 0;
		}

		public boolean hasSpaceForPhotograph() {
			return this.findFirstWithSpaceForPhotograph().isPresent();
		}

		public Optional<ItemStack> findFirstWithSpaceForPhotograph() {
			for (ItemStack stack : this.items) {
				final FilmContents filmContents = stack.get(CameraPortDataComponents.FILM_CONTENTS);
				if (filmContents == null) continue;
				if (FilmItem.getWeightSafe(filmContents).compareTo(Fraction.ONE) < 0) return Optional.of(stack);
			}
			return Optional.empty();
		}

		public void toggleSelectedItem(int selectedItem) {
			this.selectedItem = this.selectedItem != selectedItem && !this.indexIsOutsideAllowedBounds(selectedItem) ? selectedItem : -NO_SELECTED_ITEM_INDEX;
		}

		private boolean indexIsOutsideAllowedBounds(int selectedItem) {
			return selectedItem < 0 || selectedItem >= this.items.size();
		}

		@Nullable
		public ItemStack removeOne() {
			if (this.items.isEmpty()) return null;

			final int removeIndex = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
			final ItemStack stack = this.items.remove(removeIndex).copy();
			this.weight = this.weight.subtract(getWeight(stack).getOrThrow().multiplyBy(Fraction.getFraction(stack.getCount(), 1)));
			this.toggleSelectedItem(-NO_SELECTED_ITEM_INDEX);
			return stack;
		}

		public Fraction weight() {
			return this.weight;
		}

		public CameraContents toImmutable() {
			final ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
			for (ItemStack item : this.items) builder.add(ItemStackTemplate.fromNonEmptyStack(item));
			return new CameraContents(builder.build(), this.selectedItem);
		}
	}
}
