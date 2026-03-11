package net.lunade.camera.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lunade.camera.item.FilmItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FilmFullness implements RangeSelectItemModelProperty {
	public static final MapCodec<FilmFullness> MAP_CODEC = MapCodec.unit(new FilmFullness());

	@Override
	public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		return FilmItem.getFullnessDisplay(stack);
	}

	@Override
	public MapCodec<FilmFullness> type() {
		return MAP_CODEC;
	}
}
