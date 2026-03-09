package net.lunade.camera.datagen.recipe;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.frozenblock.lib.recipe.api.RecipeExportNamespaceFix;
import net.lunade.camera.CameraPortConstants;
import net.lunade.camera.registry.CameraPortItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class CameraPortRecipeProvider extends FabricRecipeProvider {

	public CameraPortRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Contract("_, _ -> new")
	@Override
	protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput exporter) {
		return new RecipeProvider(registries, exporter) {
			@Override
			public void buildRecipes() {
				RecipeExportNamespaceFix.setCurrentGeneratingModId(CameraPortConstants.MOD_ID);

				this.shaped(RecipeCategory.TOOLS, CameraPortItems.CAMERA)
					.group("camera")
					.define('S', Ingredient.of(Items.STICK))
					.define('#', Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.PLANKS)))
					.define('A', Ingredient.of(Items.AMETHYST_SHARD))
					.pattern("S#S")
					.pattern("#A#")
					.pattern("S#S")
					.unlockedBy(RecipeProvider.getHasName(Items.AMETHYST_SHARD), this.has(Items.AMETHYST_SHARD))
					.save(exporter);

				this.shaped(RecipeCategory.TOOLS, CameraPortItems.DISC_CAMERA)
					.group("camera")
					.define('#', Ingredient.of(CameraPortItems.CAMERA))
					.define('X', Ingredient.of(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ConventionalItemTags.MUSIC_DISCS)))
					.pattern("#")
					.pattern("X")
					.unlockedBy(RecipeProvider.getHasName(CameraPortItems.DISC_CAMERA), this.has(CameraPortItems.DISC_CAMERA))
					.save(exporter);

				RecipeExportNamespaceFix.clearCurrentGeneratingModId();
			}
		};
	}

	@Override
	@NotNull
	public String getName() {
		return "Camera Port Recipes";
	}
}
