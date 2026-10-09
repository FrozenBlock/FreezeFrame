package net.frozenblock.freezeframe.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.registry.FFCriteriaTriggers;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class PrintPhotographTrigger extends SimpleCriterionTrigger<PrintPhotographTrigger.TriggerInstance> {

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack item) {
		this.trigger(player, trigger -> trigger.matches(player, item.get(FFDataComponents.PHOTOGRAPH.get())));
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, boolean playerMustBePhotographer, boolean allowCopy) implements SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
			Codec.BOOL.fieldOf("player_must_be_photographer").forGetter(TriggerInstance::playerMustBePhotographer),
			Codec.BOOL.fieldOf("allow_copy").forGetter(TriggerInstance::allowCopy)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> of(EntityPredicate.Builder player, boolean playerMustBePhotographer, boolean allowCopy) {
			return FFCriteriaTriggers.PRINT_PHOTOGRAPH.get()
				.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(player)), playerMustBePhotographer, allowCopy));
		}

		public static Criterion<TriggerInstance> of(EntityPredicate.Builder player, boolean playerMustBePhotographer) {
			return of(player, playerMustBePhotographer, false);
		}

		public static Criterion<TriggerInstance> of(boolean playerMustBePhotographer) {
			return FFCriteriaTriggers.PRINT_PHOTOGRAPH.get()
				.createCriterion(new TriggerInstance(Optional.empty(), playerMustBePhotographer, false));
		}

		public boolean matches(ServerPlayer player, @Nullable Photograph photograph) {
			if (photograph == null) return false;
			return (!this.playerMustBePhotographer || photograph.isPlayerPhotographer(player)) && (this.allowCopy || photograph.isOriginal());
		}
	}
}
