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

import blue.endless.jankson.Comment;
import net.frozenblock.lib.config.api.instance.Config;
import net.frozenblock.lib.config.api.instance.json.JsonConfig;
import net.frozenblock.lib.config.api.instance.json.JsonType;
import net.frozenblock.lib.config.api.registry.ConfigRegistry;
import net.lunade.camera.CameraPortConstants;

public class CameraPortConfig {
    public static final Config<CameraPortConfig> INSTANCE = ConfigRegistry.register(
        new JsonConfig<>(
            CameraPortConstants.MOD_ID,
            CameraPortConfig.class,
            JsonType.JSON5_UNQUOTED_KEYS,
            true
        )
    );

    @Comment("Whether the latest-taken Photo in a world should replace the world icon.")
    public boolean useLatestPhotoAsWorldIcon = false;

    @Comment("Hides embedded photo previews and photo information text in Film Roll tooltips.")
    public boolean hideFilmPhotoPreviewAndInfo = false;

    @Comment("Hides Photograph image previews in tooltips while keeping text information visible.")
    public boolean hidePhotographPreview = false;

    public static void init() {
    }

    public static CameraPortConfig get(boolean real) {
        if (real) return INSTANCE.instance();
        return INSTANCE.config();
    }

    public static CameraPortConfig get() {
        return get(false);
    }
}
