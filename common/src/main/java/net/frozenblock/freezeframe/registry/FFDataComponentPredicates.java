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

package net.frozenblock.freezeframe.registry;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.predicates.CameraPredicate;
import net.frozenblock.lib.platform.api.registry.DeferredHolder;
import net.frozenblock.lib.platform.api.registry.DeferredRegister;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;

public final class FFDataComponentPredicates {
	private static final DeferredRegister<DataComponentPredicate.Type<?>> REGISTER = DeferredRegister.create(
		Registries.DATA_COMPONENT_PREDICATE_TYPE,
		FFConstants.MOD_ID
	);

	public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<CameraPredicate>> CAMERA_CONTENTS = REGISTER.register(
		"camera_contents",
		() -> new DataComponentPredicate.ConcreteType<>(CameraPredicate.CODEC)
	);

	static {
		REGISTER.register();
	}

	public static void init() {}

	private FFDataComponentPredicates() {}
}
