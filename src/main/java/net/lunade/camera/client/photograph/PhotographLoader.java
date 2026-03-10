package net.lunade.camera.client.photograph;

import com.mojang.datafixers.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.frozenblock.lib.texture.client.api.ServerTextureDownloader;
import net.lunade.camera.CameraPortConstants;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class PhotographLoader {
	private static final Identifier FALLBACK = CameraPortConstants.id("textures/photographs/empty.png");
	private static final ArrayList<Pair<Identifier, Date>> LOCAL_PHOTOGRAPHS = new ArrayList<>();

	public static boolean hasAnyLocalPhotographs() {
		return !LOCAL_PHOTOGRAPHS.isEmpty();
	}

	public static Identifier getPhotograph(int index) {
		return LOCAL_PHOTOGRAPHS.get(index).getFirst();
	}

	public static Identifier getAndLoadPhotograph(String photographName, boolean local) {
		return getAndLoadPhotograph(getPhotographLocation(photographName), local);
	}

	public static Identifier getAndLoadPhotograph(Identifier photographId, boolean local) {
		String filename = photographId.getPath().replace("photographs/", "");
		if (!filename.endsWith(".png")) filename += ".png";

		Identifier downloaderId = ServerTextureDownloader.getOrLoadServerTexture(
			photographId,
			"photographs",
			filename,
			FALLBACK
		);
		if (local) return photographId;
		return downloaderId;
	}

	public static int getSize() {
		return LOCAL_PHOTOGRAPHS.size();
	}

	@Nullable
	public static Identifier getInfiniteLocalPhotograph(int index) {
		if (LOCAL_PHOTOGRAPHS.isEmpty()) return null;
		int size = LOCAL_PHOTOGRAPHS.size();
		int adjustedIndex = ((index % size) + size) % size;
		return getPhotograph(adjustedIndex);
	}

	private static Identifier getPhotographLocation(String name) {
		return CameraPortConstants.id("photographs/" + name);
	}

	public static int loadLocalPhotographs() {
		final File file = FabricLoader.getInstance().getGameDir().resolve("photographs").resolve(ServerTextureDownloader.LOCAL_TEXTURE_SOURCE).toFile();
		final File[] fileList = file.listFiles();
		if (fileList == null) return 0;

		final Stream<File> fileStream = Arrays.stream(fileList)
			.filter(File::isFile)
			.filter(file1 -> file1.getName().endsWith(".png"));
		LOCAL_PHOTOGRAPHS.clear();

		ArrayList<Pair<Identifier, Date>> localPhotographs = new ArrayList<>();
		for (String name : fileStream.map(File::getName).toList()) {
			String strippedFileName = name.replace(".png", "");
			parseDate(strippedFileName).ifPresent(date -> {
				localPhotographs.add(Pair.of(PhotographLoader.getAndLoadPhotograph(strippedFileName, true), date));
			});
		}

		localPhotographs.stream().sorted(Comparator.comparing(Pair::getSecond)).forEach(LOCAL_PHOTOGRAPHS::add);
		return LOCAL_PHOTOGRAPHS.size();
	}

	public static Optional<Date> parseDate(String fileName) {
		int lastIndex = fileName.lastIndexOf(".png");
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
