package net.lunade.camera.item;

import java.util.Optional;
import com.mojang.datafixers.util.Pair;
import net.lunade.camera.component.PhotographComponent;
import net.lunade.camera.component.tooltip.PhotographTooltipComponent;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

public class PhotographItem extends Item {

	public PhotographItem(Properties properties) {
		super(properties);
	}

	public static Pair<Identifier, String> getPhotographInfo(ItemStack stack) {
		PhotographComponent component = stack.get(CameraPortItems.PHOTO_COMPONENT);
		if (component != null) return Pair.of(component.identifier(), component.photographer());
		return null;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		final TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		if (!tooltipDisplay.shows(CameraPortItems.PHOTO_COMPONENT)) return Optional.empty();

		final Pair<Identifier, String> photographInfo = getPhotographInfo(stack);
		if (photographInfo != null) return Optional.of(new PhotographTooltipComponent(photographInfo.getFirst(), photographInfo.getSecond()));
		return Optional.empty();
	}
}
