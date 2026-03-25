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

package net.frozenblock.freezeframe.item;

import java.util.Optional;
import net.frozenblock.freezeframe.component.Photograph;
import net.frozenblock.freezeframe.component.tooltip.PhotographTooltip;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

public class PhotographItem extends Item {

	public PhotographItem(Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		final Photograph component = stack.get(FFDataComponents.PHOTOGRAPH);
		if (component == null || StringUtil.isNullOrEmpty(component.name())) return super.getName(stack);
		return Component.literal(component.name());
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		final TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
		if (!tooltipDisplay.shows(FFDataComponents.PHOTOGRAPH)) return Optional.empty();

		final Photograph component = stack.get(FFDataComponents.PHOTOGRAPH);
		if (component != null) return Optional.of(new PhotographTooltip(component.identifier(), component.name(), component.photographer(), component.generation()));
		return Optional.empty();
	}
}
