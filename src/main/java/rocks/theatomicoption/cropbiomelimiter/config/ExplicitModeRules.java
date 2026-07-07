package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public record ExplicitModeRules(
		CropBehavior defaultBehavior,
		Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules
) implements DimensionRules {
	@Override
	public RuleMode mode() {
		return RuleMode.EXPLICIT;
	}

	@Override
	public CropBehavior resolve(Identifier cropId, Holder<Biome> biome) {
		Map<Identifier, CropBehavior> cropRules = cropBiomeRules.get(cropId);
		if (cropRules == null) {
			return defaultBehavior;
		}

		Identifier biomeId = biome.unwrapKey()
				.map(ResourceKey::identifier)
				.orElse(null);
		if (biomeId == null) {
			return defaultBehavior;
		}

		return cropRules.getOrDefault(biomeId, defaultBehavior);
	}
}