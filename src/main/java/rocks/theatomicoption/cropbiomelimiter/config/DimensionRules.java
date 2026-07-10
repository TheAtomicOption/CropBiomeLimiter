package rocks.theatomicoption.cropbiomelimiter.config;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public interface DimensionRules {
	RuleMode mode();

	CropBehavior resolve(ResourceLocation cropId, Holder<Biome> biome);
}