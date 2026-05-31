package net.frozenblock.freezeframe.item.crafting.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.registry.FFDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public record FilmDyeFilterSlotDisplay(SlotDisplay dye, SlotDisplay target, boolean withExclusion) implements SlotDisplay {
	public static final MapCodec<FilmDyeFilterSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		SlotDisplay.CODEC.fieldOf("dye").forGetter(FilmDyeFilterSlotDisplay::dye),
		SlotDisplay.CODEC.fieldOf("target").forGetter(FilmDyeFilterSlotDisplay::target),
		Codec.BOOL.optionalFieldOf("with_exclusion", false).forGetter(FilmDyeFilterSlotDisplay::withExclusion)
	).apply(instance, FilmDyeFilterSlotDisplay::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FilmDyeFilterSlotDisplay> STREAM_CODEC = StreamCodec.composite(
		SlotDisplay.STREAM_CODEC, FilmDyeFilterSlotDisplay::dye,
		SlotDisplay.STREAM_CODEC, FilmDyeFilterSlotDisplay::target,
		ByteBufCodecs.BOOL, FilmDyeFilterSlotDisplay::withExclusion,
		FilmDyeFilterSlotDisplay::new
	);
	public static final SlotDisplay.Type<FilmDyeFilterSlotDisplay> TYPE = new SlotDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

	@Override
	public SlotDisplay.Type<FilmDyeFilterSlotDisplay> type() {
		return TYPE;
	}

	@Override
	public <T> Stream<T> resolve(final ContextMap context, final DisplayContentsFactory<T> factory) {
		final BinaryOperator<ItemStack> transformation = (target, dye) -> {
			final DyeColor dyeValue = dye.getOrDefault(DataComponents.DYE, DyeColor.WHITE);
			final FilmFilter filmFilter = FilmFilter.dyeDemo(dyeValue, this.withExclusion);

			final ItemStack finalTarget = target.copyWithCount(1);
			finalTarget.set(FFDataComponents.FILM_FILTER, filmFilter);
			return finalTarget;
		};
		return SlotDisplay.applyDemoTransformation(context, factory, this.target, this.dye, transformation);
	}
}
