package rocks.theatomicoption.cropbiomelimiter.viewer.rei;

import java.util.List;
import java.util.Optional;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.Identifier;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.viewer.BiomeAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasItemStacks;

public final class ReiBiomeAtlasDisplay extends BasicDisplay {
	public static final CategoryIdentifier<ReiBiomeAtlasDisplay> CATEGORY = CategoryIdentifier.of(
			CropBiomeLimiter.MOD_ID,
			"biome_atlas"
	);
	private final BiomeAtlasPage page;

	public ReiBiomeAtlasDisplay(BiomeAtlasPage page) {
		super(
				List.of(EntryIngredient.of(EntryStacks.of(AtlasItemStacks.biome(
						page.entry().biome(),
						page.entry().temperatureDomain()
				)))),
				List.of(),
				Optional.of(identifier(page))
		);
		this.page = page;
	}

	public BiomeAtlasPage page() {
		return page;
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CATEGORY;
	}

	@Override
	public DisplaySerializer<? extends ReiBiomeAtlasDisplay> getSerializer() {
		return null;
	}

	private static Identifier identifier(BiomeAtlasPage page) {
		var entry = page.entry();
		return CropBiomeLimiter.id("biome_atlas/"
				+ entry.dimension().identifier().getNamespace() + "/"
				+ entry.dimension().identifier().getPath() + "/"
				+ entry.biome().id().getNamespace() + "/"
				+ entry.biome().id().getPath() + "/page/"
				+ (page.results().index() + 1));
	}
}
