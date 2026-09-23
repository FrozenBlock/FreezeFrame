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

import java.util.Set;
import net.frozenblock.freezeframe.client.scope.ScopePostEffectController;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ClientOnly
@Mixin(ShaderManager.class)
public class ShaderManagerMixin {

	@Inject(method = "isPostEffectValid", at = @At("HEAD"), cancellable = true)
	private void freezeFrame$allowDynamicFilterPostEffects(Identifier id, Set<Identifier> allowedTargets, CallbackInfoReturnable<Boolean> info) {
		if (ScopePostEffectController.isRegisteredDynamicEffect(ShaderManager.class.cast(this), id)) info.setReturnValue(true);
	}
}
