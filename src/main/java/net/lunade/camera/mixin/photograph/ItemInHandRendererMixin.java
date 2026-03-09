package net.lunade.camera.mixin.photograph;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lunade.camera.client.photograph.PhotographRenderer;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm);

	@Inject(
		method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
			ordinal = 0,
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	private void cameraPort$renderArmWithItem(
		AbstractClientPlayer player,
		float frameInterp,
		float xRot,
		InteractionHand hand,
		float attackValue,
		ItemStack stack,
		float inverseArmHeight,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int lightCoords,
		CallbackInfo info,
		@Local(name = "arm") HumanoidArm arm
	) {
		if (!stack.is(CameraPortItems.PHOTOGRAPH)) return;

		final PhotographComponent photographComponent = stack.get(CameraPortItems.PHOTO_COMPONENT);
		if (photographComponent == null) return;

		this.cameraPort$submitPhotographInHand(poseStack, collector, lightCoords, inverseArmHeight, attackValue, arm, photographComponent);
		info.cancel();
	}

	@Unique
	private void cameraPort$submitPhotographInHand(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int lightCoords,
		float inverseArmHeight,
		float attackValue,
		HumanoidArm arm,
		PhotographComponent photographComponent
	) {
		poseStack.pushPose();

		float armOffset = arm == HumanoidArm.RIGHT ? 1F : -1F;
		poseStack.translate(armOffset * 0.125F, -0.125F, 0F);
		if (!this.minecraft.player.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.ZP.rotationDegrees(armOffset * 10F));
			this.renderPlayerArm(poseStack, collector, lightCoords, inverseArmHeight, attackValue, arm);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate(armOffset * 0.51F, -0.08F + inverseArmHeight * -1.2F, -0.75F);
		float g = Mth.sqrt(attackValue);
		float h = Mth.sin(g * Mth.PI);
		float i = -0.5F * h;
		float j = 0.4F * Mth.sin(g * (Mth.PI * 2F));
		float k = -0.3F * Mth.sin(attackValue * Mth.PI);
		poseStack.translate(armOffset * i, j - 0.3F * h, k);
		poseStack.mulPose(Axis.XP.rotationDegrees(h * -45F));
		poseStack.mulPose(Axis.YP.rotationDegrees(armOffset * h * -30F));
		this.cameraPort$renderPhotograph(poseStack, collector, lightCoords, photographComponent);
		poseStack.popPose();

		poseStack.popPose();
	}

	@Unique
	private void cameraPort$renderPhotograph(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, PhotographComponent photographComponent) {
		poseStack.mulPose(Axis.YP.rotationDegrees(180F));
		poseStack.scale(0.38F, 0.38F, 0.38F);
		PhotographRenderer.submit(poseStack, collector, photographComponent.identifier(), lightCoords, true);
	}
}
