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

import net.frozenblock.freezeframe.networking.FFClientNetworking;
import net.frozenblock.freezeframe.networking.FFNetworking;
import net.frozenblock.lib.networking.api.platform.NetworkingHelperImpl;
import net.frozenblock.lib.platform.ModLoader;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(FFConstants.MOD_ID)
public final class FreezeFrameNeoForge {

	public FreezeFrameNeoForge(IEventBus modBus) {
		FreezeFrame.init();

		modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			FFNetworking.setup();

			if (ModLoader.isClient()) FFClientNetworking.setup();

			final PayloadRegistrar registrar = event.registrar(FFConstants.MOD_ID);
			NetworkingHelperImpl.flush(registrar);
		});

		// AFTER register event
		modBus.addListener(FMLCommonSetupEvent.class, event -> {
			FreezeFrame.setup();
		});
	}
}
