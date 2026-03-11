package net.lunade.camera.component.tooltip;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PhotographTooltipComponent(Identifier identifier, String photographer, boolean isCopy) implements TooltipComponent {
}
