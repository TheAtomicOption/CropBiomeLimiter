package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record CropBiomeLimiterConfig(
		GeneralOptions generalOptions,
		DimensionRules fallbackRules,
		Map<ResourceKey<Level>, DimensionRules> dimensionRules
) {
	public CropBehavior resolve(ResourceKey<Level> dimension, Identifier cropId, Holder<Biome> biome) {
		return rulesFor(dimension).resolve(cropId, biome);
	}

	public DimensionRules rulesFor(ResourceKey<Level> dimension) {
		return dimensionRules.getOrDefault(dimension, fallbackRules);
	}
}