package net.lunade.camera.client.renderer.entity.state.impl;

import net.minecraft.resources.Identifier;

public interface ItemFrameRenderStateInterface {
	void cameraPort$addPhotographLocation(Identifier location);
	Identifier cameraPort$getPhotographLocation();
}
