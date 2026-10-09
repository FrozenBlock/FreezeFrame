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

package net.frozenblock.freezeframe.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.frozenblock.freezeframe.registry.FFCriteriaTriggers;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class TakePhotographTrigger extends SimpleCriterionTrigger<TakePhotographTrigger.TriggerInstance> {

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack item) {
		this.trigger(player, trigger -> trigger.matches(item));
	}

	public record TriggerInstance(
		Optional<ContextAwarePredicate> player,
		Optional<ItemPredicate> item
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
			ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> lookingAt(EntityPredicate.Builder player, ItemPredicate.Builder with) {
			return FFCriteriaTriggers.TAKE_PHOTOGRAPH.get()
				.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), Optional.of(with.build())));
		}

		public static Criterion<TriggerInstance> lookingAt(EntityPredicate.Builder player) {
			return FFCriteriaTriggers.TAKE_PHOTOGRAPH.get()
				.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), Optional.empty()));
		}

		public static Criterion<TriggerInstance> using(ItemPredicate.Builder using) {
			return FFCriteriaTriggers.TAKE_PHOTOGRAPH.get()
				.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(using.build())));
		}

		public boolean matches(ItemStack item) {
			return !this.item.isPresent() || this.item.get().test(item);
		}
	}
}
