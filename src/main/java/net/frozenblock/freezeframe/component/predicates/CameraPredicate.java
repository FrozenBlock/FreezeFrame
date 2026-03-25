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

package net.frozenblock.freezeframe.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.frozenblock.freezeframe.component.CameraContents;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemInstance;

public record CameraPredicate(Optional<CollectionPredicate<ItemInstance, ItemPredicate>> items) implements SingleComponentItemPredicate<CameraContents> {
	public static final Codec<CameraPredicate> CODEC = RecordCodecBuilder.create(
		i -> i.group(
			CollectionPredicate.codec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(CameraPredicate::items)
		).apply(i, CameraPredicate::new)
	);

	@Override
	public DataComponentType<CameraContents> componentType() {
		return FFDataComponents.CAMERA_CONTENTS;
	}

	public boolean matches(CameraContents value) {
		return !this.items.isPresent() || this.items.get().test(value.items());
	}
}
