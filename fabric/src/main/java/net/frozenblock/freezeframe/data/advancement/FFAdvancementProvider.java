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

package net.frozenblock.freezeframe.data.advancement;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.advancements.trigger.PrintPhotographTrigger;
import net.frozenblock.freezeframe.advancements.trigger.TakePhotographTrigger;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.component.filter.SpecialFilmFilter;
import net.frozenblock.freezeframe.component.predicates.CameraPredicate;
import net.frozenblock.freezeframe.component.predicates.FilmFilterPredicate;
import net.frozenblock.freezeframe.registry.FFDataComponentPredicates;
import net.frozenblock.freezeframe.registry.FFItems;
import net.frozenblock.freezeframe.registry.FFRegistries;
import net.frozenblock.freezeframe.registry.FFSpecialFilmFilters;
import net.frozenblock.lib.advancement.api.predicates.entity.MobPredicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.PlayerPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class FFAdvancementProvider extends FabricAdvancementProvider {

	public FFAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> writer) {
		final HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
		final HolderGetter<SpecialFilmFilter> specialFilmFilters = registries.lookupOrThrow(FFRegistries.SPECIAL_FILM_FILTER);

		final AdvancementHolder adventure = Advancement.Builder.advancement().build(FFConstants.vanillaId("adventure/root"));
		final AdvancementHolder husbandry = Advancement.Builder.advancement().build(FFConstants.vanillaId("husbandry/root"));

		final HolderLookup.RegistryLookup<Block> blocks = registries.lookupOrThrow(Registries.BLOCK);
		final HolderLookup.RegistryLookup<EntityType<?>> entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

		final AdvancementHolder photographOfMob = Advancement.Builder.advancement()
			.parent(adventure)
			.display(
				FFItems.CAMERA,
				Component.translatable("freezeframe.advancements.adventure.photograph_of_mob.title"),
				Component.translatable("freezeframe.advancements.adventure.photograph_of_mob.description"),
				null,
				AdvancementType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"photograph_of_mob",
				takePhotographOfEntity(
					EntityPredicate.Builder.entity().put(MobPredicate.CODEC, MobPredicate.INSTANCE)
				)
			)
			.save(writer, FFConstants.id("adventure/photograph_of_mob"));

		final AdvancementHolder printAPhotograph = Advancement.Builder.advancement()
			.parent(photographOfMob)
			.display(
				FFItems.PHOTOGRAPH,
				Component.translatable("freezeframe.advancements.adventure.print_a_photograph.title"),
				Component.translatable("freezeframe.advancements.adventure.print_a_photograph.description"),
				null,
				AdvancementType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"print_a_photograph",
				printPhotograph(true)
			)
			.save(writer, FFConstants.id("adventure/print_a_photograph"));

		final AdvancementHolder photographOfInvertedEnderman = Advancement.Builder.advancement()
			.parent(printAPhotograph)
			.display(
				Items.ENDER_PEARL,
				Component.translatable("freezeframe.advancements.adventure.photograph_of_inverted_enderman.title"),
				Component.translatable("freezeframe.advancements.adventure.photograph_of_inverted_enderman.description"),
				null,
				AdvancementType.TASK,
				true,
				true,
				false
			)
			.addCriterion(
				"photograph_of_inverted_enderman",
				takePhotographOfEntityWith(
					EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.ENDERMAN),
					ItemPredicate.Builder.item().withComponents(
						DataComponentMatchers.Builder.components().partial(
							FFDataComponentPredicates.CAMERA_CONTENTS.get(),
							CameraPredicate.containsOnly(
								ItemPredicate.Builder.item().withComponents(
									DataComponentMatchers.Builder.components().partial(
										FFDataComponentPredicates.FILM_FILTER.get(),
										FilmFilterPredicate.containsOnly(
											new FilmFilterPredicate.LayerPredicate(
												FilmFilter.Layer.special(
													specialFilmFilters.getOrThrow(FFSpecialFilmFilters.INVERT)
												)
											)
										)
									).build()
								).build()
							)
						).build()
					)
				)
			)
			.save(writer, FFConstants.id("adventure/photograph_of_inverted_enderman"));
	}

	private static Criterion<TakePhotographTrigger.TriggerInstance> takePhotographOfEntityWith(EntityPredicate.Builder lookingAt, ItemPredicate.Builder with) {
		return TakePhotographTrigger.TriggerInstance.lookingAt(
			EntityPredicate.Builder.entity().player(
				PlayerPredicate.Builder.player().setLookingAt(lookingAt).build()
			),
			with
		);
	}

	private static Criterion<TakePhotographTrigger.TriggerInstance> takePhotographOfEntity(EntityPredicate.Builder lookingAt) {
		return TakePhotographTrigger.TriggerInstance.lookingAt(
			EntityPredicate.Builder.entity().player(
				PlayerPredicate.Builder.player().setLookingAt(lookingAt).build()
			)
		);
	}

	private static Criterion<PrintPhotographTrigger.TriggerInstance> printPhotograph(boolean mustBePhotographer) {
		return PrintPhotographTrigger.TriggerInstance.of(mustBePhotographer);
	}
}
