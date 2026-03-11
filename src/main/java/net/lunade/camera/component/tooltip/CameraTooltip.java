package net.lunade.camera.component.tooltip;

import net.lunade.camera.component.CameraContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CameraTooltip(CameraContents contents) implements TooltipComponent {
}
