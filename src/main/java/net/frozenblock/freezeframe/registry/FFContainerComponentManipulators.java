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

package net.frozenblock.freezeframe.registry;

import java.util.stream.Stream;
import net.frozenblock.freezeframe.component.CameraContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;

public class FFContainerComponentManipulators {
	public static final ContainerComponentManipulator<CameraContents> CAMERA_CONTENTS = new ContainerComponentManipulator<>() {
		@Override
		public DataComponentType<CameraContents> type() {
			return FFDataComponents.CAMERA_CONTENTS;
		}

		public CameraContents empty() {
			return CameraContents.EMPTY;
		}

		public Stream<ItemStack> getContents(CameraContents component) {
			return component.itemCopyStream();
		}

		public CameraContents setContents(CameraContents component, Stream<ItemStack> newContents) {
			final CameraContents.Mutable builder = new CameraContents.Mutable(component).clearItems();
			newContents.forEach(builder::tryInsert);
			return builder.toImmutable();
		}
	};

	public static void init() {
	}
}
