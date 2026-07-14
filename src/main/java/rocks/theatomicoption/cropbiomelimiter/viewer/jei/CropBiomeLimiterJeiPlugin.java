package rocks.theatomicoption.cropbiomelimiter.viewer.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.viewer.BiomeAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropBiomeAtlas;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.ClientAtlasData;

@JeiPlugin
public final class CropBiomeLimiterJeiPlugin implements IModPlugin {
	private static volatile IJeiRuntime runtime;
	private static volatile boolean atlasRegistered;
	public static final IRecipeType<CropAtlasPage> CROP_ATLAS = IRecipeType.create(
			CropBiomeLimiter.MOD_ID,
			"crop_atlas",
			CropAtlasPage.class
	);
	public static final IRecipeType<BiomeAtlasPage> BIOME_ATLAS = IRecipeType.create(
			CropBiomeLimiter.MOD_ID,
			"biome_atlas",
			BiomeAtlasPage.class
	);

	@Override
	public Identifier getPluginUid() {
		return CropBiomeLimiter.id("jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(
				new JeiCropAtlasCategory(guiHelper),
				new JeiBiomeAtlasCategory(guiHelper)
		);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addCraftingStation(BIOME_ATLAS, Items.COMPASS);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		CropBiomeAtlas atlas = ClientAtlasData.create();
		if (atlas.biomeEntries().isEmpty()) {
			return;
		}
		registration.addRecipes(CROP_ATLAS, CropAtlasPage.paginate(atlas.cropEntries()));
		registration.addRecipes(BIOME_ATLAS, BiomeAtlasPage.paginate(atlas.biomeEntries()));
		atlasRegistered = true;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
		if (Minecraft.getInstance().getConnection() != null) {
			refreshAfterJoin();
		}
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
		atlasRegistered = false;
	}

	public static synchronized void refreshAfterJoin() {
		IJeiRuntime currentRuntime = runtime;
		if (currentRuntime == null || atlasRegistered) {
			return;
		}
		CropBiomeAtlas atlas = ClientAtlasData.create();
		if (atlas.biomeEntries().isEmpty()) {
			return;
		}
		currentRuntime.getRecipeManager().addRecipes(CROP_ATLAS, CropAtlasPage.paginate(atlas.cropEntries()));
		currentRuntime.getRecipeManager().addRecipes(BIOME_ATLAS, BiomeAtlasPage.paginate(atlas.biomeEntries()));
		atlasRegistered = true;
	}
}
