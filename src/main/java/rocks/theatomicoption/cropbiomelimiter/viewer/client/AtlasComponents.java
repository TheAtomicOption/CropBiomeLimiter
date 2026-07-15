package rocks.theatomicoption.cropbiomelimiter.viewer.client;

import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.PrecipitationRequirement;
import rocks.theatomicoption.cropbiomelimiter.viewer.AtlasBiome;
import rocks.theatomicoption.cropbiomelimiter.viewer.TemperatureDomain;

public final class AtlasComponents {
	private AtlasComponents() {
	}

	public static Component biomeName(ResourceLocation id) {
		return Component.translatableWithFallback(id.toLanguageKey("biome"), humanize(id.getPath()));
	}

	public static Component cropName(ResourceLocation id) {
		return Component.translatableWithFallback(id.toLanguageKey("block"), humanize(id.getPath()));
	}

	public static Component dimensionName(ResourceKey<Level> dimension) {
		ResourceLocation id = dimension.location();
		return Component.translatableWithFallback(id.toLanguageKey("dimension"), humanize(id.getPath()));
	}

	public static Component behavior(CropBehavior behavior) {
		return Component.translatable("schema.cropbiomelimiter.behavior." + behavior.name().toLowerCase(Locale.ROOT));
	}

	public static Component behaviorWithCount(CropBehavior behavior, int count) {
		return Component.translatable("viewer.cropbiomelimiter.behavior_count", behavior(behavior), count);
	}

	public static Component temperatureValue(float temperature) {
		return Component.literal(formatTemperature(temperature));
	}

	public static Component precipitationValue(boolean hasPrecipitation) {
		return Component.translatable(hasPrecipitation
				? "viewer.cropbiomelimiter.precipitation_yes"
				: "viewer.cropbiomelimiter.precipitation_no");
	}

	public static List<Component> biomeLore(AtlasBiome biome, TemperatureDomain temperatureDomain) {
		return List.of(
				lore(Component.literal(biome.id().toString()), ChatFormatting.DARK_GRAY),
				lore(Component.translatable(
						"viewer.cropbiomelimiter.temperature",
						temperatureValue(biome.temperature())
				), TemperatureScale.textColor(biome.temperature(), temperatureDomain)),
				lore(Component.translatable(
						"viewer.cropbiomelimiter.precipitation",
						precipitationValue(biome.hasPrecipitation())
				), biome.hasPrecipitation() ? ChatFormatting.AQUA : ChatFormatting.GRAY)
		);
	}

	public static Component temperatureSummary(List<ClimateRule> rules) {
		if (rules.isEmpty()) {
			return Component.translatable("viewer.cropbiomelimiter.no_climate_ranges");
		}
		if (rules.size() == 1) {
			ClimateRule rule = rules.getFirst();
			return Component.translatable(
					"viewer.cropbiomelimiter.temperature_range",
					formatTemperature(rule.minTemperatureInclusive()),
					formatTemperature(rule.maxTemperatureExclusive())
			);
		}
		return Component.translatable("viewer.cropbiomelimiter.temperature_ranges", rules.size());
	}

	public static Component precipitationSummary(List<ClimateRule> rules) {
		if (rules.isEmpty()) {
			return precipitation(PrecipitationRequirement.IGNORED);
		}
		PrecipitationRequirement first = rules.getFirst().precipitation();
		if (rules.stream().allMatch(rule -> rule.precipitation() == first)) {
			return precipitation(first);
		}
		return Component.translatable("viewer.cropbiomelimiter.precipitation_mixed");
	}

	public static Component climateRule(ClimateRule rule) {
		return Component.translatable(
				"viewer.cropbiomelimiter.climate_rule",
				formatTemperature(rule.minTemperatureInclusive()),
				formatTemperature(rule.maxTemperatureExclusive()),
				precipitation(rule.precipitation()),
				behavior(rule.behavior())
		);
	}

	public static Component precipitation(PrecipitationRequirement requirement) {
		return Component.translatable("viewer.cropbiomelimiter.precipitation_" + requirement.serializedName());
	}

	private static String formatTemperature(float temperature) {
		return String.format(Locale.ROOT, "%.2f", temperature);
	}

	private static Component lore(Component component, ChatFormatting color) {
		return component.copy().withStyle(style -> style.withColor(color).withItalic(false));
	}

	private static String humanize(String path) {
		String[] words = path.split("_");
		StringBuilder name = new StringBuilder();
		for (String word : words) {
			if (!name.isEmpty()) {
				name.append(' ');
			}
			if (!word.isEmpty()) {
				name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
			}
		}
		return name.toString();
	}
}
