package net.frozenblock.freezeframe;

import net.frozenblock.freezeframe.config.gui.FFConfigGui;
import net.frozenblock.lib.FrozenBools;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = FFConstants.MOD_ID, dist = Dist.CLIENT)
public final class FreezeFrameNeoForgeClient {

	public FreezeFrameNeoForgeClient(IEventBus modBus) {
		FreezeFrameClient.init();

		// AFTER register event
		modBus.addListener(FMLClientSetupEvent.class, event -> {
			FreezeFrameClient.setup();
		});

		if (FrozenBools.HAS_CLOTH_CONFIG) {
			ModLoadingContext.get().registerExtensionPoint(
				IConfigScreenFactory.class,
				() -> (container, parent) -> FFConfigGui.buildScreen(parent)
			);
		}
	}
}
