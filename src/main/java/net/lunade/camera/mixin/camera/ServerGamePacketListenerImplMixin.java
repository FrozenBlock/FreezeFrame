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

package net.lunade.camera.mixin.camera;

import java.util.List;
import net.lunade.camera.component.BookPagePhotographs;
import net.lunade.camera.registry.CameraPortDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Unique
	private ItemStack cameraPort$preSignBook = ItemStack.EMPTY;

	@Inject(method = "signBook", at = @At("HEAD"))
	private void cameraPort$capturePreSignBook(FilteredText title, List<FilteredText> pages, int slot, CallbackInfo info) {
		this.cameraPort$preSignBook = this.player.getInventory().getItem(slot).copy();
	}

	@Inject(method = "signBook", at = @At("TAIL"))
	private void cameraPort$copyBookPhotoComponentToWrittenBook(FilteredText title, List<FilteredText> pages, int slot, CallbackInfo info) {
		final ItemStack written = this.player.getInventory().getItem(slot);
		if (!written.is(Items.WRITTEN_BOOK)) return;

		final BookPagePhotographs photographs = this.cameraPort$preSignBook.get(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		if (photographs != null) {
			written.set(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS, photographs);
		} else {
			written.remove(CameraPortDataComponents.BOOK_PAGE_PHOTOGRAPHS);
		}
	}
}
