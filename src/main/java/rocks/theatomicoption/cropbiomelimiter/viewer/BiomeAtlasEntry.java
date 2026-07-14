package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.RuleMode;

public record BiomeAtlasEntry(
		ResourceKey<Level> dimension,
		AtlasBiome biome,
		RuleMode mode,
		TemperatureDomain temperatureDomain,
		Map<CropBehavior, List<Identifier>> cropsByBehavior
) {
	public BiomeAtlasEntry {
		cropsByBehavior = copyGroups(cropsByBehavior);
	}

	private static Map<CropBehavior, List<Identifier>> copyGroups(Map<CropBehavior, List<Identifier>> source) {
		EnumMap<CropBehavior, List<Identifier>> copy = new EnumMap<>(CropBehavior.class);
		for (CropBehavior behavior : CropBehavior.values()) {
			copy.put(behavior, List.copyOf(source.getOrDefault(behavior, List.of())));
		}
		return Map.copyOf(copy);
	}
}
