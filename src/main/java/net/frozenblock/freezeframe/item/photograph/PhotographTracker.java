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

import com.google.common.collect.ImmutableList;
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
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmContents;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.config.FFConfig;
import net.frozenblock.freezeframe.networking.packet.DeletePhotographPacket;
import net.frozenblock.freezeframe.registry.FFAttachmentTypes;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.frozenblock.lib.file.transfer.FileTransferPacket;
import net.frozenblock.lib.networking.FrozenNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;

public record PhotographTracker(Map<String, Integer> photographCounts, List<String> deletedPhotographs) {
	private static final boolean LOG_DELETIONS = true;
	private static final boolean LOG_FAILED_DELETION_ATTEMPTS = false;
	private static final boolean LOG_INCREMENTS = true;
	private static final PhotographTracker EMPTY = new PhotographTracker(Map.of(), List.of());
	public static final Codec<PhotographTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("photograph_counts").forGetter(PhotographTracker::photographCounts),
		Codec.STRING.listOf().fieldOf("deleted_photographs").forGetter(PhotographTracker::deletedPhotographs)
	).apply(instance, PhotographTracker::new));

	public static void init() {
		ServerPlayerEvents.JOIN.register(PhotographTracker::notifyOfAllDeletedPhotographs);

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> removeCreativeModeCarriedItem(entity));
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> removeCreativeModeCarriedItem(player));
		ServerPlayerEvents.LEAVE.register(PhotographTracker::removeCreativeModeCarriedItem);
	}

	public static void notifyOfAllDeletedPhotographs(ServerPlayer player) {
		final PhotographTracker tracker = get(player.server);
		if (tracker.deletedPhotographs.isEmpty()) return;
		if (FrozenNetworking.isLocalPlayer(player)) return;
		ServerPlayNetworking.send(player, new DeletePhotographPacket(tracker.deletedPhotographs));
	}

	public static void notifyOfDeletedPhotograph(MinecraftServer server, List<String> photographNames) {
		for (ServerPlayer player : PlayerLookup.all(server)) {
			if (FrozenNetworking.isLocalPlayer(player)) continue;
			ServerPlayNetworking.send(player, new DeletePhotographPacket(photographNames));
		}
	}

	public static void removeCreativeModeCarriedItem(Entity entity) {
		if (!(entity instanceof ServerPlayer player)) return;
		final ItemStack carriedAttachment = player.getAttachedOrElse(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM, ItemStack.EMPTY);
		if (!carriedAttachment.isEmpty()) incrementOnItemStackDeletion(player.level(), carriedAttachment);
		player.removeAttached(FFAttachmentTypes.CREATIVE_MODE_CARRIED_ITEM);
	}

	public static PhotographTracker get(MinecraftServer server) {
		return server.overworld().getAttachedOrElse(FFAttachmentTypes.PHOTOGRAPH_TRACKER, EMPTY);
	}

	public static PhotographTracker get(Level level) {
		return get(level.getServer());
	}

	public static PhotographTracker setAttached(MinecraftServer server, PhotographTracker tracker) {
		return server.overworld().setAttached(FFAttachmentTypes.PHOTOGRAPH_TRACKER, tracker);
	}

	public static PhotographTracker setAttached(Level level, PhotographTracker tracker) {
		return setAttached(level.getServer(), tracker);
	}

	public static void incrementPhotographCountAndDeleteIfEmpty(Level level, String photographName, int step) {
		if (!(level instanceof ServerLevel)) return;

		final PhotographTracker initialTracker = get(level);
		final int oldCount = initialTracker.photographCounts.getOrDefault(photographName, 0);

		final Mutable tracker = initialTracker.mutable();
		tracker.incrementPhotographCount(photographName, step);
		final PhotographTracker finalTracker = tracker.toImmutable();
		setAttached(level, tracker.toImmutable());

		final List<String> newlyDeletedPhotographs = initialTracker.photographCounts.keySet().stream()
			.filter(key -> !finalTracker.photographCounts.containsKey(key))
			.toList();

		deletePhotographs(level.getServer().getServerDirectory(), newlyDeletedPhotographs);
		notifyOfDeletedPhotograph(level.getServer(), newlyDeletedPhotographs);

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

	public static ItemStack stripAllPhotographComponents(ItemStack stack) {
		stack.remove(FFDataComponents.PHOTOGRAPH);

		final FilmContents initialFilmContents = stack.getOrDefault(FFDataComponents.FILM_CONTENTS, FilmContents.EMPTY);
		if (!initialFilmContents.isEmpty()) {
			final FilmContents.Mutable filmContents = new FilmContents.Mutable(initialFilmContents);
			filmContents.removeAllPhotographs();
			stack.set(FFDataComponents.FILM_CONTENTS, filmContents.toImmutable());
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
		private final List<String> deletedPhotographs;

		public Mutable(PhotographTracker tracker) {
			this.photographCounts = new HashMap<>(tracker.photographCounts);
			this.deletedPhotographs = new ArrayList<>(tracker.deletedPhotographs);
		}

		public void incrementPhotographCount(String photographName, int step) {
			if (step == 0 || (step < 0 && !this.photographCounts.containsKey(photographName))) return;
			this.photographCounts.merge(photographName, step, Integer::sum);
		}

		public PhotographTracker toImmutable() {
			final Map<String, Integer> cleanedMap = new HashMap<>(this.photographCounts);
			final List<String> deletedPhotographs = new ArrayList<>(this.deletedPhotographs);
			cleanedMap.entrySet().removeIf(entry -> {
				if (entry.getValue() <= 0) {
					deletedPhotographs.add(entry.getKey());
					return true;
				}
				return false;
			});
			return new PhotographTracker(ImmutableMap.copyOf(cleanedMap), ImmutableList.copyOf(deletedPhotographs));
		}
	}
}
