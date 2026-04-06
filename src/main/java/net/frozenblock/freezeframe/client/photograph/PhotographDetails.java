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

package net.frozenblock.freezeframe.client.photograph;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import net.frozenblock.freezeframe.component.Photograph;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;
import net.minecraft.ChatFormatting;

public final class PhotographDetails {
	private PhotographDetails() {
	}

	public static List<Component> buildTooltipLines(Photograph photograph) {
		final List<Component> lines = new ArrayList<>(3);
		final Component name = getPhotographNameLine(photograph);
		if (name != null) lines.add(name);

		final Component photographer = getPhotographerLine(photograph);
		if (photographer != null) lines.add(photographer);

		final Component date = getDateLine(photograph);
		if (date != null) lines.add(date);

		return lines;
	}

	@Nullable
	public static Component getPhotographNameLine(Photograph photograph) {
		if (StringUtil.isNullOrEmpty(photograph.name())) {
			return Component.translatable("item.camera_port.photograph").withStyle(ChatFormatting.GRAY);
		}
		return Component.literal(photograph.name()).withStyle(ChatFormatting.GRAY);
	}

	@Nullable
	public static Component getPhotographerLine(Photograph photograph) {
		if (StringUtil.isNullOrEmpty(photograph.photographer())) return null;
		return Component.translatable("photograph.photographer", photograph.photographer()).withStyle(ChatFormatting.GRAY);
	}

	@Nullable
	public static Component getDateLine(Photograph photograph) {
		final Optional<Date> optionalDate = PhotographLoader.parseDate(photograph.identifier().getPath());
		return optionalDate
			.map(date -> Component.translatable("photograph.date", formatDate(date)).withStyle(ChatFormatting.GRAY))
			.orElse(null);
	}

	private static String formatDate(Date date) {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
	}
}
