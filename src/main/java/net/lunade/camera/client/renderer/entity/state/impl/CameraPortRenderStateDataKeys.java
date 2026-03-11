package net.lunade.camera.client.renderer.entity.state.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class CameraPortRenderStateDataKeys {
	public static final RenderStateDataKey<Identifier> PHOTOGRAPH_ID = RenderStateDataKey.create();
}
