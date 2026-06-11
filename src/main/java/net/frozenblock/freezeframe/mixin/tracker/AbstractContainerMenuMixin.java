package net.frozenblock.freezeframe.mixin.tracker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.networking.packet.ChangeItemStackSizePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Environment(EnvType.CLIENT)
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

	@Shadow
	@Final
	public static int QUICKCRAFT_TYPE_CLONE;

	@Shadow
	private int quickcraftType;

	@WrapOperation(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/Slot;setByPlayer(Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;getQuickCraftPlaceCount(IILnet/minecraft/world/item/ItemStack;)I"
			)
		)
	)
	public void freezeFrame$onDragCloneQuickCraft(Slot instance, ItemStack itemStack, Operation<Void> original) {
		if (this.quickcraftType == QUICKCRAFT_TYPE_CLONE) {
			final int increaseCount = itemStack.getCount() - instance.getItem().getCount();
			if (increaseCount > 0) {
				ClientPlayNetworking.send(new ChangeItemStackSizePacket(itemStack.copy(), increaseCount));
				FFConstants.log("freezeFrame$onDragCloneQuickCraft - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
			}
		}
		original.call(instance, itemStack);
	}

	@WrapOperation(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;",
			ordinal = 0
		),
		slice = @Slice(
			from = @At(
				value = "FIELD",
				target = "Lnet/minecraft/world/inventory/ContainerInput;CLONE:Lnet/minecraft/world/inventory/ContainerInput;",
				opcode = Opcodes.GETSTATIC,
				ordinal = 0
			)
		)
	)
	public ItemStack freezeframe$onClone(ItemStack instance, int count, Operation<ItemStack> original) {
		if (count > 0) {
			ClientPlayNetworking.send(new ChangeItemStackSizePacket(instance.copy(), count));
			FFConstants.log("freezeFrame$onClone - AbstractContainerMenu", FFConstants.UNSTABLE_LOGGING);
		}
		return original.call(instance, count);
	}
}
