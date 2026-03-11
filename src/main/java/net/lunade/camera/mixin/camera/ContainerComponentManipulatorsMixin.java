package net.lunade.camera.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.stream.Stream;
import net.lunade.camera.registry.CameraPortContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ContainerComponentManipulators.class)
public class ContainerComponentManipulatorsMixin {

	@ModifyExpressionValue(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/stream/Stream;of([Ljava/lang/Object;)Ljava/util/stream/Stream;"
		)
	)
	private static Stream cameraPort$addCameraContainerComponentManipulator(Stream original) {
		final Stream.Builder newStream = Stream.builder();
		original.forEach(object -> newStream.add(object));
		newStream.add(CameraPortContainerComponentManipulators.CAMERA_CONTENTS);
		return newStream.build();
	}

}
