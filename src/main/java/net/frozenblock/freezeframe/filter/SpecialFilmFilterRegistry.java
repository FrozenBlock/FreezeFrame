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

package net.frozenblock.freezeframe.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

public final class SpecialFilmFilterRegistry {
	private static final String DIRECTORY = "freezeframe/film_filters";
	private static final String JSON_SUFFIX = ".json";
	private static final Map<String, SpecialFilmFilterDefinition> BY_ID = new HashMap<>();
	private static final Map<Item, SpecialFilmFilterDefinition> BY_INGREDIENT = new HashMap<>();
	private static List<SpecialFilmFilterDefinition> DEFINITIONS = List.of();

	private SpecialFilmFilterRegistry() {
	}

	public static void init() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return FFConstants.id("film_filters");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				loadDefinitions(resourceManager);
			}
		});
	}

	public static List<SpecialFilmFilterDefinition> all() {
		return DEFINITIONS;
	}

	public static SpecialFilmFilterDefinition getById(String id) {
		return BY_ID.get(id);
	}

	public static SpecialFilmFilterDefinition getByIngredient(Item ingredient) {
		return BY_INGREDIENT.get(ingredient);
	}

	public static Identifier shaderId(String specialId) {
		final SpecialFilmFilterDefinition definition = getById(specialId);
		if (definition == null || definition.shader().isBlank()) return null;
		return Identifier.tryParse(definition.shader());
	}

	private static void loadDefinitions(ResourceManager resourceManager) {
		final List<Map.Entry<Identifier, Resource>> resources = new ArrayList<>(resourceManager.listResources(DIRECTORY, id -> id.getPath().endsWith(JSON_SUFFIX)).entrySet());
		resources.sort(Comparator.comparing(entry -> entry.getKey().toString()));

		final Map<String, SpecialFilmFilterDefinition> byId = new HashMap<>();
		final Map<Item, SpecialFilmFilterDefinition> byIngredient = new HashMap<>();
		final List<SpecialFilmFilterDefinition> definitions = new ArrayList<>();

		for (Map.Entry<Identifier, Resource> entry : resources) {
			final Identifier filterId = filterIdFromResourceId(entry.getKey());
			if (filterId == null) continue;

			try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
				final JsonElement json = JsonParser.parseReader(reader);
				final SpecialFilmFilterDefinition definition = SpecialFilmFilterDefinition.FILE_CODEC
					.parse(JsonOps.INSTANCE, json)
					.getOrThrow()
					.withId(filterId);

				final Item ingredient = BuiltInRegistries.ITEM.getOptional(definition.ingredient()).orElse(null);
				if (ingredient == null) {
					FFConstants.warn("Skipping film filter " + filterId + " because ingredient " + definition.ingredient() + " is not registered", true);
					continue;
				}

				final SpecialFilmFilterDefinition previousByIngredient = byIngredient.put(ingredient, definition);
				if (previousByIngredient != null) {
					FFConstants.warn("Film filter " + filterId + " replaced ingredient mapping from " + previousByIngredient.id() + " for " + definition.ingredient(), true);
				}
				byId.put(definition.id().toString(), definition);
				definitions.add(definition);
			} catch (Exception exception) {
				FFConstants.error("Failed to load film filter definition " + filterId, exception);
			}
		}

		BY_ID.clear();
		BY_INGREDIENT.clear();
		BY_ID.putAll(byId);
		BY_INGREDIENT.putAll(byIngredient);
		DEFINITIONS = List.copyOf(definitions);
		FFConstants.log("Loaded " + DEFINITIONS.size() + " film filter definitions", FFConstants.UNSTABLE_LOGGING);
	}

	private static Identifier filterIdFromResourceId(Identifier resourceId) {
		final String path = resourceId.getPath();
		if (!path.startsWith(DIRECTORY + "/") || !path.endsWith(JSON_SUFFIX)) return null;
		final String filterPath = path.substring((DIRECTORY + "/").length(), path.length() - JSON_SUFFIX.length());
		return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), filterPath);
	}
}
