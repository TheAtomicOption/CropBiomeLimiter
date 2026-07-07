package rocks.theatomicoption.cropbiomelimiter.config;

public record ClimateRule(
		float minTemperatureInclusive,
		float maxTemperatureExclusive,
		PrecipitationRequirement precipitation,
		CropBehavior behavior
) {
	public boolean matches(float baseTemperature, boolean hasPrecipitation) {
		return baseTemperature >= minTemperatureInclusive
				&& baseTemperature < maxTemperatureExclusive
				&& precipitation.matches(hasPrecipitation);
	}
}