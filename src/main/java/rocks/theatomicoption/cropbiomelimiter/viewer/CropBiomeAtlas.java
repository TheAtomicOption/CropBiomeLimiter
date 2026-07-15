package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfig;
import rocks.theatomicoption.cropbiomelimiter.config.DimensionRules;
import rocks.theatomicoption.cropbiomelimiter.config.ThresholdModeRules;

public record CropBiomeAtlas(
		List<CropAtlasEntry> cropEntries,
		List<BiomeAtlasEntry> biomeEntries
) {
	public CropBiomeAtlas {
		cropEntries = List.copyOf(cropEntries);
		biomeEntries = List.copyOf(biomeEntries);
	}

	public static CropBiomeAtlas build(
			CropBiomeLimiterConfig config,
			Collection<ResourceKey<Level>> liveDimensions,
			Collection<ResourceLocation> cropIds,
			Collection<AtlasBiome> biomes
	) {
		List<ResourceKey<Level>> dimensions = dimensions(config, liveDimensions);
		List<ResourceLocation> crops = cropIds.stream()
				.filter(id -> !config.generalOptions().isExcluded(id))
				.distinct()
				.sorted(Comparator.comparing(ResourceLocation::toString))
				.toList();
		List<AtlasBiome> sortedBiomes = biomes.stream()
				.sorted(Comparator.comparing(biome -> biome.id().toString()))
				.toList();
		TemperatureDomain temperatureDomain = TemperatureDomain.from(sortedBiomes);

		List<CropAtlasEntry> cropEntries = new ArrayList<>();
		List<BiomeAtlasEntry> biomeEntries = new ArrayList<>();
		for (ResourceKey<Level> dimension : dimensions) {
			DimensionRules rules = config.rulesFor(dimension);
			List<AtlasBiome> dimensionBiomes = sortedBiomes.stream()
					.filter(biome -> biome.belongsTo(dimension))
					.toList();
			for (ResourceLocation cropId : crops) {
				cropEntries.add(cropEntry(dimension, cropId, rules, dimensionBiomes, temperatureDomain));
			}
			for (AtlasBiome biome : dimensionBiomes) {
				biomeEntries.add(biomeEntry(dimension, biome, rules, crops, temperatureDomain));
			}
		}
		return new CropBiomeAtlas(cropEntries, biomeEntries);
	}

	private static List<ResourceKey<Level>> dimensions(
			CropBiomeLimiterConfig config,
			Collection<ResourceKey<Level>> liveDimensions
	) {
		Set<ResourceKey<Level>> dimensions = new LinkedHashSet<>();
		if (liveDimensions != null) {
			dimensions.addAll(liveDimensions);
		}
		dimensions.addAll(config.dimensionRules().keySet());
		if (dimensions.isEmpty()) {
			dimensions.add(Level.OVERWORLD);
		}
		return dimensions.stream()
				.sorted(Comparator.comparing(key -> key.location().toString()))
				.toList();
	}

	private static CropAtlasEntry cropEntry(
			ResourceKey<Level> dimension,
			ResourceLocation cropId,
			DimensionRules rules,
			List<AtlasBiome> biomes,
			TemperatureDomain temperatureDomain
	) {
		EnumMap<CropBehavior, List<AtlasBiome>> groups = emptyGroups();
		for (AtlasBiome biome : biomes) {
			groups.get(rules.resolve(cropId, biome.holder())).add(biome);
		}
		return new CropAtlasEntry(
				dimension,
				cropId,
				rules.mode(),
				climateRules(rules, cropId),
				temperatureDomain,
				groups
		);
	}

	private static BiomeAtlasEntry biomeEntry(
			ResourceKey<Level> dimension,
			AtlasBiome biome,
			DimensionRules rules,
			List<ResourceLocation> cropIds,
			TemperatureDomain temperatureDomain
	) {
		EnumMap<CropBehavior, List<ResourceLocation>> groups = emptyGroups();
		for (ResourceLocation cropId : cropIds) {
			groups.get(rules.resolve(cropId, biome.holder())).add(cropId);
		}
		return new BiomeAtlasEntry(dimension, biome, rules.mode(), temperatureDomain, groups);
	}

	private static List<ClimateRule> climateRules(DimensionRules rules, ResourceLocation cropId) {
		if (rules instanceof ThresholdModeRules thresholdRules) {
			return thresholdRules.cropRules().getOrDefault(cropId, thresholdRules.defaultRule()).climateRules();
		}
		return List.of();
	}

	private static <T> EnumMap<CropBehavior, List<T>> emptyGroups() {
		EnumMap<CropBehavior, List<T>> groups = new EnumMap<>(CropBehavior.class);
		for (CropBehavior behavior : CropBehavior.values()) {
			groups.put(behavior, new ArrayList<>());
		}
		return groups;
	}

}
