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

package net.frozenblock.freezeframe.mixin.client.camera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.client.model.FreezeFrameArmPoses;
import net.frozenblock.freezeframe.item.CameraItem;
import net.frozenblock.freezeframe.tag.CameraPortItemTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

	@Inject(
		method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/model/HumanoidModel$ArmPose;ITEM:Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
			opcode = Opcodes.GETSTATIC
		),
		cancellable = true
	)
	private static void freezeFrame$setArmPoseToCamera(Avatar avatar, ItemStack itemInHand, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> info) {
		if (avatar.swinging || hand != InteractionHand.MAIN_HAND || !itemInHand.is(CameraPortItemTags.CAMERAS)) return;
		if (CameraItem.isCapableOfTakingPhotos(itemInHand)) {
			info.setReturnValue(FreezeFrameArmPoses.CAMERA);
		} else if (avatar.isUsingItem() && avatar.getUsedItemHand() == hand) {
			info.setReturnValue(FreezeFrameArmPoses.CAMERA_ONE_ARM);
		}
	}
}
