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

package net.frozenblock.freezeframe.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;

@Environment(EnvType.CLIENT)
public final class BookPagePhotographUiState {
	private static InteractionHand hand = InteractionHand.MAIN_HAND;
	private static int pageIndex = 0;
	private static boolean suppressBookEditorPhotoControls = false;

	private BookPagePhotographUiState() {
	}

	public static void rememberOpenRequest(InteractionHand requestedHand, int requestedPageIndex) {
		hand = requestedHand;
		pageIndex = Math.max(0, requestedPageIndex);
	}

	public static int resolveRequestedPage(InteractionHand requestedHand, int fallbackPageIndex) {
		if (hand == requestedHand) return pageIndex;
		return Math.max(0, fallbackPageIndex);
	}

	public static void setSuppressBookEditorPhotoControls(boolean suppress) {
		suppressBookEditorPhotoControls = suppress;
	}

	public static boolean suppressBookEditorPhotoControls() {
		return suppressBookEditorPhotoControls;
	}
}
