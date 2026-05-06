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

package net.frozenblock.freezeframe.util.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.filter.SpecialFilmFilterDefinition;
import net.frozenblock.freezeframe.filter.SpecialFilmFilterRegistry;
import net.frozenblock.freezeframe.mixin.client.camera.GameRendererAccessor;
import net.frozenblock.freezeframe.mixin.client.camera.ShaderManagerAccessor;
import net.frozenblock.freezeframe.mixin.client.camera.ShaderManagerCompilationCacheAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ScopePostEffectController {
	private static final Identifier VERTEX_SHADER = FFConstants.vanillaId("core/screenquad");
	private static final Identifier BLIT_SHADER = FFConstants.id("post/copy");
	private static final Identifier SWAP_TARGET = FFConstants.id("film/scope_swap");
	private static final Identifier TEMP_TARGET = FFConstants.id("film/scope_temp");
	private static final PostChainConfig.InternalTarget TRANSIENT_TARGET = new PostChainConfig.InternalTarget(Optional.empty(), Optional.empty(), false, 0);

	@Nullable
	private static Identifier appliedEffect;

	private ScopePostEffectController() {
	}

	public static void applyFromFilter(Minecraft minecraft, FilmFilter filter) {
		if (minecraft.gameRenderer == null) return;
		final Identifier desired = getOrCreateEffect(minecraft, filter);
		if (desired == null) {
			clearIfApplied(minecraft);
			return;
		}
		if (desired.equals(appliedEffect) && desired.equals(minecraft.gameRenderer.currentPostEffect())) return;
		((GameRendererAccessor) minecraft.gameRenderer).freezeFrame$setPostEffect(desired);
		appliedEffect = desired;
	}

	public static void clearIfApplied(Minecraft minecraft) {
		if (appliedEffect == null || minecraft.gameRenderer == null) return;
		if (appliedEffect.equals(minecraft.gameRenderer.currentPostEffect())) {
			minecraft.gameRenderer.clearPostEffect();
		}
		appliedEffect = null;
	}

	@Nullable
	private static Identifier getOrCreateEffect(Minecraft minecraft, FilmFilter filter) {
		if (filter.isEmpty()) return null;
		final List<PassSpec> passSpecs = buildPassSpecs(filter);
		if (passSpecs.isEmpty()) return null;

		final Identifier effectId = FFConstants.id("film/dynamic/" + Integer.toUnsignedString(filter.layers().hashCode(), 16));
		if (ensurePostChainRegistered(minecraft, effectId, passSpecs)) return effectId;
		return null;
	}

	private static boolean ensurePostChainRegistered(Minecraft minecraft, Identifier effectId, List<PassSpec> passSpecs) {
		try {
			final ShaderManager shaderManager = minecraft.getShaderManager();
			final ShaderManagerAccessor shaderManagerAccessor = (ShaderManagerAccessor) shaderManager;
			final Object cache = getCompilationCache(shaderManager);
			if (cache == null) return false;

			final Map<Identifier, Optional<PostChain>> postChains = ((ShaderManagerCompilationCacheAccessor) cache).freezeFrame$getPostChains();
			final Optional<PostChain> cached = postChains.get(effectId);
			if (cached != null && cached.isPresent()) return true;

			final PostChain chain = PostChain.load(
				buildPostChainConfig(passSpecs),
				shaderManagerAccessor.freezeFrame$getTextureManager(),
				LevelTargetBundle.MAIN_TARGETS,
				effectId,
				shaderManagerAccessor.freezeFrame$getPostChainProjection(),
				shaderManagerAccessor.freezeFrame$getPostChainProjectionMatrixBuffer()
			);
			postChains.put(effectId, Optional.of(chain));
			return true;
		} catch (Exception exception) {
			FFConstants.error("Failed to build film scope post effect", exception);
			return false;
		}
	}

	@Nullable
	private static Object getCompilationCache(ShaderManager shaderManager) {
		try {
			final java.lang.reflect.Field field = ShaderManager.class.getDeclaredField("compilationCache");
			field.setAccessible(true);
			return field.get(shaderManager);
		} catch (ReflectiveOperationException exception) {
			FFConstants.error("Failed to access shader compilation cache for film scope post effect", exception);
			return null;
		}
	}

	private static PostChainConfig buildPostChainConfig(List<PassSpec> passSpecs) {
		final Map<Identifier, PostChainConfig.InternalTarget> targets = new LinkedHashMap<>();
		targets.put(SWAP_TARGET, TRANSIENT_TARGET);
		targets.put(TEMP_TARGET, TRANSIENT_TARGET);

		final List<PostChainConfig.Pass> passes = new ArrayList<>();
		Identifier inputTarget = PostChain.MAIN_TARGET_ID;
		Identifier outputTarget = SWAP_TARGET;
		for (PassSpec passSpec : passSpecs) {
			passes.add(pass(passSpec.shader(), inputTarget, outputTarget, passSpec.uniforms()));
			inputTarget = outputTarget;
			outputTarget = outputTarget.equals(SWAP_TARGET) ? TEMP_TARGET : SWAP_TARGET;
		}
		passes.add(pass(BLIT_SHADER, inputTarget, PostChain.MAIN_TARGET_ID, Map.of()));
		return new PostChainConfig(targets, passes);
	}

	private static PostChainConfig.Pass pass(Identifier fragmentShader, Identifier inputTarget, Identifier outputTarget, Map<String, List<UniformValue>> uniforms) {
		return new PostChainConfig.Pass(
			VERTEX_SHADER,
			fragmentShader,
			List.of(new PostChainConfig.TargetInput("In", inputTarget, false, false)),
			outputTarget,
			uniforms
		);
	}

	private static List<PassSpec> buildPassSpecs(FilmFilter filter) {
		final List<PassSpec> passSpecs = new ArrayList<>();
		for (FilmFilter.Layer layer : filter.layers()) {
			if (layer.isDye()) {
				passSpecs.add(dyePass(layer));
				continue;
			}

			final SpecialFilmFilterDefinition definition = SpecialFilmFilterRegistry.getById(layer.specialId());
			if (definition == null) continue;
			final Identifier shader = SpecialFilmFilterRegistry.shaderId(layer.specialId());
			if (shader == null) continue;
			passSpecs.add(specialPass(shader, definition));
		}
		return passSpecs;
	}

	private static PassSpec dyePass(FilmFilter.Layer layer) {
		final float red = ((layer.color() >> 16) & 0xFF) / 255F;
		final float green = ((layer.color() >> 8) & 0xFF) / 255F;
		final float blue = (layer.color() & 0xFF) / 255F;
		return new PassSpec(
			FFConstants.id("post/tint_dynamic"),
			Map.of("TintConfig", List.of(
				new UniformValue.Vec4Uniform(new Vector4f(red, green, blue, 1F)),
				new UniformValue.Vec4Uniform(new Vector4f(layer.exclusionTint() ? 1F : 0F, layer.exclusionTint() ? 1F : 0.4F, 0F, 0F))
			))
		);
	}

	private static PassSpec specialPass(Identifier shader, SpecialFilmFilterDefinition definition) {
		final Map<String, List<UniformValue>> configuredUniforms = configuredUniforms(definition);
		if (!configuredUniforms.isEmpty()) return new PassSpec(shader, configuredUniforms);

		return switch (definition.operation()) {
			case "crunchy" -> new PassSpec(shader, Map.of("CrunchConfig", List.of(new UniformValue.FloatUniform(2F), new UniformValue.FloatUniform(6F))));
			case "high_contrast" -> new PassSpec(shader, Map.of("ContrastConfig", List.of(new UniformValue.FloatUniform(1.35F), new UniformValue.FloatUniform(0F))));
			case "chromatic_aberration" -> new PassSpec(shader, Map.of("OffsetConfig", List.of(new UniformValue.FloatUniform(1F))));
			case "temperature_up" -> new PassSpec(shader, temperatureUniforms(0.18F, 0.02F, -0.12F));
			case "temperature_down" -> new PassSpec(shader, temperatureUniforms(-0.12F, 0.04F, 0.2F));
			case "sapped" -> new PassSpec(shader, tintShiftUniforms(0xEC7214, 0.68F, 1.18F, 0.88F));
			case "warding" -> new PassSpec(shader, tintShiftUniforms(0x29DFEB, 0.52F, 1.15F, 0.72F));
			case "triple_vision" -> new PassSpec(shader, Map.of("OffsetConfig", List.of(new UniformValue.FloatUniform(2.0F))));
			case "bloom" -> new PassSpec(shader, Map.of("BloomConfig", List.of(new UniformValue.FloatUniform(0.55F))));
			default -> new PassSpec(shader, Map.of());
		};
	}

	private static Map<String, List<UniformValue>> configuredUniforms(SpecialFilmFilterDefinition definition) {
		if (definition.uniforms().isEmpty()) return Map.of();
		final Map<String, List<UniformValue>> uniforms = new LinkedHashMap<>();
		for (Map.Entry<String, List<SpecialFilmFilterDefinition.ConfiguredUniform>> entry : definition.uniforms().entrySet()) {
			final List<UniformValue> values = new ArrayList<>();
			for (SpecialFilmFilterDefinition.ConfiguredUniform uniform : entry.getValue()) {
				final UniformValue value = configuredUniformValue(definition, uniform);
				if (value != null) values.add(value);
			}
			if (!values.isEmpty()) uniforms.put(entry.getKey(), List.copyOf(values));
		}
		return uniforms;
	}

	@Nullable
	private static UniformValue configuredUniformValue(SpecialFilmFilterDefinition definition, SpecialFilmFilterDefinition.ConfiguredUniform uniform) {
		return switch (uniform.type()) {
			case "float" -> uniform.values().isEmpty() ? null : new UniformValue.FloatUniform(uniform.values().getFirst());
			case "vec4" -> uniform.values().size() < 4 ? null : new UniformValue.Vec4Uniform(new Vector4f(uniform.values().get(0), uniform.values().get(1), uniform.values().get(2), uniform.values().get(3)));
			default -> {
				FFConstants.warn("Unsupported film filter uniform type '" + uniform.type() + "' in " + definition.id(), true);
				yield null;
			}
		};
	}

	private static Map<String, List<UniformValue>> temperatureUniforms(float red, float green, float blue) {
		return Map.of("TemperatureConfig", List.of(new UniformValue.Vec4Uniform(new Vector4f(red, green, blue, 0F))));
	}

	private static Map<String, List<UniformValue>> tintShiftUniforms(int color, float tintAmount, float contrastAmount, float saturationAmount) {
		final float red = ((color >> 16) & 0xFF) / 255F;
		final float green = ((color >> 8) & 0xFF) / 255F;
		final float blue = (color & 0xFF) / 255F;
		return Map.of("TintShiftConfig", List.of(
			new UniformValue.Vec4Uniform(new Vector4f(red, green, blue, 1F)),
			new UniformValue.Vec4Uniform(new Vector4f(tintAmount, contrastAmount, saturationAmount, 0F))
		));
	}

	private record PassSpec(Identifier shader, Map<String, List<UniformValue>> uniforms) {
	}
}
