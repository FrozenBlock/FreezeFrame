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

package net.lunade.camera.mixin.client.photograph;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.component.tooltip.CameraTooltip;
import net.lunade.camera.component.tooltip.FilmTooltip;
import net.lunade.camera.component.tooltip.PhotographTooltip;
import net.lunade.camera.component.tooltip.client.ClientCameraTooltip;
import net.lunade.camera.component.tooltip.client.ClientFilmTooltip;
import net.lunade.camera.component.tooltip.client.ClientPhotographTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {

	@Inject(
		at = @At("HEAD"),
		method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
		cancellable = true
	)
	private static void cameraPort$create(TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> info) {
		if (component instanceof PhotographTooltip tooltip) info.setReturnValue(new ClientPhotographTooltip(tooltip));
		if (component instanceof FilmTooltip tooltip) info.setReturnValue(new ClientFilmTooltip(tooltip.contents()));
		if (component instanceof CameraTooltip tooltip) info.setReturnValue(new ClientCameraTooltip(tooltip.contents()));
	}
}
