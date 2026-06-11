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

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.item.photograph.PhotographTracker;

public class FFAttachmentTypes {
	public static final AttachmentType<PhotographTracker> PHOTOGRAPH_TRACKER = AttachmentRegistry.create(
		FFConstants.id("photograph_tracker"),
		builder -> {
			builder.persistent(PhotographTracker.CODEC);
		}
	);
	public static final AttachmentType<FilmFilter> FILM_FILTER = AttachmentRegistry.create(
		FFConstants.id("film_filter"),
		builder -> {
			builder.persistent(FilmFilter.CODEC);
			builder.syncWith(FilmFilter.STREAM_CODEC, AttachmentSyncPredicate.all());
		}
	);

	public static void init() {}
}
