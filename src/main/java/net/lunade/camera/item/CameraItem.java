package net.lunade.camera.item;

import net.lunade.camera.CameraPortMain;
import net.lunade.camera.client.camera.CameraScreenshotManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

public class CameraItem extends SpawnEggItem {

	public CameraItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		final InteractionResult interactionResult = super.use(level, player, hand);
		if (interactionResult.consumesAction()) return interactionResult;

		final ItemStack stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack)) return interactionResult;

		player.getCooldowns().addCooldown(stack, 10);
		if (level.isClientSide()) {
			if (CameraScreenshotManager.isPossessingCamera()) return InteractionResult.FAIL;
			CameraScreenshotManager.executeScreenshot(null, true);
		}
		level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), CameraPortMain.CAMERA_SNAP, SoundSource.PLAYERS, 0.5F, 1F);
		return InteractionResult.SUCCESS;
	}

}
