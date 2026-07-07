package rocks.theatomicoption.cropbiomelimiter.config;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public interface DimensionRules {
	RuleMode mode();

	CropBehavior resolve(Identifier cropId, Holder<Biome> biome);
}