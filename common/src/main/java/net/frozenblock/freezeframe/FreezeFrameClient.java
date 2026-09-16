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

package net.frozenblock.freezeframe;

import com.mojang.blaze3d.platform.InputConstants;
import net.frozenblock.freezeframe.client.FFModelLayers;
import net.frozenblock.freezeframe.client.scope.ScopeAndCameraUseController;
import net.frozenblock.freezeframe.component.tooltip.CameraTooltip;
import net.frozenblock.freezeframe.component.tooltip.FilmTooltip;
import net.frozenblock.freezeframe.component.tooltip.PhotographTooltip;
import net.frozenblock.freezeframe.component.tooltip.client.ClientCameraTooltip;
import net.frozenblock.freezeframe.component.tooltip.client.ClientFilmTooltip;
import net.frozenblock.freezeframe.component.tooltip.client.ClientPhotographTooltip;
import net.frozenblock.freezeframe.registry.FFMenuScreens;
import net.frozenblock.lib.event.api.events.client.ClientTooltipComponentCallback;
import net.frozenblock.lib.platform.client.KeyMappingRegistry;
import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.client.KeyMapping;

@ClientOnly
public final class FreezeFrameClient {
	public static final KeyMapping RESET_SCOPE_ZOOM = new KeyMapping("key.resetScopeZoom", InputConstants.Type.MOUSE, 2, KeyMapping.Category.GAMEPLAY, 1);

	public static void init() {
		FFModelLayers.init();
		ScopeAndCameraUseController.init();

		KeyMappingRegistry.register(RESET_SCOPE_ZOOM);
		ClientTooltipComponentCallback.EVENT.register(component -> {
			if (component instanceof PhotographTooltip tooltip) return new ClientPhotographTooltip(tooltip);
			if (component instanceof FilmTooltip tooltip) return new ClientFilmTooltip(tooltip.contents(), tooltip.maxPhotographs());
			if (component instanceof CameraTooltip tooltip) return new ClientCameraTooltip(tooltip.contents());
			return null;
		});
	}

	public static void setup() {
		FFModelLayers.setup();
		FFMenuScreens.setup();
	}

	private FreezeFrameClient() {}
}
