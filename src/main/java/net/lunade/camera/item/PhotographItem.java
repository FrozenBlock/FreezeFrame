package net.lunade.camera.item;

import java.util.Optional;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.tooltip.PhotographTooltipComponent;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

public class PhotographItem extends Item {

	public PhotographItem(Properties properties) {
		super(properties);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		final TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		if (!tooltipDisplay.shows(CameraPortItems.PHOTO_COMPONENT)) return Optional.empty();

		final PhotographComponent component = stack.get(CameraPortItems.PHOTO_COMPONENT);
		if (component != null) return Optional.of(new PhotographTooltipComponent(component.identifier(), component.photographer(), component.isCopy()));
		return Optional.empty();
	}
}
