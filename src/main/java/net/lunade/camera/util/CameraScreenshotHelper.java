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
