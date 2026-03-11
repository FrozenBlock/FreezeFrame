package net.lunade.camera.component.tooltip;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PhotographTooltip(Identifier identifier, String photographer, boolean isCopy) implements TooltipComponent {
}
