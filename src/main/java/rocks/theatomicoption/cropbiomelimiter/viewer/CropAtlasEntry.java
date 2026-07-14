package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.RuleMode;

public record CropAtlasEntry(
		ResourceKey<Level> dimension,
		Identifier cropId,
		RuleMode mode,
		List<ClimateRule> climateRules,
		TemperatureDomain temperatureDomain,
		Map<CropBehavior, List<AtlasBiome>> biomesByBehavior
) {
	public CropAtlasEntry {
		climateRules = climateRules == null ? List.of() : List.copyOf(climateRules);
		biomesByBehavior = copyGroups(biomesByBehavior);
	}

	private static Map<CropBehavior, List<AtlasBiome>> copyGroups(Map<CropBehavior, List<AtlasBiome>> source) {
		EnumMap<CropBehavior, List<AtlasBiome>> copy = new EnumMap<>(CropBehavior.class);
		for (CropBehavior behavior : CropBehavior.values()) {
			copy.put(behavior, List.copyOf(source.getOrDefault(behavior, List.of())));
		}
		return Map.copyOf(copy);
	}
}
