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

package net.lunade.camera.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;
import io.netty.buffer.ByteBuf;
import net.frozenblock.lib.config.v2.config.ConfigData;
import net.frozenblock.lib.config.v2.config.ConfigSettings;
import net.frozenblock.lib.config.v2.entry.ConfigEntry;
import net.frozenblock.lib.config.v2.entry.EntryType;
import net.frozenblock.lib.config.v2.registry.ID;
import net.lunade.camera.CameraPortConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public class CameraPortConfig {
	public static final ConfigData<?> CONFIG = ConfigData.createAndRegister(ID.of(CameraPortConstants.id("config")), ConfigSettings.JSON5);
	private static final EntryType<PhotographFormat> PHOTOGRAPH_FORMAT_ENTRY_TYPE = EntryType.create(PhotographFormat.CODEC, PhotographFormat.STREAM_CODEC);

	public static final ConfigEntry<Boolean> USE_LATEST_PHOTO_AS_WORLD_ICON = CONFIG.entryBuilder("useLatestPhotoAsWorldIcon", EntryType.BOOL, true)
		.comment("Whether the latest-taken Photo in a world should replace the world icon.")
		.build();

	public static final ConfigEntry<Boolean> HIDE_FILM_PHOTO_PREVIEW_AND_INFO = CONFIG.entryBuilder("hideFilmPhotoPreviewAndInfo", EntryType.BOOL, false)
		.comment("Hides embedded photo previews and photo information text in Film Roll tooltips.")
		.build();

	public static final ConfigEntry<Boolean> HIDE_PHOTOGRAPH_PREVIEW = CONFIG.entryBuilder("hidePhotographPreview", EntryType.BOOL, false)
		.comment("Hides Photograph image previews in tooltips while keeping text information visible.")
		.build();

	public static final ConfigEntry<PhotographFormat> PHOTOGRAPH_FORMAT = CONFIG.unsyncableEntryBuilder("photographFormat", PHOTOGRAPH_FORMAT_ENTRY_TYPE, PhotographFormat.MCPHOTO_LOSSLESS)
		.comment("Dictates the file format to save photographs as. mcphoto is treated as jpeg.")
		.build();

    public static void init() {
    }

	public static enum PhotographFormat implements StringRepresentable {
		PNG("png", "png"),
		MCPHOTO_LOSSLESS("mcphoto_lossless", "mcphoto"),
		MCPHOTO_COMPRESSED("mcphoto_compressed", "mcphoto", 95);
		public static final Codec<PhotographFormat> CODEC = StringRepresentable.fromEnum(PhotographFormat::values);
		public static final StreamCodec<ByteBuf, PhotographFormat> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
			string -> CODEC.parse(JavaOps.INSTANCE, string).result().orElseThrow(),
			PhotographFormat::getSerializedName
		);
		private final String name;
		private final String extension;
		private final int quality;

		PhotographFormat(String name, String extension) {
			this(name, extension, 100);
		}

		PhotographFormat(String name, String extension, int quality) {
			this.name = name;
			this.extension = extension;
			this.quality = quality;
		}

		public String extension() {
			return this.extension;
		}

		public int quality() {
			return this.quality;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
