package net.frozenblock.freezeframe.item.photograph;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public record PhotographTracker(Map<String, Integer> photographCounts) {
	private static final PhotographTracker EMPTY = new PhotographTracker(Map.of());
	public static final Codec<PhotographTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("photograph_counts").forGetter(PhotographTracker::photographCounts)
	).apply(instance, PhotographTracker::new));

	public static PhotographTracker get(Level level) {
		return level.getServer().overworld().getAttachedOrElse(FFAttachmentTypes.PHOTOGRAPH_TRACKER, EMPTY);
	}

	public static PhotographTracker setAttached(Level level, PhotographTracker tracker) {
		return level.getServer().overworld().setAttached(FFAttachmentTypes.PHOTOGRAPH_TRACKER, tracker);
	}

	public static void incrementPhotographCountAndDeleteIfEmpty(Level level, String photographName, int step) {
		if (!(level instanceof ServerLevel)) return;

		final PhotographTracker initialTracker = get(level);

		final Mutable tracker = initialTracker.mutable();
		tracker.incrementPhotographCount(photographName, step);
		final PhotographTracker finalTracker = tracker.toImmutable();
		setAttached(level, tracker.toImmutable());

		initialTracker.photographCounts.keySet().stream()
			.filter(key -> !finalTracker.photographCounts.containsKey(key))
			.map(name -> getPossiblePhotographPaths(level, name))
			.forEach(paths -> {
				paths.forEach(path -> {
					try {
						if (Files.deleteIfExists(path)) {
							FFConstants.log("Deleted photograph file " + path, FFConstants.UNSTABLE_LOGGING);
						} else {
							FFConstants.log("Failed to delete photograph file " + path, FFConstants.UNSTABLE_LOGGING);
						}
					} catch (Exception e) {
						FFConstants.log("Failed to delete photograph file DUE TO CRASH" + path, FFConstants.UNSTABLE_LOGGING);
					}
				});
			});

		System.out.println("Incremented photograph count for " + photographName + " by " + step);
		System.out.println("Current counts: " + tracker.photographCounts);
	}

	private static List<Path> getPossiblePhotographPaths(Level level, String photographName) {
		final Path photographsDirectory = level.getServer().getServerDirectory().resolve("photographs");
		if (Files.notExists(photographsDirectory)) {
			FFConstants.log("Photographs directory does not exist", FFConstants.UNSTABLE_LOGGING);
			return List.of();
		}

		final List<Path> paths = new ArrayList<>();
		for (String extension : Arrays.stream(FFConfig.PhotographFormat.values()).map(FFConfig.PhotographFormat::extension).distinct().toArray(String[]::new)) {
			final String fileNameWithExtension = photographName + "." + extension;
			for (boolean local : Arrays.asList(false, true)) {
				final Path finalPath = local ? photographsDirectory.resolve(FileTransferPacket.LOCAL_SOURCE) : photographsDirectory;
				paths.add(finalPath.resolve(fileNameWithExtension));
			}
		}
		return paths;
	}

	public static void incrementOnItemStackDeletion(Level level, ItemStack stack, boolean allowRecursion) {
		incrementOnItemStackSizeChange(level, stack, -stack.getCount(), allowRecursion);
	}

	public static void incrementOnItemStackDeletion(Level level, ItemStack stack) {
		incrementOnItemStackDeletion(level, stack, true);
	}

	public static void incrementOnItemStackClone(Level level, ItemStack stack, boolean allowRecursion) {
		incrementOnItemStackSizeChange(level, stack, stack.getCount(), allowRecursion);
	}

	public static void incrementOnItemStackClone(Level level, ItemStack stack) {
		incrementOnItemStackClone(level, stack, true);
	}

	public static void incrementOnItemStackSizeChange(Level level, ItemStack stack, int delta, boolean allowRecursion) {
		final Photograph photograph = stack.get(FFDataComponents.PHOTOGRAPH);
		if (photograph != null) incrementPhotographCountAndDeleteIfEmpty(level, photograph.identifier().getPath(), delta);

		final FilmContents filmContents = stack.getOrDefault(FFDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		filmContents.photographs().forEach(filmPhotograph -> incrementPhotographCountAndDeleteIfEmpty(level, filmPhotograph.identifier().getPath(), delta));

		if (!allowRecursion) return;
		ContainerComponentManipulators.ALL_MANIPULATORS.forEach((type, manipulator) -> {
			manipulator.getSlots(stack).itemCopies().forEach(nested -> incrementOnItemStackSizeChange(level, nested, delta));
		});
	}

	public static void incrementOnItemStackSizeChange(Level level, ItemStack stack, int delta) {
		incrementOnItemStackSizeChange(level, stack, delta, true);
	}

	public Mutable mutable() {
		return new Mutable(this);
	}

	public static class Mutable {
		private final Map<String, Integer> photographCounts;

		public Mutable(PhotographTracker tracker) {
			this.photographCounts = new HashMap<>(tracker.photographCounts);
		}

		public void incrementPhotographCount(String photographName, int step) {
			if (step == 0 || (step < 0 && !this.photographCounts.containsKey(photographName))) return;
			this.photographCounts.merge(photographName, step, Integer::sum);
		}

		public PhotographTracker toImmutable() {
			final Map<String, Integer> cleanedMap = new HashMap<>(this.photographCounts);
			cleanedMap.entrySet().removeIf(entry -> entry.getValue() <= 0);
			return new PhotographTracker(ImmutableMap.copyOf(cleanedMap));
		}
	}
}
