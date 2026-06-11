package net.frozenblock.freezeframe.mixin.client.tracker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.networking.packet.DeleteItemStackPacket;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(CreativeModeInventoryScreen.ItemPickerMenu.class)
public abstract class CreativeInventoryScreenItemPickerMenuMixin extends AbstractContainerMenu {

	protected CreativeInventoryScreenItemPickerMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
		super(menuType, containerId);
	}

	@WrapOperation(
		method = "quickMoveStack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/Slot;setByPlayer(Lnet/minecraft/world/item/ItemStack;)V",
			ordinal = 0
		)
	)
	public void freezeFrame$onDeleteFromQuickMove(Slot instance, ItemStack itemStack, Operation<Void> original) {
		if (!instance.getItem().isEmpty()) {
			ClientPlayNetworking.send(new DeleteItemStackPacket(instance.getItem().copy()));
			FFConstants.log("freezeFrame$onDeleteFromQuickMove - CreativeModeInventoryScreen$ItemPickerMenu", FFConstants.UNSTABLE_LOGGING);
		}
		original.call(instance, itemStack);
	}
}
