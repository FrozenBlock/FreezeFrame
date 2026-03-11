package net.lunade.camera.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.client.gui.screens.inventory.PrinterScreen;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class CameraPortScreens {

	public static void init() {
		MenuScreens.register(CameraPortMenuTypes.PRINTER, PrinterScreen::new);
	}
}
