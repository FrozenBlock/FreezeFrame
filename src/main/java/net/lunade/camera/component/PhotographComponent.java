package net.lunade.camera.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record PhotographComponent(Identifier identifier, String photographer) {
	public static final Codec<PhotographComponent> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
			Identifier.CODEC.fieldOf("identifier").forGetter(component -> component.identifier),
			Codec.STRING.fieldOf("author").forGetter(PhotographComponent::photographer)
		)
		.apply(instance, PhotographComponent::new)
	);
	public static final StreamCodec<ByteBuf, PhotographComponent> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC, PhotographComponent::identifier,
		ByteBufCodecs.STRING_UTF8, PhotographComponent::photographer,
		PhotographComponent::new
	);
}
