package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.List;

import net.minecraft.world.level.biome.Biome;

public record ThresholdCropRule(CropBehavior defaultBehavior, List<ClimateRule> climateRules) {
	public CropBehavior resolve(Biome biome) {
		float baseTemperature = biome.getBaseTemperature();
		boolean hasPrecipitation = biome.hasPrecipitation();

		for (ClimateRule rule : climateRules) {
			if (rule.matches(baseTemperature, hasPrecipitation)) {
				return rule.behavior();
			}
		}
		return defaultBehavior;
	}
}