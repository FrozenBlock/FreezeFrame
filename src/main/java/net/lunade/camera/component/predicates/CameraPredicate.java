package net.lunade.camera.component.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.lunade.camera.component.CameraContents;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.advancements.criterion.CollectionPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemInstance;

public record CameraPredicate(Optional<CollectionPredicate<ItemInstance, ItemPredicate>> items) implements SingleComponentItemPredicate<CameraContents> {
	public static final Codec<CameraPredicate> CODEC = RecordCodecBuilder.create(
		i -> i.group(
			CollectionPredicate.codec(ItemPredicate.CODEC).optionalFieldOf("items").forGetter(CameraPredicate::items)
		).apply(i, CameraPredicate::new)
	);

	@Override
	public DataComponentType<CameraContents> componentType() {
		return CameraPortDataComponents.CAMERA_CONTENTS;
	}

	public boolean matches(CameraContents value) {
		return !this.items.isPresent() || this.items.get().test(value.items());
	}
}
