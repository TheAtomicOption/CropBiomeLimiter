package rocks.theatomicoption.cropbiomelimiter.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class CropBiomeLimiterConfigLoader {
	public static final String CONFIG_DIRECTORY_NAME = ConfigAppSchema.CONFIG_DIRECTORY_NAME;
	public static final String GENERAL_FILE_NAME = ConfigAppSchema.GENERAL_FILE_NAME;
	public static final String EXPLICIT_MODE_FILE_NAME = ConfigAppSchema.EXPLICIT_MODE_FILE_NAME;
	public static final String THRESHOLD_MODE_FILE_NAME = ConfigAppSchema.THRESHOLD_MODE_FILE_NAME;

	private static final int SCHEMA_VERSION = ConfigAppSchema.SCHEMA_VERSION;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final float DEFAULT_MIN_TEMPERATURE = -1000.0F;
	private static final float DEFAULT_MAX_TEMPERATURE = 1000.0F;

	private CropBiomeLimiterConfigLoader() {
	}

	public static ConfigLoadResult tryLoadConfig(Path minecraftConfigDirectory) {
		List<String> diagnostics = new ArrayList<>();
		CropBiomeLimiterConfig fallback = tryCreateDefaultConfig(diagnostics);
		Path directory = configDirectory(minecraftConfigDirectory);
		Path generalPath = directory.resolve(GENERAL_FILE_NAME);
		Path explicitPath = directory.resolve(EXPLICIT_MODE_FILE_NAME);
		Path thresholdPath = directory.resolve(THRESHOLD_MODE_FILE_NAME);
		tryCreateDirectory(directory, diagnostics);

		GeneralFile defaultGeneral = generalFile(fallback);
		ExplicitModeFile defaultExplicit = explicitModeFile(fallback);
		ThresholdModeFile defaultThreshold = thresholdModeFile(fallback);

		boolean hasGeneral = fileExists(generalPath, diagnostics);
		boolean hasExplicit = fileExists(explicitPath, diagnostics);
		boolean hasThreshold = fileExists(thresholdPath, diagnostics);
		List<String> createdDefaultFiles = new ArrayList<>();

		if (!hasGeneral && tryWriteConfig(generalPath, toJson(defaultGeneral), diagnostics)) {
			createdDefaultFiles.add(GENERAL_FILE_NAME);
		}
		if (!hasExplicit && tryWriteConfig(explicitPath, toJson(defaultExplicit), diagnostics)) {
			createdDefaultFiles.add(EXPLICIT_MODE_FILE_NAME);
		}
		if (!hasThreshold && tryWriteConfig(thresholdPath, toJson(defaultThreshold), diagnostics)) {
			createdDefaultFiles.add(THRESHOLD_MODE_FILE_NAME);
		}

		GeneralFile general = hasGeneral
				? readObjectFile(generalPath, "general config", diagnostics).map(root -> readGeneral(root, defaultGeneral, diagnostics)).orElse(defaultGeneral)
				: defaultGeneral;
		ExplicitModeFile explicit = hasExplicit
				? readObjectFile(explicitPath, "Explicit mode config", diagnostics).map(root -> readExplicitFile(root, defaultExplicit, diagnostics)).orElse(defaultExplicit)
				: defaultExplicit;
		ThresholdModeFile threshold = hasThreshold
				? readObjectFile(thresholdPath, "Threshold mode config", diagnostics).map(root -> readThresholdFile(root, defaultThreshold, diagnostics)).orElse(defaultThreshold)
				: defaultThreshold;

		return new ConfigLoadResult(toConfig(general, explicit, threshold), directory, diagnostics, createdDefaultFiles);
	}

	public static boolean tryWriteDefaultConfig(Path minecraftConfigDirectory) {
		List<String> diagnostics = new ArrayList<>();
		CropBiomeLimiterConfig defaults = tryCreateDefaultConfig(diagnostics);
		Path directory = configDirectory(minecraftConfigDirectory);
		tryCreateDirectory(directory, diagnostics);
		boolean wroteGeneral = tryWriteConfig(directory.resolve(GENERAL_FILE_NAME), toJson(generalFile(defaults)), diagnostics);
		boolean wroteExplicit = tryWriteConfig(directory.resolve(EXPLICIT_MODE_FILE_NAME), toJson(explicitModeFile(defaults)), diagnostics);
		boolean wroteThreshold = tryWriteConfig(directory.resolve(THRESHOLD_MODE_FILE_NAME), toJson(thresholdModeFile(defaults)), diagnostics);
		return wroteGeneral && wroteExplicit && wroteThreshold;
	}

	public static JsonObject generalJson(CropBiomeLimiterConfig config) {
		return toJson(generalFile(config));
	}

	public static JsonObject explicitModeJson(CropBiomeLimiterConfig config) {
		return toJson(explicitModeFile(config));
	}

	public static JsonObject thresholdModeJson(CropBiomeLimiterConfig config) {
		return toJson(thresholdModeFile(config));
	}

	private static CropBiomeLimiterConfig toConfig(GeneralFile general, ExplicitModeFile explicit, ThresholdModeFile threshold) {
		DimensionRules fallbackRules = switch (general.fallbackMode()) {
			case EXPLICIT -> explicit.fallback();
			case THRESHOLD -> threshold.fallback();
		};
		Map<ResourceKey<Level>, DimensionRules> dimensions = new LinkedHashMap<>();
		general.dimensionModes().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> dimensions.put(entry.getKey(), rulesFor(entry.getKey(), entry.getValue(), explicit, threshold)));
		return new CropBiomeLimiterConfig(general.options(), fallbackRules, Map.copyOf(dimensions));
	}

	private static DimensionRules rulesFor(ResourceKey<Level> dimension, RuleMode mode, ExplicitModeFile explicit, ThresholdModeFile threshold) {
		return switch (mode) {
			case EXPLICIT -> explicit.dimensions().getOrDefault(dimension, explicit.fallback());
			case THRESHOLD -> threshold.dimensions().getOrDefault(dimension, threshold.fallback());
		};
	}

	private static Path configDirectory(Path minecraftConfigDirectory) {
		return minecraftConfigDirectory == null ? Path.of(CONFIG_DIRECTORY_NAME) : minecraftConfigDirectory.resolve(CONFIG_DIRECTORY_NAME);
	}

	private static CropBiomeLimiterConfig tryCreateDefaultConfig(List<String> diagnostics) {
		try {
			return DefaultCropBiomeConfig.create();
		} catch (RuntimeException exception) {
			diagnostics.add("Default config construction failed; using minimal permissive fallback.");
			CropBiomeLimiter.LOGGER.error("Crop Biome Limiter could not construct its default config.", exception);
			ThresholdCropRule permissiveRule = new ThresholdCropRule(CropBehavior.GROWABLE, List.of());
			ThresholdModeRules permissiveRules = new ThresholdModeRules(permissiveRule, Map.of());
			return new CropBiomeLimiterConfig(GeneralOptions.defaults(), permissiveRules, Map.of());
		}
	}

	private static void tryCreateDirectory(Path path, List<String> diagnostics) {
		try {
			Files.createDirectories(path);
		} catch (IOException | RuntimeException exception) {
			diagnostics.add("Could not create config directory " + path + "; using in-memory defaults.");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not create its config directory: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter config directory creation failure details.", exception);
		}
	}

	private static boolean fileExists(Path path, List<String> diagnostics) {
		try {
			return Files.exists(path);
		} catch (RuntimeException exception) {
			diagnostics.add("Could not inspect config file " + path + "; using defaults.");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not inspect its config file: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter config inspection failure details.", exception);
			return false;
		}
	}

	private static Optional<JsonObject> readObjectFile(Path path, String description, List<String> diagnostics) {
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonElement root = JsonParser.parseReader(reader);
			Optional<JsonObject> object = asObject(root);
			if (object.isEmpty()) {
				diagnostics.add(description + " root must be a JSON object; using defaults for that file.");
			}
			return object;
		} catch (IOException | RuntimeException exception) {
			diagnostics.add("Could not read or parse " + path + "; using defaults for that file.");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not read {}. Defaults will be used for that file: {}", path, exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter config read failure details.", exception);
			return Optional.empty();
		}
	}

	private static boolean tryWriteConfig(Path path, JsonObject object, List<String> diagnostics) {
		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(object, writer);
			}
			return true;
		} catch (IOException | RuntimeException exception) {
			diagnostics.add("Could not write default config to " + path + ".");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not write its default config file: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter default config write failure details.", exception);
			return false;
		}
	}

	private static GeneralFile generalFile(CropBiomeLimiterConfig config) {
		Map<ResourceKey<Level>, RuleMode> dimensions = new LinkedHashMap<>();
		config.dimensionRules().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> dimensions.put(entry.getKey(), entry.getValue().mode()));
		return new GeneralFile(config.generalOptions(), config.fallbackRules().mode(), Map.copyOf(dimensions));
	}

	private static ExplicitModeFile explicitModeFile(CropBiomeLimiterConfig config) {
		ExplicitModeRules fallback = explicitRulesFor(config.fallbackRules());
		Map<ResourceKey<Level>, ExplicitModeRules> dimensions = new LinkedHashMap<>();
		config.dimensionRules().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> dimensions.put(entry.getKey(), explicitRulesFor(entry.getValue())));
		return new ExplicitModeFile(fallback, Map.copyOf(dimensions));
	}

	private static ExplicitModeRules explicitRulesFor(DimensionRules rules) {
		if (rules instanceof ExplicitModeRules explicitModeRules) {
			return explicitModeRules;
		}
		if (rules instanceof ThresholdModeRules thresholdModeRules) {
			return explicitRulesFromThreshold(thresholdModeRules);
		}
		return new ExplicitModeRules(defaultBehavior(rules), Map.of());
	}

	private static ExplicitModeRules explicitRulesFromThreshold(ThresholdModeRules rules) {
		CropBehavior defaultBehavior = rules.defaultRule().defaultBehavior();
		Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules = new LinkedHashMap<>();
		rules.cropRules().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
				.forEach(entry -> addExplicitCropRules(entry.getKey(), entry.getValue(), defaultBehavior, cropBiomeRules));
		return new ExplicitModeRules(defaultBehavior, immutableCropBiomeRules(cropBiomeRules));
	}

	private static void addExplicitCropRules(Identifier cropId, ThresholdCropRule thresholdRule, CropBehavior explicitDefault, Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules) {
		Map<Identifier, CropBehavior> biomeRules = new LinkedHashMap<>();
		for (BiomeClimate biome : defaultBiomeClimates()) {
			CropBehavior behavior = resolveThresholdRule(thresholdRule, biome);
			if (behavior != explicitDefault) {
				biomeRules.put(biome.id(), behavior);
			}
		}
		if (!biomeRules.isEmpty()) {
			cropBiomeRules.put(cropId, biomeRules);
		}
	}

	private static CropBehavior resolveThresholdRule(ThresholdCropRule thresholdRule, BiomeClimate biome) {
		for (ClimateRule climateRule : thresholdRule.climateRules()) {
			if (climateRule.matches(biome.temperature(), biome.hasPrecipitation())) {
				return climateRule.behavior();
			}
		}
		return thresholdRule.defaultBehavior();
	}

	private static ThresholdModeFile thresholdModeFile(CropBiomeLimiterConfig config) {
		ThresholdModeRules fallback = config.fallbackRules() instanceof ThresholdModeRules rules
				? rules
				: new ThresholdModeRules(new ThresholdCropRule(defaultBehavior(config.fallbackRules()), List.of()), Map.of());
		Map<ResourceKey<Level>, ThresholdModeRules> dimensions = new LinkedHashMap<>();
		config.dimensionRules().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.filter(entry -> entry.getValue() instanceof ThresholdModeRules)
				.forEach(entry -> dimensions.put(entry.getKey(), (ThresholdModeRules) entry.getValue()));
		return new ThresholdModeFile(fallback, Map.copyOf(dimensions));
	}

	private static GeneralFile readGeneral(JsonObject object, GeneralFile fallback, List<String> diagnostics) {
		GeneralOptions options = readGeneralOptions(object, fallback.options(), diagnostics);
		RuleMode fallbackMode = modeMember(object, "fallback_mode", fallback.fallbackMode(), "general.fallback_mode", diagnostics);
		Map<ResourceKey<Level>, RuleMode> dimensionModes = readDimensionModes(object.get("dimensions"), fallback.dimensionModes(), diagnostics);
		return new GeneralFile(options, fallbackMode, Map.copyOf(dimensionModes));
	}

	private static ExplicitModeFile readExplicitFile(JsonObject object, ExplicitModeFile fallback, List<String> diagnostics) {
		ExplicitModeRules fallbackRules = objectMember(object, "fallback")
				.map(fallbackObject -> readExplicitRules(fallbackObject, fallback.fallback(), "explicit.fallback", diagnostics))
				.orElse(fallback.fallback());
		Map<ResourceKey<Level>, ExplicitModeRules> dimensions = new LinkedHashMap<>(fallback.dimensions());
		objectMember(object, "dimensions").ifPresent(dimensionObject -> dimensionObject.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> readExplicitDimension(entry, dimensions, fallbackRules, diagnostics)));
		return new ExplicitModeFile(fallbackRules, Map.copyOf(dimensions));
	}

	private static ThresholdModeFile readThresholdFile(JsonObject object, ThresholdModeFile fallback, List<String> diagnostics) {
		ThresholdModeRules fallbackRules = objectMember(object, "fallback")
				.map(fallbackObject -> readThresholdRules(fallbackObject, fallback.fallback(), "threshold.fallback", diagnostics))
				.orElse(fallback.fallback());
		Map<ResourceKey<Level>, ThresholdModeRules> dimensions = new LinkedHashMap<>(fallback.dimensions());
		objectMember(object, "dimensions").ifPresent(dimensionObject -> dimensionObject.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> readThresholdDimension(entry, dimensions, fallbackRules, diagnostics)));
		return new ThresholdModeFile(fallbackRules, Map.copyOf(dimensions));
	}

	private static GeneralOptions readGeneralOptions(JsonObject object, GeneralOptions fallback, List<String> diagnostics) {
		boolean affectsBonemeal = booleanMember(object, "affects_bonemeal", fallback.affectsBonemeal(), "general.affects_bonemeal", diagnostics);
		boolean affectsBlockPlacement = booleanMember(object, "affects_block_placement", fallback.affectsBlockPlacement(), "general.affects_block_placement", diagnostics);
		boolean affectsVillageFarmGeneration = booleanMember(object, "affects_village_farm_generation", fallback.affectsVillageFarmGeneration(), "general.affects_village_farm_generation", diagnostics);
		boolean chatInfo = booleanMember(object, "chat_info", fallback.chatInfo(), "general.chat_info", diagnostics);
		Set<Identifier> excludedBlocks = identifiers(object.get("excluded_blocks"), fallback.excludedBlocks(), "general.excluded_blocks", diagnostics);
		return new GeneralOptions(affectsBonemeal, affectsBlockPlacement, affectsVillageFarmGeneration, chatInfo, excludedBlocks);
	}

	private static Map<ResourceKey<Level>, RuleMode> readDimensionModes(JsonElement element, Map<ResourceKey<Level>, RuleMode> fallback, List<String> diagnostics) {
		if (element == null || element.isJsonNull()) {
			return fallback;
		}
		Optional<JsonObject> object = asObject(element);
		if (object.isEmpty()) {
			diagnostics.add("general.dimensions must be an object of dimension ids to mode names; using default dimension modes.");
			return fallback;
		}
		Map<ResourceKey<Level>, RuleMode> dimensions = new LinkedHashMap<>(fallback);
		object.get().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> readDimensionMode(entry, dimensions, diagnostics));
		return dimensions;
	}

	private static void readDimensionMode(Map.Entry<String, JsonElement> entry, Map<ResourceKey<Level>, RuleMode> dimensions, List<String> diagnostics) {
		Optional<ResourceKey<Level>> dimension = dimensionKey(entry.getKey(), "general.dimensions", diagnostics);
		if (dimension.isEmpty()) {
			return;
		}
		String value = string(entry.getValue(), null, "general.dimensions." + entry.getKey(), diagnostics);
		if (value == null) {
			return;
		}
		RuleMode.tryFromSerializedName(value).ifPresentOrElse(
				mode -> dimensions.put(dimension.get(), mode),
				() -> diagnostics.add("general.dimensions." + entry.getKey() + " has unknown mode '" + value + "'; leaving default mode unchanged.")
		);
	}

	private static void readExplicitDimension(Map.Entry<String, JsonElement> entry, Map<ResourceKey<Level>, ExplicitModeRules> dimensions, ExplicitModeRules fallback, List<String> diagnostics) {
		Optional<ResourceKey<Level>> dimension = dimensionKey(entry.getKey(), "explicit.dimensions", diagnostics);
		if (dimension.isEmpty()) {
			return;
		}
		Optional<JsonObject> object = asObject(entry.getValue());
		if (object.isEmpty()) {
			diagnostics.add("explicit.dimensions." + entry.getKey() + " must be an object; using default Explicit mode rules.");
			return;
		}
		dimensions.put(dimension.get(), readExplicitRules(object.get(), dimensions.getOrDefault(dimension.get(), fallback), "explicit.dimensions." + entry.getKey(), diagnostics));
	}

	private static void readThresholdDimension(Map.Entry<String, JsonElement> entry, Map<ResourceKey<Level>, ThresholdModeRules> dimensions, ThresholdModeRules fallback, List<String> diagnostics) {
		Optional<ResourceKey<Level>> dimension = dimensionKey(entry.getKey(), "threshold.dimensions", diagnostics);
		if (dimension.isEmpty()) {
			return;
		}
		Optional<JsonObject> object = asObject(entry.getValue());
		if (object.isEmpty()) {
			diagnostics.add("threshold.dimensions." + entry.getKey() + " must be an object; using default Threshold mode rules.");
			return;
		}
		dimensions.put(dimension.get(), readThresholdRules(object.get(), dimensions.getOrDefault(dimension.get(), fallback), "threshold.dimensions." + entry.getKey(), diagnostics));
	}

	private static ExplicitModeRules readExplicitRules(JsonObject object, ExplicitModeRules fallback, String path, List<String> diagnostics) {
		CropBehavior defaultBehavior = behaviorMember(object, "default_behavior", fallback.defaultBehavior(), path + ".default_behavior", diagnostics);
		Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules = mutableCropBiomeRules(fallback.cropBiomeRules());
		objectMember(object, "biomes").ifPresent(biomes -> biomes.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> readExplicitBiome(entry, cropBiomeRules, defaultBehavior, path, diagnostics)));
		return new ExplicitModeRules(defaultBehavior, immutableCropBiomeRules(cropBiomeRules));
	}

	private static void readExplicitBiome(Map.Entry<String, JsonElement> entry, Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules, CropBehavior defaultBehavior, String path, List<String> diagnostics) {
		Optional<Identifier> biomeId = identifier(entry.getKey(), path + ".biomes", diagnostics);
		if (biomeId.isEmpty()) {
			return;
		}
		Optional<JsonObject> crops = asObject(entry.getValue());
		if (crops.isEmpty()) {
			diagnostics.add(path + ".biomes." + entry.getKey() + " must be an object of crop ids to behaviors; ignoring biome rules.");
			return;
		}
		crops.get().entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(cropEntry -> readExplicitBiomeCrop(cropEntry, biomeId.get(), cropBiomeRules, defaultBehavior, path + ".biomes." + entry.getKey(), diagnostics));
	}

	private static void readExplicitBiomeCrop(Map.Entry<String, JsonElement> entry, Identifier biomeId, Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules, CropBehavior defaultBehavior, String path, List<String> diagnostics) {
		Optional<Identifier> cropId = identifier(entry.getKey(), path, diagnostics);
		if (cropId.isEmpty()) {
			return;
		}
		CropBehavior behavior = behavior(entry.getValue(), defaultBehavior, path + "." + entry.getKey(), diagnostics);
		cropBiomeRules.computeIfAbsent(cropId.get(), ignored -> new LinkedHashMap<>()).put(biomeId, behavior);
	}

	private static ThresholdModeRules readThresholdRules(JsonObject object, ThresholdModeRules fallback, String path, List<String> diagnostics) {
		ThresholdCropRule defaultRule = member(object, "default_rule")
				.map(element -> readThresholdCropRule(element, fallback.defaultRule(), path + ".default_rule", diagnostics))
				.orElse(fallback.defaultRule());
		Map<Identifier, ThresholdCropRule> cropRules = new LinkedHashMap<>(fallback.cropRules());
		objectMember(object, "crops").ifPresent(crops -> crops.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> readThresholdCropRuleEntry(entry, cropRules, defaultRule, path + ".crops", diagnostics)));
		return new ThresholdModeRules(defaultRule, Map.copyOf(cropRules));
	}

	private static void readThresholdCropRuleEntry(Map.Entry<String, JsonElement> entry, Map<Identifier, ThresholdCropRule> cropRules, ThresholdCropRule defaultRule, String path, List<String> diagnostics) {
		Optional<Identifier> cropId = identifier(entry.getKey(), path, diagnostics);
		if (cropId.isEmpty()) {
			return;
		}
		ThresholdCropRule fallback = cropRules.getOrDefault(cropId.get(), defaultRule);
		cropRules.put(cropId.get(), readThresholdCropRule(entry.getValue(), fallback, path + "." + entry.getKey(), diagnostics));
	}

	private static ThresholdCropRule readThresholdCropRule(JsonElement element, ThresholdCropRule fallback, String path, List<String> diagnostics) {
		Optional<JsonObject> object = asObject(element);
		if (object.isEmpty()) {
			diagnostics.add(path + " must be an object; using default threshold rule.");
			return fallback;
		}
		CropBehavior defaultBehavior = behaviorMember(object.get(), "default_behavior", fallback.defaultBehavior(), path + ".default_behavior", diagnostics);
		List<ClimateRule> climateRules = climateRules(object.get().get("climate_rules"), fallback.climateRules(), defaultBehavior, path + ".climate_rules", diagnostics);
		return new ThresholdCropRule(defaultBehavior, List.copyOf(climateRules));
	}

	private static List<ClimateRule> climateRules(JsonElement element, List<ClimateRule> fallback, CropBehavior defaultBehavior, String path, List<String> diagnostics) {
		if (element == null || element.isJsonNull()) {
			return fallback;
		}
		if (!element.isJsonArray()) {
			diagnostics.add(path + " must be an array; using default climate rules.");
			return fallback;
		}
		List<ClimateRule> rules = new ArrayList<>();
		JsonArray array = element.getAsJsonArray();
		for (int i = 0; i < array.size(); i++) {
			climateRule(array.get(i), defaultBehavior, path + "[" + i + "]", diagnostics).ifPresent(rules::add);
		}
		return rules;
	}

	private static Optional<ClimateRule> climateRule(JsonElement element, CropBehavior defaultBehavior, String path, List<String> diagnostics) {
		Optional<JsonObject> object = asObject(element);
		if (object.isEmpty()) {
			diagnostics.add(path + " must be an object; ignoring climate rule.");
			return Optional.empty();
		}
		float minTemperature = floatMember(object.get(), "min_temperature", DEFAULT_MIN_TEMPERATURE, path + ".min_temperature", diagnostics);
		float maxTemperature = floatMember(object.get(), "max_temperature", DEFAULT_MAX_TEMPERATURE, path + ".max_temperature", diagnostics);
		if (maxTemperature <= minTemperature) {
			diagnostics.add(path + " has max_temperature <= min_temperature; ignoring climate rule.");
			return Optional.empty();
		}
		PrecipitationRequirement precipitation = precipitationMember(object.get(), "precipitation", PrecipitationRequirement.IGNORED, path + ".precipitation", diagnostics);
		CropBehavior behavior = behaviorMember(object.get(), "behavior", defaultBehavior, path + ".behavior", diagnostics);
		return Optional.of(new ClimateRule(minTemperature, maxTemperature, precipitation, behavior));
	}

	private static JsonObject toJson(GeneralFile general) {
		JsonObject object = new JsonObject();
		object.addProperty("schema_version", SCHEMA_VERSION);
		object.addProperty("affects_bonemeal", general.options().affectsBonemeal());
		object.addProperty("affects_block_placement", general.options().affectsBlockPlacement());
		object.addProperty("affects_village_farm_generation", general.options().affectsVillageFarmGeneration());
		object.addProperty("chat_info", general.options().chatInfo());
		JsonArray excludedBlocks = new JsonArray();
		general.options().excludedBlocks().stream().map(Object::toString).sorted().forEach(excludedBlocks::add);
		object.add("excluded_blocks", excludedBlocks);
		object.addProperty("fallback_mode", general.fallbackMode().serializedName());
		JsonObject dimensions = new JsonObject();
		general.dimensionModes().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> dimensions.addProperty(entry.getKey().identifier().toString(), entry.getValue().serializedName()));
		object.add("dimensions", dimensions);
		return object;
	}

	private static JsonObject toJson(ExplicitModeFile explicit) {
		JsonObject object = new JsonObject();
		object.addProperty("schema_version", SCHEMA_VERSION);
		object.add("fallback", explicitRulesJson(explicit.fallback()));
		JsonObject dimensions = new JsonObject();
		explicit.dimensions().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> dimensions.add(entry.getKey().identifier().toString(), explicitRulesJson(entry.getValue())));
		object.add("dimensions", dimensions);
		return object;
	}

	private static JsonObject toJson(ThresholdModeFile threshold) {
		JsonObject object = new JsonObject();
		object.addProperty("schema_version", SCHEMA_VERSION);
		object.add("fallback", thresholdRulesJson(threshold.fallback()));
		JsonObject dimensions = new JsonObject();
		threshold.dimensions().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> dimensions.add(entry.getKey().identifier().toString(), thresholdRulesJson(entry.getValue())));
		object.add("dimensions", dimensions);
		return object;
	}

	private static JsonObject explicitRulesJson(ExplicitModeRules rules) {
		JsonObject object = new JsonObject();
		object.addProperty("default_behavior", rules.defaultBehavior().serializedName());
		JsonObject biomes = new JsonObject();
		biomeCropRules(rules.cropBiomeRules()).entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
				.forEach(entry -> biomes.add(entry.getKey().toString(), cropBehaviorRules(entry.getValue())));
		object.add("biomes", biomes);
		return object;
	}

	private static JsonObject cropBehaviorRules(Map<Identifier, CropBehavior> rules) {
		JsonObject object = new JsonObject();
		rules.entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
				.forEach(entry -> object.addProperty(entry.getKey().toString(), entry.getValue().serializedName()));
		return object;
	}

	private static Map<Identifier, Map<Identifier, CropBehavior>> biomeCropRules(Map<Identifier, Map<Identifier, CropBehavior>> cropBiomeRules) {
		Map<Identifier, Map<Identifier, CropBehavior>> biomes = new LinkedHashMap<>();
		cropBiomeRules.forEach((cropId, biomeRules) -> biomeRules.forEach((biomeId, behavior) -> biomes.computeIfAbsent(biomeId, ignored -> new LinkedHashMap<>()).put(cropId, behavior)));
		return biomes;
	}

	private static JsonObject thresholdRulesJson(ThresholdModeRules rules) {
		JsonObject object = new JsonObject();
		object.add("default_rule", thresholdCropRuleJson(rules.defaultRule()));
		JsonObject crops = new JsonObject();
		rules.cropRules().entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
				.forEach(entry -> crops.add(entry.getKey().toString(), thresholdCropRuleJson(entry.getValue())));
		object.add("crops", crops);
		return object;
	}

	private static JsonObject thresholdCropRuleJson(ThresholdCropRule rule) {
		JsonObject object = new JsonObject();
		object.addProperty("default_behavior", rule.defaultBehavior().serializedName());
		JsonArray climateRules = new JsonArray();
		for (ClimateRule climateRule : rule.climateRules()) {
			climateRules.add(climateRuleJson(climateRule));
		}
		object.add("climate_rules", climateRules);
		return object;
	}

	private static JsonObject climateRuleJson(ClimateRule rule) {
		JsonObject object = new JsonObject();
		object.addProperty("min_temperature", rule.minTemperatureInclusive());
		object.addProperty("max_temperature", rule.maxTemperatureExclusive());
		object.addProperty("precipitation", rule.precipitation().serializedName());
		object.addProperty("behavior", rule.behavior().serializedName());
		return object;
	}

	private static CropBehavior defaultBehavior(DimensionRules rules) {
		if (rules instanceof ExplicitModeRules explicitModeRules) {
			return explicitModeRules.defaultBehavior();
		}
		if (rules instanceof ThresholdModeRules thresholdModeRules) {
			return thresholdModeRules.defaultRule().defaultBehavior();
		}
		return CropBehavior.GROWABLE;
	}

	private static Map<Identifier, Map<Identifier, CropBehavior>> mutableCropBiomeRules(Map<Identifier, Map<Identifier, CropBehavior>> rules) {
		Map<Identifier, Map<Identifier, CropBehavior>> mutableRules = new LinkedHashMap<>();
		rules.forEach((cropId, biomeRules) -> mutableRules.put(cropId, new LinkedHashMap<>(biomeRules)));
		return mutableRules;
	}

	private static Map<Identifier, Map<Identifier, CropBehavior>> immutableCropBiomeRules(Map<Identifier, Map<Identifier, CropBehavior>> rules) {
		Map<Identifier, Map<Identifier, CropBehavior>> immutableRules = new LinkedHashMap<>();
		rules.forEach((cropId, biomeRules) -> immutableRules.put(cropId, Map.copyOf(biomeRules)));
		return Map.copyOf(immutableRules);
	}

	private static Optional<ResourceKey<Level>> dimensionKey(String value, String path, List<String> diagnostics) {
		return identifier(value, path, diagnostics).map(identifier -> ResourceKey.create(Registries.DIMENSION, identifier));
	}

	private static Optional<Identifier> identifier(String value, String path, List<String> diagnostics) {
		Optional<Identifier> identifier = DefaultCropBiomeConfig.tryId(value);
		if (identifier.isEmpty()) {
			diagnostics.add("Ignoring invalid identifier at " + path + ": " + value);
		}
		return identifier;
	}

	private static Optional<JsonElement> member(JsonObject object, String name) {
		JsonElement element = object.get(name);
		return element == null || element.isJsonNull() ? Optional.empty() : Optional.of(element);
	}

	private static Optional<JsonObject> objectMember(JsonObject object, String name) {
		return member(object, name).flatMap(CropBiomeLimiterConfigLoader::asObject);
	}

	private static Optional<JsonObject> asObject(JsonElement element) {
		if (element == null || element.isJsonNull() || !element.isJsonObject()) {
			return Optional.empty();
		}
		return Optional.of(element.getAsJsonObject());
	}

	private static Set<Identifier> identifiers(JsonElement element, Set<Identifier> fallback, String path, List<String> diagnostics) {
		if (element == null || element.isJsonNull()) {
			return fallback;
		}
		if (!element.isJsonArray()) {
			diagnostics.add(path + " must be an array; using default identifiers.");
			return fallback;
		}
		Set<Identifier> identifiers = new LinkedHashSet<>();
		JsonArray array = element.getAsJsonArray();
		for (int i = 0; i < array.size(); i++) {
			String value = string(array.get(i), null, path + "[" + i + "]", diagnostics);
			if (value == null) {
				continue;
			}
			Optional<Identifier> identifier = DefaultCropBiomeConfig.tryId(value);
			if (identifier.isPresent()) {
				identifiers.add(identifier.get());
			} else {
				diagnostics.add("Ignoring invalid identifier at " + path + "[" + i + "]: " + value);
			}
		}
		return Set.copyOf(identifiers);
	}

	private static boolean booleanMember(JsonObject object, String name, boolean fallback, String path, List<String> diagnostics) {
		return member(object, name).map(element -> bool(element, fallback, path, diagnostics)).orElse(fallback);
	}

	private static boolean bool(JsonElement element, boolean fallback, String path, List<String> diagnostics) {
		try {
			return element.getAsBoolean();
		} catch (RuntimeException exception) {
			diagnostics.add(path + " must be true or false; using default value.");
			return fallback;
		}
	}

	private static float floatMember(JsonObject object, String name, float fallback, String path, List<String> diagnostics) {
		return member(object, name).map(element -> floatingPoint(element, fallback, path, diagnostics)).orElse(fallback);
	}

	private static float floatingPoint(JsonElement element, float fallback, String path, List<String> diagnostics) {
		try {
			return element.getAsFloat();
		} catch (RuntimeException exception) {
			diagnostics.add(path + " must be a number; using default value.");
			return fallback;
		}
	}

	private static RuleMode modeMember(JsonObject object, String name, RuleMode fallback, String path, List<String> diagnostics) {
		return member(object, name).map(element -> {
			String value = string(element, null, path, diagnostics);
			if (value == null) {
				return fallback;
			}
			return RuleMode.tryFromSerializedName(value).orElseGet(() -> {
				diagnostics.add(path + " has unknown mode '" + value + "'; using " + fallback.serializedName() + ".");
				return fallback;
			});
		}).orElse(fallback);
	}

	private static CropBehavior behaviorMember(JsonObject object, String name, CropBehavior fallback, String path, List<String> diagnostics) {
		return member(object, name).map(element -> behavior(element, fallback, path, diagnostics)).orElse(fallback);
	}

	private static CropBehavior behavior(JsonElement element, CropBehavior fallback, String path, List<String> diagnostics) {
		String value = string(element, null, path, diagnostics);
		if (value == null) {
			return fallback;
		}
		return CropBehavior.tryFromSerializedName(value).orElseGet(() -> {
			diagnostics.add(path + " has unknown behavior '" + value + "'; using " + fallback.serializedName() + ".");
			return fallback;
		});
	}

	private static PrecipitationRequirement precipitationMember(JsonObject object, String name, PrecipitationRequirement fallback, String path, List<String> diagnostics) {
		return member(object, name).map(element -> {
			String value = string(element, null, path, diagnostics);
			if (value == null) {
				return fallback;
			}
			return PrecipitationRequirement.tryFromSerializedName(value).orElseGet(() -> {
				diagnostics.add(path + " has unknown precipitation value '" + value + "'; using " + fallback.serializedName() + ".");
				return fallback;
			});
		}).orElse(fallback);
	}

	private static String string(JsonElement element, String fallback, String path, List<String> diagnostics) {
		try {
			return element.getAsString();
		} catch (RuntimeException exception) {
			diagnostics.add(path + " must be a string; using default value.");
			return fallback;
		}
	}

	private static List<BiomeClimate> defaultBiomeClimates() {
		return List.of(
				biomeClimate("minecraft:the_void", 0.5F, false),
				biomeClimate("minecraft:plains", 0.8F, true),
				biomeClimate("minecraft:sunflower_plains", 0.8F, true),
				biomeClimate("minecraft:snowy_plains", 0.0F, true),
				biomeClimate("minecraft:ice_spikes", 0.0F, true),
				biomeClimate("minecraft:desert", 2.0F, false),
				biomeClimate("minecraft:swamp", 0.8F, true),
				biomeClimate("minecraft:mangrove_swamp", 0.8F, true),
				biomeClimate("minecraft:forest", 0.7F, true),
				biomeClimate("minecraft:flower_forest", 0.7F, true),
				biomeClimate("minecraft:birch_forest", 0.6F, true),
				biomeClimate("minecraft:dark_forest", 0.7F, true),
				biomeClimate("minecraft:pale_garden", 0.7F, true),
				biomeClimate("minecraft:old_growth_birch_forest", 0.6F, true),
				biomeClimate("minecraft:old_growth_pine_taiga", 0.3F, true),
				biomeClimate("minecraft:old_growth_spruce_taiga", 0.25F, true),
				biomeClimate("minecraft:taiga", 0.25F, true),
				biomeClimate("minecraft:snowy_taiga", -0.5F, true),
				biomeClimate("minecraft:savanna", 2.0F, false),
				biomeClimate("minecraft:savanna_plateau", 2.0F, false),
				biomeClimate("minecraft:windswept_hills", 0.2F, true),
				biomeClimate("minecraft:windswept_gravelly_hills", 0.2F, true),
				biomeClimate("minecraft:windswept_forest", 0.2F, true),
				biomeClimate("minecraft:windswept_savanna", 2.0F, false),
				biomeClimate("minecraft:jungle", 0.95F, true),
				biomeClimate("minecraft:sparse_jungle", 0.95F, true),
				biomeClimate("minecraft:bamboo_jungle", 0.95F, true),
				biomeClimate("minecraft:badlands", 2.0F, false),
				biomeClimate("minecraft:eroded_badlands", 2.0F, false),
				biomeClimate("minecraft:wooded_badlands", 2.0F, false),
				biomeClimate("minecraft:meadow", 0.5F, true),
				biomeClimate("minecraft:cherry_grove", 0.5F, true),
				biomeClimate("minecraft:grove", -0.2F, true),
				biomeClimate("minecraft:snowy_slopes", -0.3F, true),
				biomeClimate("minecraft:frozen_peaks", -0.7F, true),
				biomeClimate("minecraft:jagged_peaks", -0.7F, true),
				biomeClimate("minecraft:stony_peaks", 1.0F, true),
				biomeClimate("minecraft:river", 0.5F, true),
				biomeClimate("minecraft:frozen_river", 0.0F, true),
				biomeClimate("minecraft:beach", 0.8F, true),
				biomeClimate("minecraft:snowy_beach", 0.05F, true),
				biomeClimate("minecraft:stony_shore", 0.2F, true),
				biomeClimate("minecraft:warm_ocean", 0.5F, true),
				biomeClimate("minecraft:lukewarm_ocean", 0.5F, true),
				biomeClimate("minecraft:deep_lukewarm_ocean", 0.5F, true),
				biomeClimate("minecraft:ocean", 0.5F, true),
				biomeClimate("minecraft:deep_ocean", 0.5F, true),
				biomeClimate("minecraft:cold_ocean", 0.5F, true),
				biomeClimate("minecraft:deep_cold_ocean", 0.5F, true),
				biomeClimate("minecraft:frozen_ocean", 0.0F, true),
				biomeClimate("minecraft:deep_frozen_ocean", 0.5F, true),
				biomeClimate("minecraft:mushroom_fields", 0.9F, true),
				biomeClimate("minecraft:dripstone_caves", 0.8F, true),
				biomeClimate("minecraft:lush_caves", 0.5F, true),
				biomeClimate("minecraft:deep_dark", 0.8F, true),
				biomeClimate("minecraft:nether_wastes", 2.0F, false),
				biomeClimate("minecraft:warped_forest", 2.0F, false),
				biomeClimate("minecraft:crimson_forest", 2.0F, false),
				biomeClimate("minecraft:soul_sand_valley", 2.0F, false),
				biomeClimate("minecraft:basalt_deltas", 2.0F, false),
				biomeClimate("minecraft:the_end", 0.5F, false),
				biomeClimate("minecraft:end_highlands", 0.5F, false),
				biomeClimate("minecraft:end_midlands", 0.5F, false),
				biomeClimate("minecraft:small_end_islands", 0.5F, false),
				biomeClimate("minecraft:end_barrens", 0.5F, false)
		);
	}

	private static BiomeClimate biomeClimate(String id, float temperature, boolean hasPrecipitation) {
		return new BiomeClimate(DefaultCropBiomeConfig.id(id), temperature, hasPrecipitation);
	}

	private record GeneralFile(GeneralOptions options, RuleMode fallbackMode, Map<ResourceKey<Level>, RuleMode> dimensionModes) {
	}

	private record ExplicitModeFile(ExplicitModeRules fallback, Map<ResourceKey<Level>, ExplicitModeRules> dimensions) {
	}

	private record ThresholdModeFile(ThresholdModeRules fallback, Map<ResourceKey<Level>, ThresholdModeRules> dimensions) {
	}

	private record BiomeClimate(Identifier id, float temperature, boolean hasPrecipitation) {
	}
}
