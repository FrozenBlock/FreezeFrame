package net.lunade.camera;

import net.fabricmc.api.ModInitializer;
import net.lunade.camera.networking.CameraPortNetworking;
import net.lunade.camera.registry.CameraPortBlocks;
import net.lunade.camera.registry.CameraPortContainerComponentManipulators;
import net.lunade.camera.registry.CameraPortDataComponentPredicates;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.lunade.camera.registry.CameraPortEntityTypes;
import net.lunade.camera.registry.CameraPortItems;
import net.lunade.camera.registry.CameraPortMenuTypes;
import net.lunade.camera.registry.CameraPortSounds;

public class CameraPortMain implements ModInitializer {


	@Override
	public void onInitialize() {
		CameraPortEntityTypes.init();
		CameraPortBlocks.register();
		CameraPortItems.init();
		CameraPortContainerComponentManipulators.init();
		CameraPortDataComponentPredicates.init();
		CameraPortSounds.init();
		CameraPortDataComponents.init();
		CameraPortMenuTypes.register();

		CameraPortNetworking.init();
	}
}
