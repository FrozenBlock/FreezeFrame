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
