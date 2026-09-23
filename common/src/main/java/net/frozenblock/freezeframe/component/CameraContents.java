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

package net.frozenblock.freezeframe.component;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.frozenblock.freezeframe.item.FilmItem;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.freezeframe.registry.FFItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ContainerComponent;
import net.minecraft.world.item.component.GrowableMutableContainer;
import net.minecraft.world.item.slot.SlotSelector;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public final class CameraContents implements ContainerComponent<CameraContents> {
	public static final CameraContents EMPTY = new CameraContents(List.of());
	public static final Codec<CameraContents> CODEC = ItemStackTemplate.CODEC.listOf().xmap(CameraContents::new, contents -> contents.items);
	public static final StreamCodec<RegistryFriendlyByteBuf, CameraContents> STREAM_CODEC = ItemStackTemplate.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(CameraContents::new, contents -> contents.items);
	private static final Fraction FILM_WEIGHT = Fraction.ONE;
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
		if (!item.is(FFItems.FILM.asHolder())) return DataResult.error(() -> "Item is not film");
		return DataResult.success(FILM_WEIGHT);
	}

	public static boolean canItemBeInCamera(ItemStack itemToAdd) {
		return !itemToAdd.isEmpty() && itemToAdd.is(FFItems.FILM.get());
	}

	public int getNumberOfItemsToShow() {
		final int numberOfItemStacks = this.size();
		final int availableItemsToShow = numberOfItemStacks > 12 ? 11 : 12;
		final int itemsOnNonFullRow = numberOfItemStacks % 4;
		final int emptySpaceOnNonFullRow = itemsOnNonFullRow == 0 ? 0 : 4 - itemsOnNonFullRow;
		return Math.min(numberOfItemStacks, availableItemsToShow - emptySpaceOnNonFullRow);
	}

	@Override
	public Stream<ItemStack> itemCopies() {
		return this.items.stream().map(ItemStackTemplate::create);
	}

	public List<ItemStackTemplate> items() {
		return this.items;
	}

	@Override
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
			final FilmContents filmContents = stack.get(FFDataComponents.FILM_CONTENTS.get());
			if (filmContents == null) continue;
			if (FilmItem.getWeightSafe(filmContents, stack).compareTo(Fraction.ONE) < 0) return true;
		}
		return false;
	}

	@Nullable
	public ItemStackTemplate getSelectedItem() {
		return this.selectedItem == NO_SELECTED_ITEM_INDEX ? null : this.items.get(this.selectedItem);
	}

	@Override
	public CameraContents copyWithContents(Stream<ItemStack> newContents) {
		final Mutable builder = new CameraContents.Mutable();
		newContents.forEach(builder::tryInsert);
		return builder.toImmutable();
	}

	@Override
	public Mutable asMutable() {
		final DataResult<Fraction> currentWeight = this.weight.get();
		if (currentWeight.isError()) return new Mutable();

		final List<ItemStack> itemsList = new ArrayList<>(this.items.size());
		for (ItemStackTemplate item : this.items) itemsList.add(item.create());

		return new Mutable(itemsList, currentWeight.getOrThrow(), this.selectedItem);
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

	public static final class Mutable extends GrowableMutableContainer<CameraContents> {
		private Fraction weight;
		private int selectedItem;
		private boolean needsFlattening;

		private Mutable(List<ItemStack> items, Fraction weight, int selectedItem) {
			super(items);
			this.weight = weight;
			this.selectedItem = selectedItem;
		}

		public Mutable() {
			this(new ArrayList<>(), Fraction.ZERO, -1);
		}

		public Mutable clearItems() {
			this.items.clear();
			this.weight = Fraction.ZERO;
			this.selectedItem = -NO_SELECTED_ITEM_INDEX;
			return this;
		}

		private int findStackIndexWithinRange(ItemStack itemsToAdd, int minInclusive, int maxExclusive) {
			if (!itemsToAdd.isStackable()) return NO_STACK_INDEX;

			final int startIndex = Math.max(minInclusive, 0);
			final int endIndex = Math.min(maxExclusive, this.items.size());
			for (int i = startIndex; i < endIndex; i++) {
				if (ItemStack.isSameItemSameComponents(this.items.get(i), itemsToAdd)) return i;
			}

			return NO_STACK_INDEX;
		}

		private int findStackIndex(ItemStack itemsToAdd) {
			return this.findStackIndexWithinRange(itemsToAdd, 0, this.items.size());
		}

		private int getMaxAmountToAdd(Fraction itemWeight) {
			final Fraction remainingWeight = Fraction.ONE.subtract(this.weight);
			return Math.max(remainingWeight.divideBy(itemWeight).intValue(), 0);
		}

		public int tryInsert(ItemStack itemsToAdd) {
			if (!canItemBeInCamera(itemsToAdd)) return 0;

			final DataResult<Fraction> maybeItemWeight = getWeight(itemsToAdd);
			if (maybeItemWeight.isError()) return 0;

			final Fraction itemWeight = maybeItemWeight.getOrThrow();
			final int amountToAdd = Math.min(itemsToAdd.getCount(), this.getMaxAmountToAdd(itemWeight));
			if (amountToAdd == 0) return 0;

			this.weight = this.weight.add(getStackedWeight(itemWeight, amountToAdd));
			int stackIndex = this.findStackIndex(itemsToAdd);
			if (stackIndex != NO_STACK_INDEX) {
				final ItemStack removedStack = this.items.remove(stackIndex);
				final ItemStack mergedStack = removedStack.copyWithCount(removedStack.getCount() + amountToAdd);
				itemsToAdd.shrink(amountToAdd);
				this.items.addFirst(mergedStack);
			} else {
				this.items.addFirst(itemsToAdd.split(amountToAdd));
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
				final FilmContents filmContents = stack.get(FFDataComponents.FILM_CONTENTS.get());
				if (filmContents == null) continue;
				if (FilmItem.getWeightSafe(filmContents, stack).compareTo(Fraction.ONE) < 0) return Optional.of(stack);
			}
			return Optional.empty();
		}

		public void toggleSelectedItem(int selectedItem) {
			this.selectedItem = this.selectedItem != selectedItem && !this.indexIsOutsideAllowedBounds(selectedItem) ? selectedItem : NO_SELECTED_ITEM_INDEX;
		}

		private boolean indexIsOutsideAllowedBounds(int selectedItem) {
			return selectedItem < 0 || selectedItem >= this.items.size();
		}

		public @Nullable ItemStack removeOne() {
			if (this.items.isEmpty()) return null;

			final int removeIndex = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
			final ItemStack stack = this.items.remove(removeIndex).copy();
			this.weight = this.weight.subtract(getStackedWeight(stack));
			this.toggleSelectedItem(NO_SELECTED_ITEM_INDEX);
			return stack;
		}

		private static Fraction getStackedWeight(Fraction weight, int count) {
			return weight.multiplyBy(Fraction.getFraction(count, 1));
		}

		private static Fraction getStackedWeight(ItemStack stack) {
			return getStackedWeight(getWeight(stack).getOrThrow(), stack.getCount());
		}

		public Fraction weight() {
			return this.weight;
		}

		@Override
		public int replaceSlotItems(ItemProvider newItems, SlotSelector slotSelector) {
			this.mergeIdenticalStacks();
			return super.replaceSlotItems(newItems, slotSelector);
		}

		@Override
		public void modifySlots(Consumer<? super SlotAccess> consumer, SlotSelector slotSelector) {
			this.mergeIdenticalStacks();
			super.modifySlots(consumer, slotSelector);
		}

		@Override
		protected boolean setItem(int slot, ItemStack itemStack) {
			final ItemStack currentItem = this.items.get(slot);
			final Fraction adjustedWeight = currentItem.isEmpty() ? this.weight : this.weight.subtract(getStackedWeight(currentItem));
			final Fraction newWeight = itemStack.isEmpty() ? adjustedWeight : getWeightWithAddedItems(adjustedWeight, itemStack);

			if (newWeight != null && super.setItem(slot, itemStack)) {
				this.weight = newWeight;
				this.needsFlattening = true;
				return true;
			}

			return false;
		}

		@Override
		protected boolean addSlotWithItem(ItemProvider newItems) {
			if (!newItems.findNextNonEmpty()) return false;

			final Fraction newWeight = getWeightWithAddedItems(this.weight, newItems.peek());
			if (newWeight != null && super.addSlotWithItem(newItems)) {
				this.weight = newWeight;
				this.needsFlattening = true;
				return true;
			}

			return false;
		}

		private static @Nullable Fraction getWeightWithAddedItems(Fraction weight, ItemStack itemsToAdd) {
			if (!canItemBeInCamera(itemsToAdd)) return null;

			final DataResult<Fraction> itemWeight = getWeight(itemsToAdd);
			if (itemWeight.isError()) return null;

			final Fraction newWeight = weight.add(getStackedWeight(itemWeight.getOrThrow(), itemsToAdd.getCount()));
			if (newWeight.compareTo(Fraction.ONE) <= 0) return newWeight;

			return null;
		}

		@Override
		public boolean canInsertNewSlots() {
			return this.weight.compareTo(Fraction.ONE) < 0;
		}

		private void mergeIdenticalStacks() {
			if (!this.needsFlattening) return;

			for (int index = 0; index < this.items.size(); index++) {
				final ItemStack itemStack = this.items.get(index);
				if (itemStack.isEmpty()) {
					this.items.remove(index--);
					continue;
				}

				int stackIndex = this.findStackIndexWithinRange(itemStack, index + 1, this.items.size());
				if (stackIndex == -1) continue;

				final ItemStack targetStack = this.items.get(stackIndex);
				int mergedCount = itemStack.getCount() + targetStack.getCount();
				this.items.set(stackIndex, itemStack.copyWithCount(mergedCount));
				this.items.remove(index--);
			}

			this.needsFlattening = false;
		}

		@Override
		public CameraContents toImmutable() {
			this.mergeIdenticalStacks();
			final ImmutableList.Builder<ItemStackTemplate> builder = ImmutableList.builder();
			for (ItemStack item : this.items) builder.add(ItemStackTemplate.fromNonEmptyStack(item));
			return new CameraContents(builder.build(), this.selectedItem);
		}
	}
}
