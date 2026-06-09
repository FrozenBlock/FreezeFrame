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

package net.frozenblock.freezeframe.mixin.client.scope;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.util.ScopeItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
		method = "shouldRenderBlockOutline",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/Options;hideGui:Z",
			opcode = Opcodes.GETFIELD
		),
		cancellable = true
	)
	private void freezeFrame$disableBlockOutlineWhileScoping(
		CallbackInfoReturnable<Boolean> info,
		@Local(name = "cameraEntity") Entity cameraEntity
	) {
		if (!(cameraEntity instanceof Player player) || !player.isScoping() || !this.minecraft.options.getCameraType().isFirstPerson()) return;
		if (!FFConfig.SCOPE_HIDES_BLOCK_OUTLINE.get() || !ScopeItemHelper.isPlayerUsingScopeItem(player)) return;
		info.setReturnValue(false);
	}
}
