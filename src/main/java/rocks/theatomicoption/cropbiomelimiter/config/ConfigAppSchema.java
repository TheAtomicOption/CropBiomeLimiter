package rocks.theatomicoption.cropbiomelimiter.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class ConfigAppSchema {
	public static final int SCHEMA_VERSION = 1;
	public static final String CONFIG_DIRECTORY_NAME = "cropbiomelimiter";
	public static final String GENERAL_FILE_NAME = "general.json";
	public static final String EXPLICIT_MODE_FILE_NAME = "explicit-mode.json";
	public static final String THRESHOLD_MODE_FILE_NAME = "threshold-mode.json";
	public static final String REGISTRY_SNAPSHOT_FILE_NAME = "cropbiomelimiter-registry-snapshot.json";

	private ConfigAppSchema() {
	}

	public static JsonObject metadataJson() {
		JsonObject schema = new JsonObject();
		schema.addProperty("schema_version", SCHEMA_VERSION);
		schema.addProperty("config_directory", CONFIG_DIRECTORY_NAME);
		schema.addProperty("registry_snapshot_file", REGISTRY_SNAPSHOT_FILE_NAME);
		schema.add("config_files", configFilesJson());
		schema.add("general_options", generalOptionsJson());
		schema.add("modes", modesJson());
		schema.add("behaviors", behaviorsJson());
		schema.add("behavior_cycle_order", behaviorNamesJson());
		schema.add("precipitation_requirements", precipitationRequirementsJson());
		schema.add("threshold_inputs", thresholdInputsJson());
		schema.add("explicit_mode", explicitModeJson());
		schema.add("threshold_mode", thresholdModeJson());
		return schema;
	}

	public static JsonArray modeNamesJson() {
		JsonArray values = new JsonArray();
		for (RuleMode mode : RuleMode.values()) {
			values.add(mode.serializedName());
		}
		return values;
	}

	public static JsonArray behaviorNamesJson() {
		JsonArray values = new JsonArray();
		for (CropBehavior behavior : CropBehavior.values()) {
			values.add(behavior.serializedName());
		}
		return values;
	}

	public static JsonArray precipitationRequirementNamesJson() {
		JsonArray values = new JsonArray();
		for (PrecipitationRequirement requirement : PrecipitationRequirement.values()) {
			values.add(requirement.serializedName());
		}
		return values;
	}

	public static String displayName(RuleMode mode) {
		if (mode == null) {
			return "Unknown mode";
		}
		return switch (mode) {
			case EXPLICIT -> "Explicit mode";
			case THRESHOLD -> "Threshold mode";
		};
	}

	public static String displayName(CropBehavior behavior) {
		if (behavior == null) {
			return "Unknown behavior";
		}
		return switch (behavior) {
			case GROWABLE -> "Growable";
			case BONEMEAL_REQUIRED -> "Bonemeal required";
			case UNPLANTABLE -> "Unplantable";
		};
	}

	public static String displayName(PrecipitationRequirement requirement) {
		if (requirement == null) {
			return "Unknown precipitation";
		}
		return switch (requirement) {
			case REQUIRED -> "Requires precipitation";
			case FORBIDDEN -> "Requires no precipitation";
			case IGNORED -> "Ignores precipitation";
		};
	}

	private static JsonObject configFilesJson() {
		JsonObject files = new JsonObject();
		files.addProperty("general", GENERAL_FILE_NAME);
		files.addProperty("explicit_mode", EXPLICIT_MODE_FILE_NAME);
		files.addProperty("threshold_mode", THRESHOLD_MODE_FILE_NAME);
		return files;
	}

	private static JsonObject generalOptionsJson() {
		JsonObject options = new JsonObject();
		options.addProperty("affects_bonemeal", "Limits bone meal use in restricted crop climates.");
		options.addProperty("affects_block_placement", "Limits survival placement in unplantable crop climates.");
		options.addProperty("affects_village_farm_generation", "Adjusts vanilla village farm crop processors to match Threshold mode defaults.");
		options.addProperty("chat_info", "Shows player feedback for blocked or warning-only actions.");
		options.addProperty("excluded_blocks", "Block ids ignored by all crop behavior checks.");
		return options;
	}

	private static JsonArray modesJson() {
		JsonArray values = new JsonArray();
		for (RuleMode mode : RuleMode.values()) {
			JsonObject value = new JsonObject();
			value.addProperty("id", mode.serializedName());
			value.addProperty("display_name", displayName(mode));
			value.addProperty("dimension_scoped", true);
			values.add(value);
		}
		return values;
	}

	private static JsonArray behaviorsJson() {
		JsonArray values = new JsonArray();
		for (CropBehavior behavior : CropBehavior.values()) {
			JsonObject value = new JsonObject();
			value.addProperty("id", behavior.serializedName());
			value.addProperty("display_name", displayName(behavior));
			value.addProperty("allows_planting", behavior.allowsPlanting());
			value.addProperty("allows_natural_growth", behavior.allowsNaturalGrowth());
			value.addProperty("allows_bonemeal", behavior.allowsBonemeal());
			values.add(value);
		}
		return values;
	}

	private static JsonArray precipitationRequirementsJson() {
		JsonArray values = new JsonArray();
		for (PrecipitationRequirement requirement : PrecipitationRequirement.values()) {
			JsonObject value = new JsonObject();
			value.addProperty("id", requirement.serializedName());
			value.addProperty("display_name", displayName(requirement));
			values.add(value);
		}
		return values;
	}

	private static JsonObject thresholdInputsJson() {
		JsonObject value = new JsonObject();
		value.addProperty("temperature", "Biome#getBaseTemperature()");
		value.addProperty("has_precipitation", "Biome#hasPrecipitation()");
		return value;
	}

	private static JsonObject explicitModeJson() {
		JsonObject value = new JsonObject();
		value.addProperty("config_file", EXPLICIT_MODE_FILE_NAME);
		value.addProperty("layout", "crops_as_rows_biomes_as_columns");
		value.addProperty("cell_values", "behavior_cycle_order");
		value.addProperty("dimension_key", "dimensions");
		value.addProperty("biome_key", "biomes");
		return value;
	}

	private static JsonObject thresholdModeJson() {
		JsonObject value = new JsonObject();
		value.addProperty("config_file", THRESHOLD_MODE_FILE_NAME);
		value.addProperty("layout", "crops_as_rows_with_climate_rules");
		value.addProperty("dimension_key", "dimensions");
		value.addProperty("crop_key", "crops");
		value.addProperty("preview_values", "behavior_cycle_order");
		return value;
	}
}
