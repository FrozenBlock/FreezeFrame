package net.lunade.camera.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record PhotographComponent(Identifier identifier) {
	// public record PictureComponent(ResourceLocation identifier, int day, int month, int year, int hour, int minute) {
	public static final Codec<PhotographComponent> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
			Identifier.CODEC.fieldOf("identifier").forGetter(component -> component.identifier)
                    /*
                    Codec.INT.fieldOf("day").forGetter(component -> component.day),
                    Codec.INT.fieldOf("month").forGetter(component -> component.month),
                    Codec.INT.fieldOf("year").forGetter(component -> component.year),
                    Codec.INT.fieldOf("hour").forGetter(component -> component.hour),
                    Codec.INT.fieldOf("minute").forGetter(component -> component.minute)
                     */
		)
		.apply(instance, PhotographComponent::new));

	public static final StreamCodec<ByteBuf, PhotographComponent> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC, PhotographComponent::identifier,
            /*
            ByteBufCodecs.VAR_INT, PictureComponent::day,
            ByteBufCodecs.VAR_INT, PictureComponent::month,
            ByteBufCodecs.VAR_INT, PictureComponent::year,
            ByteBufCodecs.VAR_INT, PictureComponent::hour,
            ByteBufCodecs.VAR_INT, PictureComponent::minute,
             */
		PhotographComponent::new
	);
}
