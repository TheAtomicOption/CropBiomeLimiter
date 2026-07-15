package rocks.theatomicoption.cropbiomelimiter.viewer.rei;

import java.util.List;
import java.util.Optional;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasItemStacks;

public final class ReiCropAtlasDisplay extends BasicDisplay {
	public static final CategoryIdentifier<ReiCropAtlasDisplay> CATEGORY = CategoryIdentifier.of(
			CropBiomeLimiter.MOD_ID,
			"crop_atlas"
	);
	private final CropAtlasPage page;

	public ReiCropAtlasDisplay(CropAtlasPage page) {
		super(
				List.of(EntryIngredient.of(EntryStacks.of(AtlasItemStacks.crop(page.entry().cropId())))),
				List.of(),
				Optional.of(identifier(page))
		);
		this.page = page;
	}

	public CropAtlasPage page() {
		return page;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CATEGORY;
	}

	private static ResourceLocation identifier(CropAtlasPage page) {
		var entry = page.entry();
		return CropBiomeLimiter.id("crop_atlas/"
				+ entry.dimension().location().getNamespace() + "/"
				+ entry.dimension().location().getPath() + "/"
				+ entry.cropId().getNamespace() + "/"
				+ entry.cropId().getPath() + "/page/"
				+ (page.results().index() + 1));
	}
}
