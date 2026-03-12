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
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public final class FilmContents {
	public static final FilmContents EMPTY = new FilmContents(List.of());
	private static final int MAX_PHOTOGRAPHS = 16;
	private static final Fraction PHOTOGRAPH_WEIGHT = Fraction.getFraction(1, MAX_PHOTOGRAPHS);
	public static final Codec<FilmContents> CODEC = PhotographComponent.CODEC.listOf().xmap(FilmContents::new, contents -> contents.photographs);
	public static final StreamCodec<ByteBuf, FilmContents> STREAM_CODEC = PhotographComponent.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(FilmContents::new, contents -> contents.photographs);
	private final List<PhotographComponent> photographs;
	private final int selectedPhotograph;
	private final Supplier<DataResult<Fraction>> weight;

	private FilmContents(final List<PhotographComponent> photographs, int selectedPhotograph) {
		this.photographs = photographs;
		this.selectedPhotograph = selectedPhotograph;
		this.weight = Suppliers.memoize(() -> computeContentWeight(this.photographs));
	}

	public FilmContents(final List<PhotographComponent> photographs) {
		this(photographs, 0);
	}

	private static DataResult<Fraction> computeContentWeight(final List<PhotographComponent> items) {
		final Fraction weight = Fraction.getFraction(items.size(), MAX_PHOTOGRAPHS);
		if (weight.compareTo(Fraction.ONE) <= 0) return DataResult.success(weight);
		return DataResult.error(() -> "Excessive total film weight");
	}

	public List<PhotographComponent> photographs() {
		return this.photographs;
	}

	public int size() {
		return this.photographs.size();
	}

	public DataResult<Fraction> weight() {
		return this.weight.get();
	}

	public boolean isEmpty() {
		return this.photographs.isEmpty();
	}

	public int getSelectedPhotographIndex() {
		return this.selectedPhotograph;
	}

	@Nullable
	public PhotographComponent getSelectedPhotograph() {
		return this.photographs.get(this.selectedPhotograph);
	}

	@Nullable
	public PhotographComponent getPhotographAtIndex(int index) {
		if (index < 0 || index >= this.size()) return null;
		return this.photographs.get(index);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return obj instanceof FilmContents contents && this.photographs.equals(contents.photographs);
	}

	@Override
	public int hashCode() {
		return this.photographs.hashCode();
	}

	@Override
	public String toString() {
		return "FilmContents" + this.photographs;
	}

	public static class Mutable {
		private final List<PhotographComponent> photographs;
		private Fraction weight;
		private int selectedPhotograph;

		public Mutable(FilmContents contents) {
			final DataResult<Fraction> currentWeight = contents.weight.get();
			if (currentWeight.isError()) {
				this.photographs = new ArrayList<>();
				this.weight = Fraction.ZERO;
				this.selectedPhotograph = 0;
				return;
			}

			this.photographs = new ArrayList<>(contents.photographs.size());
			this.photographs.addAll(contents.photographs);
			this.weight = currentWeight.getOrThrow();
			this.selectedPhotograph = contents.selectedPhotograph;
		}

		private boolean hasRemainingSpace() {
			return this.weight.compareTo(Fraction.ONE) < 0;
		}

		public boolean tryInsert(PhotographComponent photograph) {
			if (!this.hasRemainingSpace()) return false;
			this.weight = this.weight.add(PHOTOGRAPH_WEIGHT);
			this.photographs.addFirst(photograph);
			return true;
		}

		public void toggleSelectedPhotograph(final int selectedPhotograph) {
			this.selectedPhotograph = this.selectedPhotograph != selectedPhotograph && !this.indexIsOutsideAllowedBounds(selectedPhotograph)
				? selectedPhotograph
				: 0;
		}

		private boolean indexIsOutsideAllowedBounds(int selectedPhotograph) {
			return selectedPhotograph < 0 || selectedPhotograph >= this.photographs.size();
		}

		public Fraction weight() {
			return this.weight;
		}

		public FilmContents toImmutable() {
			final ImmutableList.Builder<PhotographComponent> builder = ImmutableList.builder();
			for (PhotographComponent photograph : this.photographs) builder.add(photograph);
			return new FilmContents(builder.build(), this.selectedPhotograph);
		}
	}
}
