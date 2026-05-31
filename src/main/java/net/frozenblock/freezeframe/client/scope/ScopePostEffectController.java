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

package net.frozenblock.freezeframe.client.scope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.freezeframe.FFConstants;
import net.frozenblock.freezeframe.component.FilmFilter;
import net.frozenblock.freezeframe.item.filter.SpecialFilmFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ScopePostEffectController {
	private static final Identifier VERTEX_SHADER = FFConstants.vanillaId("core/screenquad");
	private static final Identifier BLIT_SHADER = FFConstants.id("post/copy");
	private static final Identifier SWAP_TARGET = FFConstants.id("film/scope_swap");
	private static final Identifier TEMP_TARGET = FFConstants.id("film/scope_temp");
	private static final PostChainConfig.InternalTarget TRANSIENT_TARGET = new PostChainConfig.InternalTarget(Optional.empty(), Optional.empty(), false, 0);
	private static final Identifier DYE_PASS_SPEC_ID = FFConstants.id("post/tint_dynamic");
	private static final UniformValue.Vec4Uniform DYE_TINT_EXCLUSION = new UniformValue.Vec4Uniform(new Vector4f(1F, 1F, 0F, 0F));
	private static final UniformValue.Vec4Uniform DYE_TINT_NO_EXCLUSION = new UniformValue.Vec4Uniform(new Vector4f(0F, 0.4F, 0F, 0F));

	@Nullable
	private static Identifier appliedEffect;

	public static void applyFromFilter(Minecraft minecraft, FilmFilter filter) {
		if (minecraft.gameRenderer == null) return;

		final Identifier desired = getOrCreateEffect(minecraft, filter);
		if (desired == null) {
			clearIfApplied(minecraft);
			return;
		}
		if (desired.equals(appliedEffect) && desired.equals(minecraft.gameRenderer.currentPostEffect())) return;
		minecraft.gameRenderer.setPostEffect(desired);
		appliedEffect = desired;
	}

	public static void clearIfApplied(Minecraft minecraft) {
		if (appliedEffect == null || minecraft.gameRenderer == null) return;

		if (appliedEffect.equals(minecraft.gameRenderer.currentPostEffect())) minecraft.gameRenderer.clearPostEffect();
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
			final ShaderManager.CompilationCache cache = shaderManager.compilationCache;
			if (cache == null) return false;

			final Map<Identifier, Optional<PostChain>> postChains = cache.postChains;
			final Optional<PostChain> cached = postChains.get(effectId);
			if (cached != null && cached.isPresent()) return true;

			final PostChain chain = PostChain.load(
				buildPostChainConfig(passSpecs),
				shaderManager.textureManager,
				LevelTargetBundle.MAIN_TARGETS,
				effectId,
				shaderManager.postChainProjection,
				shaderManager.postChainProjectionMatrixBuffer
			);
			postChains.put(effectId, Optional.of(chain));
			return true;
		} catch (Exception exception) {
			FFConstants.error("Failed to build film scope post effect", exception);
			return false;
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

			if (layer.isSpecial()) {
				final Optional<Holder<SpecialFilmFilter>> specialFilmFilter = layer.specialFilmFilter();
				if (specialFilmFilter.isEmpty()) continue;

				passSpecs.add(specialPass(specialFilmFilter.get().value()));
				continue;
			}
		}
		return passSpecs;
	}

	private static PassSpec dyePass(FilmFilter.Layer layer) {
		final float red = ARGB.red(layer.color()) / 255F;
		final float green = ARGB.green(layer.color()) / 255F;
		final float blue = ARGB.blue(layer.color()) / 255F;
		return new PassSpec(
			DYE_PASS_SPEC_ID,
			Map.of("TintConfig", List.of(
				new UniformValue.Vec4Uniform(new Vector4f(red, green, blue, 1F)),
				layer.exclusionTint() ? DYE_TINT_EXCLUSION : DYE_TINT_NO_EXCLUSION
			))
		);
	}

	private static PassSpec specialPass(SpecialFilmFilter definition) {
		return new PassSpec(definition.shader(), (Map<String, List<UniformValue>>) definition.uniforms().orElse(Map.of()));
	}

	private record PassSpec(Identifier shader, Map<String, List<UniformValue>> uniforms) {}
}
