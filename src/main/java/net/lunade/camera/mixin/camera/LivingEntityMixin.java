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

package net.lunade.camera.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.lunade.camera.util.ScopeItemHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class LivingEntityMixin {

	@ModifyReturnValue(method = "isScoping", at = @At("RETURN"))
	private boolean cameraPort$enableScopingForCamera(boolean original) {
		if (original) return true;

		return ScopeItemHelper.isPlayerUsingCamera((Player) (Object) this);
	}
}
