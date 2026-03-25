/*
 * Copyright 2025-2026 FrozenBlock
 * This file is part of The Copperier Age.
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

package net.frozenblock.freezeframe.config.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.lib.config.clothconfig.FrozenClothConfig;
import static net.frozenblock.freezeframe.FFConstants.*;
import net.frozenblock.freezeframe.config.FFConfig;
import static net.frozenblock.freezeframe.config.gui.FFConfigGuiHelper.booleanEntry;
import static net.frozenblock.freezeframe.config.gui.FFConfigGuiHelper.intSliderEntry;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public final class FFConfigGui {

	private static void setupEntries(ConfigCategory category, ConfigEntryBuilder builder) {
		category.addEntry(booleanEntry(builder, "use_as_world_icon", FFConfig.USE_LATEST_PHOTO_AS_WORLD_ICON));
		category.addEntry(booleanEntry(builder, "hide_film_photo_preview_and_info", FFConfig.HIDE_FILM_PHOTO_PREVIEW_AND_INFO));
		category.addEntry(booleanEntry(builder, "hide_photograph_preview", FFConfig.HIDE_PHOTOGRAPH_PREVIEW));
		category.addEntry(intSliderEntry(builder, "photograph_resolution", FFConfig.PHOTOGRAPH_RESOLUTION, 128, 1024));
		category.addEntry(
			FrozenClothConfig.syncedEntry(
				builder.startEnumSelector(text("photograph_format"), FFConfig.PhotographFormat.class, FFConfig.PHOTOGRAPH_FORMAT.get())
					.setEnumNameProvider(value -> enumNameProvider("photograph_format." + ((FFConfig.PhotographFormat)value).getSerializedName()))
					.setTooltip(tooltip("photograph_format")),
				FFConfig.PHOTOGRAPH_FORMAT
			)
		);
	}

	public static Screen buildScreen(Screen parent) {
		final ConfigBuilder configBuilder = ConfigBuilder.create().setParentScreen(parent).setTitle(text("component.title"));
		configBuilder.setSavingRunnable(FFConfig.CONFIG::save);

		final ConfigCategory category = configBuilder.getOrCreateCategory(text("config"));
		final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();
		setupEntries(category, entryBuilder);

		return configBuilder.build();
	}
}
