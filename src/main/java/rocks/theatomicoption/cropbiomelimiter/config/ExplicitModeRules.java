package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public record ExplicitModeRules(
		CropBehavior defaultBehavior,
		Map<ResourceLocation, Map<ResourceLocation, CropBehavior>> cropBiomeRules
) implements DimensionRules {
	@Override
	public RuleMode mode() {
		return RuleMode.EXPLICIT;
	}

	@Override
	public CropBehavior resolve(ResourceLocation cropId, Holder<Biome> biome) {
		Map<ResourceLocation, CropBehavior> cropRules = cropBiomeRules.get(cropId);
		if (cropRules == null) {
			return defaultBehavior;
		}

		ResourceLocation biomeId = biome.unwrapKey()
				.map(ResourceKey::location)
				.orElse(null);
		if (biomeId == null) {
			return defaultBehavior;
		}

		return cropRules.getOrDefault(biomeId, defaultBehavior);
	}
}