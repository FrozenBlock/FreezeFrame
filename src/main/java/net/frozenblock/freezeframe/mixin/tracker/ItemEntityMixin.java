package net.frozenblock.freezeframe.mixin.tracker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

	@WrapOperation(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V",
			ordinal = 1
		)
	)
	public void freezeFrame$onItemEntityDespawned(ItemEntity instance, Operation<Void> original) {
		PhotographTracker.incrementOnItemStackDeletion(instance.level(), instance.getItem());
		original.call(instance);
	}

	@WrapOperation(
		method = "hurtServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
		)
	)
	public void freezeFrame$onItemEntityDestroyed(ItemEntity instance, Operation<Void> original) {
		PhotographTracker.incrementOnItemStackDeletion(instance.level(), instance.getItem(), false);
		original.call(instance);
	}
}
