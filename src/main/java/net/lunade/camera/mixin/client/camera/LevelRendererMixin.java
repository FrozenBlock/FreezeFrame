package net.lunade.camera.mixin.client.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.util.client.CameraScreenshotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	@Shadow
	@Final
	private Minecraft minecraft;

	@ModifyExpressionValue(
		method = "extractVisibleEntities",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;",
			ordinal = 3
		)
	)
	public Entity cameraPort$trickIntoRenderingPlayer(
		Entity original,
		@Local(name = "entity") Entity entity
	) {
		if (this.minecraft.player == null) return original;
		if (!CameraScreenshotManager.isPossessingCamera() || CameraScreenshotManager.isUsingHandheldCamera()) return original;

		return entity == this.minecraft.player ? entity : original;
	}

}
