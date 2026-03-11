package net.lunade.camera.registry;

import java.util.stream.Stream;
import net.lunade.camera.component.CameraContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;

public class CameraPortContainerComponentManipulators {
	public static final ContainerComponentManipulator<CameraContents> CAMERA_CONTENTS = new ContainerComponentManipulator<>() {
		@Override
		public DataComponentType<CameraContents> type() {
			return CameraPortDataComponents.CAMERA_CONTENTS;
		}

		public CameraContents empty() {
			return CameraContents.EMPTY;
		}

		public Stream<ItemStack> getContents(CameraContents component) {
			return component.itemCopyStream();
		}

		public CameraContents setContents(CameraContents component, Stream<ItemStack> newContents) {
			final CameraContents.Mutable builder = new CameraContents.Mutable(component).clearItems();
			newContents.forEach(builder::tryInsert);
			return builder.toImmutable();
		}
	};

	public static void init() {
	}
}
