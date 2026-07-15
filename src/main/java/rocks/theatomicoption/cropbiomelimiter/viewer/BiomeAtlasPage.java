package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.Collection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public record BiomeAtlasPage(
		BiomeAtlasEntry entry,
		AtlasResultPage<ResourceLocation> results
) {
	public static List<BiomeAtlasPage> paginate(Collection<BiomeAtlasEntry> entries) {
		return entries.stream()
				.flatMap(entry -> AtlasResultPage.paginate(entry.cropsByBehavior()).stream()
						.map(results -> new BiomeAtlasPage(entry, results)))
				.toList();
	}
}
