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

public final class PhotographDetailsUtil {
	public static final Component ORIGINAL_TOOLTIP = Component.translatable("photograph.original").withStyle(ChatFormatting.GRAY);
	public static final Component COPY_TOOLTIP = Component.translatable("photograph.copy").withStyle(ChatFormatting.GRAY);
	public static final Component COPY_OF_COPY_TOOLTIP = Component.translatable("photograph.copy_of_copy").withStyle(ChatFormatting.GRAY);

	public static List<Component> buildTooltipLines(Photograph photograph) {
		final List<Component> lines = new ArrayList<>(3);
		final Component name = getPhotographNameLine(photograph);
		if (name != null) lines.add(name);

		final Component photographer = getPhotographerLine(photograph);
		if (photographer != null) lines.add(photographer);

		final Component date = getDateLine(photograph);
		if (date != null) lines.add(date);

		final Component generation = getGenerationLine(photograph.generation(), true);
		if (generation != null) lines.add(generation);

		return lines;
	}

	public static Component getPhotographNameLine(Photograph photograph) {
		if (StringUtil.isNullOrEmpty(photograph.name())) return Component.translatable("item.freezeframe.photograph").withStyle(ChatFormatting.GRAY);
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

	@Nullable
	public static Component getGenerationLine(int generation, boolean ignoreOriginal) {
		if (generation <= 0 && ignoreOriginal) return null;
		return switch (generation) {
			case 0 -> ORIGINAL_TOOLTIP;
			case 1 -> COPY_TOOLTIP;
			default -> COPY_OF_COPY_TOOLTIP;
		};
	}
}
