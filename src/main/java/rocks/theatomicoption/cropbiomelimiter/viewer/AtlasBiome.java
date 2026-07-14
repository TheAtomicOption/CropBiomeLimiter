package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record AtlasBiome(
		Identifier id,
		Holder<Biome> holder,
		Set<ResourceKey<Level>> vanillaDimensions
) {
	public AtlasBiome {
		vanillaDimensions = vanillaDimensions == null ? Set.of() : Set.copyOf(vanillaDimensions);
	}

	public float temperature() {
		return holder.value().getBaseTemperature();
	}

	public boolean hasPrecipitation() {
		return holder.value().hasPrecipitation();
	}

	public boolean belongsTo(ResourceKey<Level> dimension) {
		if (!Level.OVERWORLD.equals(dimension) && !Level.NETHER.equals(dimension) && !Level.END.equals(dimension)) {
			return true;
		}
		return vanillaDimensions.isEmpty() || vanillaDimensions.contains(dimension);
	}
}
