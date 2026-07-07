package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public record ThresholdModeRules(
		ThresholdCropRule defaultRule,
		Map<Identifier, ThresholdCropRule> cropRules
) implements DimensionRules {
	@Override
	public RuleMode mode() {
		return RuleMode.THRESHOLD;
	}

	@Override
	public CropBehavior resolve(Identifier cropId, Holder<Biome> biome) {
		ThresholdCropRule rule = cropRules.getOrDefault(cropId, defaultRule);
		return rule.resolve(biome.value());
	}
}