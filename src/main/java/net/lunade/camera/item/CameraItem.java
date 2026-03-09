package net.lunade.camera.item;

import net.lunade.camera.CameraPortMain;
import net.lunade.camera.client.camera.CameraScreenshotManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CameraItem extends SpawnEggItem {

	public CameraItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		BlockHitResult blockHitResult = SpawnEggItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			if (!player.getCooldowns().isOnCooldown(itemStack)) {
				player.getCooldowns().addCooldown(itemStack, 10);
				if (level.isClientSide()) {
					if (CameraScreenshotManager.possessingCamera) return InteractionResult.FAIL;
					CameraScreenshotManager.executeScreenshot(null, true);
				}
				level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), CameraPortMain.CAMERA_SNAP, SoundSource.PLAYERS, 0.5F, 1F);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.FAIL;
		}
		return super.use(level, player, interactionHand);
	}

}
