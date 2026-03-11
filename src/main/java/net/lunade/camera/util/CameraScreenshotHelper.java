package net.lunade.camera.util;

import net.minecraft.world.entity.player.Player;
import java.io.File;

public class CameraScreenshotHelper {

	public static String makeFileName(Player player) {
		return player.getStringUUID() + "_" + System.currentTimeMillis();
	}

	public static File getPhotographFile(String playerUUID, long time, File directory) {
		String fileName = playerUUID + "_" + time;
		return getPhotographFile(fileName, directory);
	}

	public static File getPhotographFile(String fileName, File directory) {
		int fileIndex = 1;
		while (true) {
			File file = new File(directory, fileName + (fileIndex == 1 ? "" : "_" + fileIndex) + ".png");
			if (!file.exists()) return file;
			++fileIndex;
		}
	}
}
