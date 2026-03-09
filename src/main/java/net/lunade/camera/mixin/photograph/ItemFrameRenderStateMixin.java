package net.lunade.camera.mixin.photograph;

import net.lunade.camera.client.renderer.entity.state.impl.ItemFrameRenderStateInterface;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemFrameRenderState.class)
public class ItemFrameRenderStateMixin implements ItemFrameRenderStateInterface {

	@Unique
	private Identifier cameraPort$photographLocation;

	@Unique
	@Override
	public void cameraPort$addPhotographLocation(Identifier location) {
		this.cameraPort$photographLocation = location;
	}

	@Unique
	@Override
	public Identifier cameraPort$getPhotographLocation() {
		return this.cameraPort$photographLocation;
	}
}
