package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.Collection;
import java.util.List;

public record CropAtlasPage(
		CropAtlasEntry entry,
		AtlasResultPage<AtlasBiome> results
) {
	public static List<CropAtlasPage> paginate(Collection<CropAtlasEntry> entries) {
		return entries.stream()
				.flatMap(entry -> AtlasResultPage.paginate(entry.biomesByBehavior()).stream()
						.map(results -> new CropAtlasPage(entry, results)))
				.toList();
	}
}
