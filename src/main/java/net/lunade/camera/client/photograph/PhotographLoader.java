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

package net.lunade.camera.client.photograph;

import java.util.Date;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.lib.texture.client.api.ServerTextureDownloader;
import net.lunade.camera.CameraPortConstants;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class PhotographLoader {
	private static final Identifier FALLBACK = CameraPortConstants.id("textures/photographs/empty.png");

	public static Identifier getAndLoadPhotograph(Identifier photographId) {
		final String filename = photographId.getPath().replace("photographs/", "");
		return ServerTextureDownloader.getOrLoadServerTexture(
			photographId,
			"photographs",
			filename,
			FALLBACK
		);
	}

	public static Optional<Date> parseDate(String fileName) {
		int lastIndex = fileName.lastIndexOf(".");
		lastIndex = lastIndex == -1 ? fileName.length() : lastIndex;
		String strippedFileName = fileName.substring(Math.max(fileName.lastIndexOf("/"), 0), lastIndex);
		try {
			int firstUnderScoreIndex = strippedFileName.indexOf("_");
			int lastUnderscoreIndex = strippedFileName.lastIndexOf("_");
			lastUnderscoreIndex = lastUnderscoreIndex == -1 || lastUnderscoreIndex == firstUnderScoreIndex
				? strippedFileName.length() : lastUnderscoreIndex;

			String unixString = strippedFileName.substring(firstUnderScoreIndex + 1, lastUnderscoreIndex);
			long unixTime = Long.parseLong(unixString);
			return Optional.of(new Date(unixTime));
		} catch (Exception ignored) {
		}
		return Optional.empty();
	}
}
