/*
 * Copyright 2026 FrozenBlock
 * This file is part of Camera Port.
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

package net.lunade.camera.mixin.client.photograph;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.config.CameraPortConfig;
import org.lwjgl.stb.STBImageWrite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(NativeImage.class)
public class NativeImageMixin {

	@Unique
	private boolean cameraPort$saveAsMCPhoto;

	@Inject(method = "writeToFile(Ljava/io/File;)V", at = @At("HEAD"))
	public void cameraPort$markAsMCPhoto(File file, CallbackInfo info) {
		this.cameraPort$saveAsMCPhoto = file.toPath().toString().endsWith(".mcphoto");
	}

	@WrapOperation(
		method = "writeToChannel",
		at = @At(
			value = "INVOKE",
			target = "Lorg/lwjgl/stb/STBImageWrite;nstbi_write_png_to_func(JJIIIJI)I"
		)
	)
	private int cameraPort$saveAsMCPhoto(long func, long context, int w, int h, int comp, long data, int stride_in_bytes, Operation<Integer> original) {
		if (this.cameraPort$saveAsMCPhoto) {
			final int quality = CameraPortConfig.PHOTOGRAPH_FORMAT.get().quality();
			return STBImageWrite.nstbi_write_jpg_to_func(func, context, w, h, comp, data, quality);
		}
		return original.call(func, context, w, h, comp, data, stride_in_bytes);
	}

}
