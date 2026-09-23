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

package net.frozenblock.freezeframe.item.photograph;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.networking.packet.DeletePhotographPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.lib.event.api.events.ServerEntityLevelChangeEvents;
import net.frozenblock.lib.event.api.events.ServerLivingEntityEvents;
import net.frozenblock.lib.event.api.events.ServerPlayerEvents;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.frozenblock.lib.networking.api.NetworkingHelper;
import net.frozenblock.lib.networking.api.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public record PhotographTracker(Map<String, Integer> photographCounts, Map<String, Long> deletedPhotographs) {
	private static final boolean LOG_DELETIONS = true;
	private static final boolean LOG_FAILED_DELETION_ATTEMPTS = false;
	private static final boolean LOG_INCREMENTS = true;
	private static final PhotographTracker EMPTY = new PhotographTracker(Map.of(), Map.of());
	public static final Codec<PhotographTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("photograph_counts").forGetter(PhotographTracker::photographCounts),
		Codec.withAlternative(
			Codec.unboundedMap(Codec.STRING, Codec.LONG),
			Codec.STRING.listOf(),
			strings -> {
				final Map<String, Long> newDeletedPhotographs = new Object2LongOpenHashMap<>();
				for (String name : strings) newDeletedPhotographs.put(name, 0L);
				return newDeletedPhotographs;
			}
		).fieldOf("deleted_photographs").forGetter(PhotographTracker::deletedPhotographs)
	).apply(instance, PhotographTracker::new));

	public static void init() {
		ServerPlayerEvents.JOIN.register(((server, player) -> {
			notifyOfAllDeletedPhotographs(player);
		}));

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> removeCreativeModeCarriedItem(entity));
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> removeCreativeModeCarriedItem(player));
		ServerPlayerEvents.LEAVE.register(((server, player) -> removeCreativeModeCarriedItem(player)));
	}

	public static void notifyOfAllDeletedPhotographs(ServerPlayer player) {
		final PhotographTracker tracker = get(player.server);
		if (tracker.deletedPhotographs.isEmpty()) return;
		if (NetworkingHelper.isLocalPlayer(player)) return;

		DeletePhotographPacket.sendDeletedPhotographsAndHandleTimestamp(player, player.server.overworld().getGameTime(), tracker.deletedPhotographs);
	}

	public static void notifyAllOfDeletedPhotographs(MinecraftServer server, List<String> photographNames) {
		final long gameTime = server.overworld().getGameTime();

		for (ServerPlayer player : PlayerLookup.all(server)) {
			FFAttachmentTypes.PHOTOGRAPH_TRACKER_LAST_SYNC_TIMESTAMP.set(player, gameTime);

			if (NetworkingHelper.isLocalPlayer(player)) continue;
			NetworkingHelper.sendToPlayer(player, new DeletePhotographPacket(photographNames));
		}
	}

	public static void removeCreativeModeCarriedItem(Entity entity) {
		if (!(entity instanceof ServerPlayer player)) return;
		final ItemStack carriedAttachment = FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.getAttachedOrElse(player, ItemStack.EMPTY);
		if (!carriedAttachment.isEmpty()) incrementOnItemStackDeletion(player.level(), carriedAttachment);
		FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM.remove(player);
	}

	public static PhotographTracker get(MinecraftServer server) {
		return FFAttachmentTypes.PHOTOGRAPH_TRACKER.getAttachedOrElse(server.overworld(), EMPTY);
	}

	public static void setAttached(MinecraftServer server, PhotographTracker tracker) {
		FFAttachmentTypes.PHOTOGRAPH_TRACKER.set(server.overworld(), tracker);
	}

	public static void setAttached(Level level, PhotographTracker tracker) {
		setAttached(level.getServer(), tracker);
	}

	public static void incrementPhotographCountAndDeleteIfEmpty(Level level, String photographName, int step) {
		if (!(level instanceof ServerLevel serverLevel)) return;

		final MinecraftServer server = serverLevel.getServer();

		final PhotographTracker initialTracker = get(server);
		final int oldCount = initialTracker.photographCounts.getOrDefault(photographName, 0);

		final Mutable tracker = initialTracker.mutable();
		tracker.incrementPhotographCount(photographName, step);
		final PhotographTracker finalTracker = tracker.toImmutable(server);
		setAttached(level, tracker.toImmutable(server));

		final List<String> newlyDeletedPhotographs = initialTracker.photographCounts.keySet().stream()
			.filter(key -> !finalTracker.photographCounts.containsKey(key))
			.toList();

		deletePhotographs(server.getServerDirectory(), newlyDeletedPhotographs);
		notifyAllOfDeletedPhotographs(server, newlyDeletedPhotographs);

		FFConstants.log(
			"Incremented " + photographName + " by " + step + ": " + oldCount + " -> " +  finalTracker.photographCounts.getOrDefault(photographName, 0),
			LOG_INCREMENTS && FFConstants.UNSTABLE_LOGGING
		);
	}

	public static void deletePhotographs(Path gameDirectory, List<String> deletedPhotographs) {
		deletedPhotographs.stream()
			.map(name -> getPossiblePhotographPaths(gameDirectory, name))
			.forEach(paths -> {
				paths.forEach(path -> {
					try {
						if (Files.deleteIfExists(path)) {
							FFConstants.log("Deleted photograph file " + path, LOG_DELETIONS && FFConstants.UNSTABLE_LOGGING);
						} else {
							FFConstants.log("Failed to delete photograph file " + path, LOG_FAILED_DELETION_ATTEMPTS && FFConstants.UNSTABLE_LOGGING);
						}
					} catch (Exception e) {
						FFConstants.log("Failed to delete photograph file " + path, FFConstants.UNSTABLE_LOGGING);
					}
				});
			});
	}

	private static List<Path> getPossiblePhotographPaths(Path gameDirectory, String photographName) {
		final Path photographsDirectory = gameDirectory.resolve("photographs");
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
		final Photograph photograph = stack.get(FFDataComponents.PHOTOGRAPH.get());
		if (photograph != null) incrementPhotographCountAndDeleteIfEmpty(level, photograph.identifier().getPath(), delta);

		final FilmContents filmContents = stack.getOrDefault(FFDataComponents.FILM_CONTENTS.get(), FilmContents.EMPTY);
		filmContents.photographs().forEach(filmPhotograph -> incrementPhotographCountAndDeleteIfEmpty(level, filmPhotograph.identifier().getPath(), delta));

		if (!allowRecursion) return;
		ContainerComponentManipulators.ALL_MANIPULATORS.forEach((type, manipulator) -> {
			manipulator.getSlots(stack).itemCopies().forEach(nested -> incrementOnItemStackSizeChange(level, nested, delta));
		});
	}

	public static void incrementOnItemStackSizeChange(Level level, ItemStack stack, int delta) {
		incrementOnItemStackSizeChange(level, stack, delta, true);
	}

	public static ItemStack stripAllPhotographComponents(ItemStack stack) {
		stack.remove(FFDataComponents.PHOTOGRAPH.get());

		final FilmContents initialFilmContents = stack.getOrDefault(FFDataComponents.FILM_CONTENTS.get(), FilmContents.EMPTY);
		if (!initialFilmContents.isEmpty()) {
			final FilmContents.Mutable filmContents = new FilmContents.Mutable(initialFilmContents);
			filmContents.removeAllPhotographs();
			stack.set(FFDataComponents.FILM_CONTENTS.get(), filmContents.toImmutable());
		}

		ContainerComponentManipulators.ALL_MANIPULATORS.forEach((type, manipulator) -> {
			manipulator.modifyItems(stack, PhotographTracker::stripAllPhotographComponents);
		});

		return stack;
	}

	public Mutable mutable() {
		return new Mutable(this);
	}

	public static class Mutable {
		private final Map<String, Integer> photographCounts;
		private final Map<String, Long> deletedPhotographs;

		public Mutable(PhotographTracker tracker) {
			this.photographCounts = new Object2IntOpenHashMap<>(tracker.photographCounts);
			this.deletedPhotographs = new Object2LongOpenHashMap<>(tracker.deletedPhotographs);
		}

		public void incrementPhotographCount(String photographName, int step) {
			if (step == 0 || (step < 0 && !this.photographCounts.containsKey(photographName))) return;
			this.photographCounts.merge(photographName, step, Integer::sum);
		}

		public PhotographTracker toImmutable(MinecraftServer server) {
			final Map<String, Integer> cleanPhotographCounts = new Object2IntOpenHashMap<>(this.photographCounts);
			final Map<String, Long> deletedPhotographs = new Object2LongOpenHashMap<>(this.deletedPhotographs);

			final long gameTime = server.overworld().getGameTime();
			cleanPhotographCounts.entrySet().removeIf(entry -> {
				if (entry.getValue() <= 0) {
					deletedPhotographs.put(entry.getKey(), gameTime);
					return true;
				}
				return false;
			});

			return new PhotographTracker(ImmutableMap.copyOf(cleanPhotographCounts), ImmutableMap.copyOf(deletedPhotographs));
		}
	}
}
