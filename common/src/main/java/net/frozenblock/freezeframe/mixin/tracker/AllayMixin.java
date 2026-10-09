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

package net.frozenblock.freezeframe.mixin.tracker;

import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Allay.class)
public abstract class AllayMixin {

	@Inject(method = "removeInteractionItem", at = @At("HEAD"))
	public void freezeFrame$incrementOnCreativeRemoveInteractionItem(Player player, ItemStack interactionItem, CallbackInfo info) {
		if (player == null || !player.hasInfiniteMaterials() || player.level().isClientSide() || !FFConfig.PHOTOGRAPH_TRACKER.get()) return;

		PhotographTracker.incrementOnItemStackSizeChange(player.level(), interactionItem.copyWithCount(1), 1);
		if (FFConstants.UNSTABLE_LOGGING) FFConstants.log("incrementOnCreativeRemoveInteractionItem - Allay", true);
	}
}
