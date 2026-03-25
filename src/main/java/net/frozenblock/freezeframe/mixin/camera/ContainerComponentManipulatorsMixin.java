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

package net.frozenblock.freezeframe.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.stream.Stream;
import net.frozenblock.freezeframe.registry.FFContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerComponentManipulators.class)
public class ContainerComponentManipulatorsMixin {

	@ModifyExpressionValue(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/stream/Stream;of([Ljava/lang/Object;)Ljava/util/stream/Stream;"
		)
	)
	private static Stream freezeFrame$addCameraContainerComponentManipulator(Stream original) {
		final Stream.Builder newStream = Stream.builder();
		original.forEach(object -> newStream.add(object));
		newStream.add(FFContainerComponentManipulators.CAMERA_CONTENTS);
		return newStream.build();
	}

}
