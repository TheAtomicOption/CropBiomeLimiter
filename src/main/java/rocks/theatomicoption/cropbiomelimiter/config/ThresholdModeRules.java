package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public record ThresholdModeRules(
		ThresholdCropRule defaultRule,
		Map<ResourceLocation, ThresholdCropRule> cropRules
) implements DimensionRules {
	@Override
	public RuleMode mode() {
		return RuleMode.THRESHOLD;
	}

	@Override
	public CropBehavior resolve(ResourceLocation cropId, Holder<Biome> biome) {
		ThresholdCropRule rule = cropRules.getOrDefault(cropId, defaultRule);
		return rule.resolve(biome.value());
	}
}