package net.lunade.camera.component.tooltip;

import net.lunade.camera.component.FilmContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record FilmTooltip(FilmContents contents) implements TooltipComponent {
}
