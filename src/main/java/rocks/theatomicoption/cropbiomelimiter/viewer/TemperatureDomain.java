package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.Collection;

public record TemperatureDomain(float minimum, float maximum) {
	private static final float DEFAULT_MINIMUM = -0.7F;
	private static final float DEFAULT_MAXIMUM = 2.0F;
	private static final float SINGLE_VALUE_PADDING = 1.0F;

	public TemperatureDomain {
		if (!Float.isFinite(minimum) || !Float.isFinite(maximum) || minimum >= maximum) {
			throw new IllegalArgumentException("Temperature domain must have finite, increasing bounds");
		}
	}

	public static TemperatureDomain from(Collection<AtlasBiome> biomes) {
		float minimum = Float.POSITIVE_INFINITY;
		float maximum = Float.NEGATIVE_INFINITY;
		if (biomes != null) {
			for (AtlasBiome biome : biomes) {
				float temperature = biome.temperature();
				if (Float.isFinite(temperature)) {
					minimum = Math.min(minimum, temperature);
					maximum = Math.max(maximum, temperature);
				}
			}
		}
		if (!Float.isFinite(minimum) || !Float.isFinite(maximum)) {
			return new TemperatureDomain(DEFAULT_MINIMUM, DEFAULT_MAXIMUM);
		}
		if (minimum == maximum) {
			return new TemperatureDomain(minimum - SINGLE_VALUE_PADDING, maximum + SINGLE_VALUE_PADDING);
		}
		return new TemperatureDomain(minimum, maximum);
	}

	public float position(float temperature) {
		if (!Float.isFinite(temperature)) {
			return 0.5F;
		}
		return Math.max(0.0F, Math.min(1.0F, (temperature - minimum) / (maximum - minimum)));
	}
}
