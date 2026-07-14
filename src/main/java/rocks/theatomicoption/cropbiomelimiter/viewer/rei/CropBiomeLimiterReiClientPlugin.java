package rocks.theatomicoption.cropbiomelimiter.viewer.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.math.Rectangle;
import net.minecraft.world.item.Items;
import rocks.theatomicoption.cropbiomelimiter.viewer.BiomeAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropBiomeAtlas;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.ClientAtlasData;

public final class CropBiomeLimiterReiClientPlugin implements REIClientPlugin {
	/*
	 * REI 26 always creates a transfer widget for every display category and no
	 * longer allows its button area to be absent. Atlas pages have nothing to
	 * transfer, so keep REI's forced widget non-interactive and offscreen.
	 */
	private static final ButtonArea HIDDEN_TRANSFER_BUTTON = bounds -> new Rectangle(-10_000, -10_000, 0, 0);
	private static volatile boolean atlasRegistered;

	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.add(new ReiCropAtlasCategory(), configuration -> {
			configuration.setQuickCraftingEnabledByDefault(false);
			configuration.setPlusButtonArea(HIDDEN_TRANSFER_BUTTON);
		});
		registry.add(new ReiBiomeAtlasCategory(), configuration -> {
			configuration.setQuickCraftingEnabledByDefault(false);
			configuration.setPlusButtonArea(HIDDEN_TRANSFER_BUTTON);
		});
		registry.addWorkstations(ReiBiomeAtlasDisplay.CATEGORY, EntryStacks.of(Items.COMPASS));
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		CropBiomeAtlas atlas = ClientAtlasData.create();
		if (atlas.biomeEntries().isEmpty()) {
			return;
		}
		CropAtlasPage.paginate(atlas.cropEntries()).stream().map(ReiCropAtlasDisplay::new).forEach(registry::add);
		BiomeAtlasPage.paginate(atlas.biomeEntries()).stream().map(ReiBiomeAtlasDisplay::new).forEach(registry::add);
		atlasRegistered = true;
	}

	public static synchronized void refreshAfterJoin() {
		if (atlasRegistered) {
			return;
		}
		CropBiomeAtlas atlas = ClientAtlasData.create();
		if (atlas.biomeEntries().isEmpty()) {
			return;
		}
		DisplayRegistry registry = DisplayRegistry.getInstance();
		CropAtlasPage.paginate(atlas.cropEntries()).stream().map(ReiCropAtlasDisplay::new).forEach(registry::add);
		BiomeAtlasPage.paginate(atlas.biomeEntries()).stream().map(ReiBiomeAtlasDisplay::new).forEach(registry::add);
		atlasRegistered = true;
	}
}
