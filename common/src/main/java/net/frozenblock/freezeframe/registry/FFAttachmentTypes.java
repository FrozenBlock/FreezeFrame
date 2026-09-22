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

package net.frozenblock.freezeframe.registry;

import com.mojang.serialization.Codec;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.filter.FilmFilter;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;
import net.frozenblock.lib.platform.api.attachment.DataAttachmentSyncPredicate;
import net.frozenblock.lib.platform.api.attachment.DataAttachmentType;
import net.minecraft.world.item.ItemStack;

public final class FFAttachmentTypes {
	public static final DataAttachmentType<PhotographTracker> PHOTOGRAPH_TRACKER = DataAttachmentType.create(
		FFConstants.id("photograph_tracker"),
		builder -> {
			builder.persistent(PhotographTracker.CODEC);
		}
	);
	public static final DataAttachmentType<Long> PHOTOGRAPH_TRACKER_LAST_SYNC_TIMESTAMP = DataAttachmentType.create(
		FFConstants.id("photograph_tracker_last_sync_timestamp"),
		builder -> {
			builder.persistent(Codec.LONG);
		}
	);
	public static final DataAttachmentType<FilmFilter> FILM_FILTER = DataAttachmentType.create(
		FFConstants.id("film_filter"),
		builder -> {
			builder.persistent(FilmFilter.CODEC);
			builder.syncWith(FilmFilter.STREAM_CODEC, DataAttachmentSyncPredicate.all());
		}
	);
	public static final DataAttachmentType<ItemStack> CREATIVE_MODE_CARRIED_ITEM = DataAttachmentType.create(
		FFConstants.id("creative_mode_carried_item"),
		builder -> {
			builder.syncWith(ItemStack.STREAM_CODEC, DataAttachmentSyncPredicate.targetOnly());
		}
	);

	public static void init() {}

	private FFAttachmentTypes() {}
}
