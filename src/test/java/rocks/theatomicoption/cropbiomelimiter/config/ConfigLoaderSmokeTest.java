package rocks.theatomicoption.cropbiomelimiter.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import rocks.theatomicoption.cropbiomelimiter.logic.GrowableBlockClassifier;

public final class ConfigLoaderSmokeTest {
	private static final String MINIMAL_EXPLICIT_CONFIG = """
			{
			  "schema_version": 1,
			  "fallback": {
			    "default_behavior": "growable",
			    "biomes": {}
			  },
			  "dimensions": {}
			}
			""";
	private static final String MINIMAL_THRESHOLD_CONFIG = """
			{
			  "schema_version": 1,
			  "fallback": {
			    "default_rule": {
			      "default_behavior": "growable",
			      "climate_rules": []
			    },
			    "crops": {}
			  },
			  "dimensions": {}
			}
			""";

	private ConfigLoaderSmokeTest() {
	}

	public static void run() throws Exception {
		writesDefaultConfigWhenMissing();
		recreatesDeletedSplitConfigFiles();
		loadsExplicitModeRules();
		loadsThresholdModeRules();
		fallsBackOnMalformedJson();
		fallsBackOnUnknownSerializedValues();
		serializesDefaultConfigSchema();
		exportsConfigAppSchemaContract();
		generatedThresholdConfigDocumentsEveryDefaultCropBehavior();
		generatedExplicitConfigMirrorsThresholdDefaults();
		defaultVanillaRulesCoverKeyPlants();
		defaultThresholdRulesUseBiomeClimateStrategy();
	}

	private static void writesDefaultConfigWhenMissing() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-default-config-test");
		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		Path configDirectory = configDirectory(directory);

		assertTrue(result.createdDefault(), "missing split config should be written on first load");
		assertEquals(List.of(
				CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME,
				CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME,
				CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME
		), result.createdDefaultFiles(), "first load should report all restored split config files");
		assertEquals(configDirectory, result.path(), "load result should point to the split config directory");
		assertTrue(Files.isDirectory(configDirectory), "default config directory should exist");
		assertTrue(Files.exists(configDirectory.resolve(CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME)), "general config file should exist");
		assertTrue(Files.exists(configDirectory.resolve(CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME)), "Explicit mode config file should exist");
		assertTrue(Files.exists(configDirectory.resolve(CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME)), "Threshold mode config file should exist");
		assertTrue(!Files.exists(directory.resolve("cropbiomelimiter.json")), "single-file legacy config should not be created");
		assertNotNull(result.config(), "config should be available");
	}

	private static void recreatesDeletedSplitConfigFiles() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-deleted-config-test");
		ConfigLoadResult firstLoad = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(firstLoad.createdDefault(), "first load should write the split config files");

		Path generalPath = configPath(directory, CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME);
		Path thresholdPath = configPath(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME);
		String generalJsonBefore = Files.readString(generalPath);
		Files.delete(thresholdPath);

		ConfigLoadResult secondLoad = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(secondLoad.createdDefault(), "startup load should recreate deleted split config files");
		assertEquals(List.of(CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME), secondLoad.createdDefaultFiles(), "startup load should report the deleted split config file");
		assertTrue(Files.exists(thresholdPath), "deleted Threshold mode config file should be recreated");
		assertEquals(generalJsonBefore, Files.readString(generalPath), "existing split config files should not be overwritten");
		assertNotNull(secondLoad.config(), "config should remain available after recreating a deleted file");
	}

	private static void loadsExplicitModeRules() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-explicit-config-test");
		writeConfig(directory, CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME, """
				{
				  "schema_version": 1,
				  "affects_bonemeal": false,
				  "affects_block_placement": true,
				  "affects_village_farm_generation": false,
				  "chat_info": false,
				  "excluded_blocks": ["minecraft:cave_vines"],
				  "fallback_mode": "threshold",
				  "dimensions": {
				    "minecraft:overworld": "explicit"
				  }
				}
				""");
		writeConfig(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME, """
				{
				  "schema_version": 1,
				  "fallback": {
				    "default_behavior": "growable",
				    "biomes": {}
				  },
				  "dimensions": {
				    "minecraft:overworld": {
				      "default_behavior": "bonemeal-required",
				      "biomes": {
				        "minecraft:desert": {
				          "minecraft:wheat": "unplantable"
				        }
				      }
				    }
				  }
				}
				""");
		writeConfig(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME, MINIMAL_THRESHOLD_CONFIG);

		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		DimensionRules rules = result.config().rulesFor(Level.OVERWORLD);
		assertTrue(rules instanceof ExplicitModeRules, "overworld should use Explicit mode");

		ExplicitModeRules explicitRules = (ExplicitModeRules) rules;
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, explicitRules.defaultBehavior(), "explicit default behavior should parse");
		assertEquals(CropBehavior.UNPLANTABLE,
				explicitRules.cropBiomeRules().get(id("minecraft:wheat")).get(id("minecraft:desert")),
				"Explicit mode biome/crop cell should parse");
		assertTrue(!result.config().generalOptions().affectsBonemeal(), "affects_bonemeal should parse false");
		assertTrue(!result.config().generalOptions().affectsVillageFarmGeneration(), "affects_village_farm_generation should parse false");
		assertTrue(result.config().generalOptions().isExcluded(id("minecraft:cave_vines")), "excluded block should parse");
	}

	private static void loadsThresholdModeRules() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-threshold-config-test");
		writeConfig(directory, CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME, """
				{
				  "schema_version": 1,
				  "affects_bonemeal": true,
				  "affects_block_placement": true,
				  "affects_village_farm_generation": true,
				  "chat_info": true,
				  "excluded_blocks": [],
				  "fallback_mode": "threshold",
				  "dimensions": {
				    "minecraft:overworld": "threshold"
				  }
				}
				""");
		writeConfig(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME, MINIMAL_EXPLICIT_CONFIG);
		writeConfig(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME, """
				{
				  "schema_version": 1,
				  "fallback": {
				    "default_rule": {
				      "default_behavior": "growable",
				      "climate_rules": []
				    },
				    "crops": {}
				  },
				  "dimensions": {
				    "minecraft:overworld": {
				      "default_rule": {
				        "default_behavior": "growable",
				        "climate_rules": []
				      },
				      "crops": {
				        "minecraft:wheat": {
				          "default_behavior": "bonemeal-required",
				          "climate_rules": [
				            {
				              "min_temperature": 0.15,
				              "max_temperature": 1.5,
				              "precipitation": "required",
				              "behavior": "growable"
				            }
				          ]
				        }
				      }
				    }
				  }
				}
				""");

		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		DimensionRules rules = result.config().rulesFor(Level.OVERWORLD);
		assertTrue(rules instanceof ThresholdModeRules, "overworld should use Threshold mode");

		ThresholdCropRule wheatRule = ((ThresholdModeRules) rules).cropRules().get(id("minecraft:wheat"));
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, wheatRule.defaultBehavior(), "threshold crop default should parse");
		List<ClimateRule> climateRules = wheatRule.climateRules();
		assertEquals(1, climateRules.size(), "threshold crop should have one climate rule");
		assertEquals(PrecipitationRequirement.REQUIRED, climateRules.getFirst().precipitation(), "precipitation should parse");
		assertTrue(climateRules.getFirst().matches(0.2F, true), "parsed climate rule should match warm wet biome data");
		assertTrue(!climateRules.getFirst().matches(0.2F, false), "parsed climate rule should reject dry biome data");
	}

	private static void fallsBackOnMalformedJson() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-malformed-config-test");
		String malformedThresholdConfig = "{ this is not json";
		writeConfig(directory, CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME, """
				{
				  "schema_version": 1,
				  "fallback_mode": "threshold",
				  "dimensions": {
				    "minecraft:overworld": "threshold"
				  }
				}
				""");
		writeConfig(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME, MINIMAL_EXPLICIT_CONFIG);
		writeConfig(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME, malformedThresholdConfig);

		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(result.hasDiagnostics(), "malformed json should produce diagnostics");
		assertTrue(!result.createdDefault(), "malformed existing split config file should not be overwritten");
		assertEquals(malformedThresholdConfig, Files.readString(configPath(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME)), "malformed file should remain untouched");
		assertTrue(result.config().rulesFor(Level.OVERWORLD) instanceof ThresholdModeRules, "malformed Threshold mode config should fall back to defaults");
	}

	private static void fallsBackOnUnknownSerializedValues() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-unknown-values-test");
		writeConfig(directory, CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME, """
				{
				  "schema_version": 1,
				  "fallback_mode": "not-a-mode",
				  "dimensions": {
				    "minecraft:overworld": "still-not-a-mode"
				  }
				}
				""");
		writeConfig(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME, MINIMAL_EXPLICIT_CONFIG);
		writeConfig(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME, """
				{
				  "schema_version": 1,
				  "fallback": {
				    "default_rule": {
				      "default_behavior": "not-a-behavior",
				      "climate_rules": [
				        {
				          "min_temperature": 0.0,
				          "max_temperature": 1.0,
				          "precipitation": "not-precipitation",
				          "behavior": "still-not-a-behavior"
				        }
				      ]
				    },
				    "crops": {}
				  },
				  "dimensions": {}
				}
				""");

		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(result.hasDiagnostics(), "unknown serialized values should produce diagnostics");
		assertTrue(result.config().rulesFor(Level.OVERWORLD) instanceof ThresholdModeRules, "unknown mode should keep the default Threshold mode");
		assertEquals(RuleMode.THRESHOLD, RuleMode.fromSerializedName("missing"), "RuleMode fromSerializedName should fall back safely");
		assertEquals(CropBehavior.GROWABLE, CropBehavior.fromSerializedName("missing"), "CropBehavior fromSerializedName should fall back safely");
		assertEquals(PrecipitationRequirement.IGNORED, PrecipitationRequirement.fromSerializedName("missing"), "PrecipitationRequirement fromSerializedName should fall back safely");
	}

	private static void serializesDefaultConfigSchema() {
		CropBiomeLimiterConfig defaults = DefaultCropBiomeConfig.create();
		String generalJson = CropBiomeLimiterConfigLoader.generalJson(defaults).toString();
		String explicitJson = CropBiomeLimiterConfigLoader.explicitModeJson(defaults).toString();
		String thresholdJson = CropBiomeLimiterConfigLoader.thresholdModeJson(defaults).toString();

		assertTrue(generalJson.contains("schema_version"), "general config should include schema version");
		assertTrue(generalJson.contains("fallback_mode"), "general config should include fallback mode");
		assertTrue(generalJson.contains("affects_village_farm_generation"), "general config should include the village farm generation toggle");
		assertTrue(generalJson.contains("dimensions"), "general config should include dimension modes");
		assertTrue(generalJson.contains("minecraft:overworld"), "general config should include overworld defaults");
		assertTrue(explicitJson.contains("biomes"), "Explicit mode config should be biome-first");
		assertTrue(thresholdJson.contains("crops"), "Threshold mode config should be crop-first");
		assertTrue(thresholdJson.contains("minecraft:wheat"), "Threshold mode config should include default crop rules");
		assertTrue(thresholdJson.contains("bonemeal-required"), "Threshold mode defaults should serialize bonemeal-required as the default crop behavior");
		assertTrue(!thresholdJson.contains("rainfall"), "Threshold mode config should not serialize removed rainfall values");

		ExplicitModeRules customExplicitRules = new ExplicitModeRules(
				CropBehavior.GROWABLE,
				Map.of(id("minecraft:wheat"), Map.of(id("minecraft:desert"), CropBehavior.UNPLANTABLE))
		);
		CropBiomeLimiterConfig customExplicitConfig = new CropBiomeLimiterConfig(
				GeneralOptions.defaults(),
				customExplicitRules,
				Map.of(Level.OVERWORLD, customExplicitRules)
		);
		String customExplicitJson = CropBiomeLimiterConfigLoader.explicitModeJson(customExplicitConfig).toString();
		assertBefore(customExplicitJson, "minecraft:desert", "minecraft:wheat", "Explicit mode should serialize biome keys before crop keys");
	}

	private static void exportsConfigAppSchemaContract() {
		JsonObject schema = ConfigAppSchema.metadataJson();
		JsonObject files = schema.getAsJsonObject("config_files");
		String schemaJson = schema.toString();

		assertEquals(ConfigAppSchema.SCHEMA_VERSION, schema.get("schema_version").getAsInt(), "schema metadata should report the shared schema version");
		assertEquals(CropBiomeLimiterConfigLoader.CONFIG_DIRECTORY_NAME, schema.get("config_directory").getAsString(), "schema metadata should report the config folder");
		assertEquals(CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME, files.get("general").getAsString(), "schema metadata should report the general config file");
		assertEquals(CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME, files.get("explicit_mode").getAsString(), "schema metadata should report the Explicit mode config file");
		assertEquals(CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME, files.get("threshold_mode").getAsString(), "schema metadata should report the Threshold mode config file");
		assertEquals(ConfigAppSchema.REGISTRY_SNAPSHOT_FILE_NAME, schema.get("registry_snapshot_file").getAsString(), "schema metadata should report the registry snapshot file");
		assertTrue(schemaJson.contains("Explicit mode"), "schema metadata should include the canonical Explicit mode label");
		assertTrue(schemaJson.contains("Threshold mode"), "schema metadata should include the canonical Threshold mode label");
		assertTrue(schemaJson.contains("allows_natural_growth"), "schema metadata should expose behavior action meanings");
		assertTrue(schemaJson.contains("affects_village_farm_generation"), "schema metadata should expose the village farm generation toggle");
		assertTrue(schemaJson.contains("has_precipitation"), "schema metadata should expose the Threshold mode precipitation input");
		assertTrue(!schemaJson.contains("rainfall"), "schema metadata should not reintroduce removed rainfall values");
	}

	private static void defaultVanillaRulesCoverKeyPlants() {
		CropBiomeLimiterConfig defaults = DefaultCropBiomeConfig.create();
		assertDefaultRulesCoverTrackedVanillaGrowables((ThresholdModeRules) defaults.rulesFor(Level.OVERWORLD), "Overworld");
		assertDefaultRulesCoverTrackedVanillaGrowables((ThresholdModeRules) defaults.rulesFor(Level.NETHER), "Nether");
		assertDefaultRulesCoverTrackedVanillaGrowables((ThresholdModeRules) defaults.rulesFor(Level.END), "End");

		ThresholdModeRules overworldRules = (ThresholdModeRules) defaults.rulesFor(Level.OVERWORLD);
		ThresholdCropRule acaciaRule = overworldRules.cropRules().get(id("minecraft:acacia_sapling"));
		assertTrue(acaciaRule.climateRules().getFirst().matches(1.2F, false), "acacia should grow in hot dry climates");
		assertTrue(!acaciaRule.climateRules().getFirst().matches(1.2F, true), "acacia hot-dry rule should reject wet climates");

		ThresholdCropRule mushroomRule = overworldRules.cropRules().get(id("minecraft:brown_mushroom"));
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, mushroomRule.defaultBehavior(), "mushrooms should be bonemeal-only outside matching climates by default");
		assertTrue(!mushroomRule.climateRules().isEmpty(), "mushrooms should get climate rules by default");

		ThresholdCropRule netherWartRule = overworldRules.cropRules().get(id("minecraft:nether_wart"));
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, netherWartRule.defaultBehavior(), "nether wart should be bonemeal-required by default in Overworld Threshold mode");
		assertTrue(!netherWartRule.climateRules().isEmpty(), "wrong-dimension defaults should still document climate rules");
	}

	private static void defaultThresholdRulesUseBiomeClimateStrategy() {
		CropBiomeLimiterConfig defaults = DefaultCropBiomeConfig.create();
		assertTrue(defaults.rulesFor(Level.OVERWORLD) instanceof ThresholdModeRules, "Overworld should use Threshold mode defaults");
		assertTrue(defaults.rulesFor(Level.NETHER) instanceof ThresholdModeRules, "Nether should use Threshold mode defaults");
		assertTrue(defaults.rulesFor(Level.END) instanceof ThresholdModeRules, "End should use Threshold mode defaults");

		ThresholdModeRules overworld = (ThresholdModeRules) defaults.rulesFor(Level.OVERWORLD);
		ThresholdModeRules nether = (ThresholdModeRules) defaults.rulesFor(Level.NETHER);
		ThresholdModeRules end = (ThresholdModeRules) defaults.rulesFor(Level.END);

		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:wheat", 0.8F, true), "wheat should naturally grow in temperate wet biomes");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:wheat", 2.0F, false), "wheat should not claim hot dry carrot village climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:wheat", 0.0F, true), "wheat should not claim snowy village crop defaults");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:wheat", 0.25F, true), "wheat should not claim taiga village crop defaults");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:carrots", 0.0F, true), "carrots should not claim snowy village climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:carrots", 0.8F, true), "carrots should overlap plains village climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:carrots", 0.2F, true), "carrots should not overlap taiga village potato defaults");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:carrots", 2.0F, false), "carrots should match hot dry village climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:potatoes", 0.0F, true), "potatoes should overlap snowy village climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:potatoes", 0.2F, true), "potatoes should match taiga village climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:potatoes", 0.8F, true), "potatoes should not be a plains default crop");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:beetroots", 0.0F, true), "beetroots should match snowy village climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:beetroots", 0.25F, true), "beetroots should overlap taiga village climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:beetroots", 0.8F, true), "beetroots should not claim plains village climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:beetroots", 2.0F, false), "beetroots should not claim hot dry village climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:cactus", 2.0F, false), "cactus should naturally grow in desert-like climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:cactus", 0.0F, true), "cactus should be bonemeal-only in freezing wet biomes by default");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:cactus", 1.2F, false), "cactus should not claim merely warm dry climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:cactus_flower", 2.0F, false), "cactus flowers should grow on desert-like cactus climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:bush", 0.5F, true), "bush should be allowed in cherry grove-like climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:bush", 0.6F, true), "bush should be allowed in birch forest-like climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:bush", 2.0F, false), "bush should remain allowed in hot dry climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:cocoa", 0.95F, true), "cocoa should naturally grow in tropical wet biomes");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:cocoa", 0.8F, true), "cocoa should be bonemeal-only in ordinary plains-like biomes by default");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:jungle_sapling", 0.95F, true), "jungle saplings should naturally grow in jungle climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:bamboo", 0.95F, true), "bamboo should naturally grow in bamboo jungle climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:melon_stem", 2.0F, false), "melon stems should not match hot dry village farms");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:melon_stem", 0.8F, true), "melon stems should not be a plains village crop");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:pumpkin_stem", 0.0F, true), "pumpkins should match broad vanilla pumpkin patch generation");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:pumpkin_stem", 2.0F, false), "pumpkins should match desert and badlands patch generation");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:short_grass", 2.0F, false), "short grass should match hot dry vanilla ground cover generation");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:tall_grass", 2.0F, false), "tall grass should match hot dry vanilla savanna generation");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:firefly_bush", 2.0F, false), "firefly bush should match hot dry vanilla ground cover generation");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:fern", 0.95F, true), "fern should match jungle ground cover generation");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:sugar_cane", 2.0F, false), "sugar cane should remain valid in hot dry biomes where vanilla can generate it near water");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:sugar_cane", 0.0F, true), "sugar cane should be bonemeal-only in freezing biomes by default");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:spruce_sapling", -0.5F, true), "spruce should naturally grow in cold wet biomes");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:spruce_sapling", 0.8F, true), "spruce should not grow by default in plains tree climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:acacia_sapling", 2.0F, false), "acacia should naturally grow in hot dry savanna-like climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:oak_sapling", 0.2F, true), "oak should cover windswept forest-like tree climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:birch_sapling", 0.6F, true), "birch should naturally grow in birch forest climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:birch_sapling", 0.2F, true), "birch should not claim taiga-like tree climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:melon_stem", 0.95F, true), "melon should naturally grow in jungle-like climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:mangrove_propagule", 0.8F, true), "mangrove propagules should naturally grow in mangrove swamp climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:cherry_sapling", 0.5F, true), "cherry saplings should naturally grow in cherry grove climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:cherry_sapling", 0.8F, true), "cherry saplings should stay tied to cherry grove-like climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:pale_oak_sapling", 0.7F, true), "pale oak saplings should naturally grow in pale garden-like forest climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:cave_vines", 0.5F, true), "cave vines should naturally grow in lush cave climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:big_dripleaf", 0.5F, true), "dripleaf should naturally grow in lush cave climates");
		assertEquals(CropBehavior.GROWABLE, resolve(overworld, "minecraft:seagrass", 0.5F, true), "seagrass should naturally grow in wet river and ocean climates");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(overworld, "minecraft:nether_wart", 2.0F, false), "Nether crops should not become Overworld desert crops");

		assertEquals(CropBehavior.GROWABLE, resolve(nether, "minecraft:nether_wart", 2.0F, false), "nether wart should naturally grow in Nether climates");
		assertEquals(CropBehavior.GROWABLE, resolve(nether, "minecraft:crimson_fungus", 2.0F, false), "crimson fungus should naturally grow in Nether climates");
		assertEquals(CropBehavior.GROWABLE, resolve(nether, "minecraft:warped_fungus", 2.0F, false), "warped fungus should naturally grow in Nether climates");
		assertEquals(CropBehavior.GROWABLE, resolve(nether, "minecraft:weeping_vines", 2.0F, false), "weeping vines should naturally grow in Nether climates");
		assertEquals(CropBehavior.GROWABLE, resolve(nether, "minecraft:twisting_vines", 2.0F, false), "twisting vines should naturally grow in Nether climates");
		assertEquals(CropBehavior.GROWABLE, resolve(nether, "minecraft:bush", 2.0F, false), "cosmetic bush should remain allowed by Nether defaults");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(nether, "minecraft:wheat", 2.0F, false), "overworld crops should be bonemeal-only by Nether defaults");

		assertEquals(CropBehavior.GROWABLE, resolve(end, "minecraft:chorus_flower", 0.5F, false), "chorus should naturally grow in End climates");
		assertEquals(CropBehavior.GROWABLE, resolve(end, "minecraft:bush", 0.5F, false), "cosmetic bush should remain allowed by End defaults");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(end, "minecraft:wheat", 0.5F, false), "overworld crops should be bonemeal-only by End defaults");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, resolve(end, "minecraft:nether_wart", 0.5F, false), "Nether crops should be bonemeal-only by End defaults");

		for (ClimateSample sample : representativeVanillaClimateSamples()) {
			DimensionRules rules = defaults.rulesFor(sample.dimension());
			assertTrue(rules instanceof ThresholdModeRules, sample.name() + " should use Threshold mode rules");
			ThresholdModeRules thresholdRules = (ThresholdModeRules) rules;
			assertHasBehavior(thresholdRules, sample, CropBehavior.GROWABLE);
			assertHasBehavior(thresholdRules, sample, CropBehavior.BONEMEAL_REQUIRED);
		}
	}

	private static void generatedThresholdConfigDocumentsEveryDefaultCropBehavior() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-generated-threshold-test");
		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(result.createdDefault(), "fresh startup should write default split config files");

		JsonObject root = JsonParser.parseString(Files.readString(configPath(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME))).getAsJsonObject();
		JsonObject dimensions = root.getAsJsonObject("dimensions");
		assertGeneratedDimensionRulesDocumentCropBehavior(dimensions, "minecraft:overworld");
		assertGeneratedDimensionRulesDocumentCropBehavior(dimensions, "minecraft:the_nether");
		assertGeneratedDimensionRulesDocumentCropBehavior(dimensions, "minecraft:the_end");
	}

	private static void generatedExplicitConfigMirrorsThresholdDefaults() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-generated-explicit-test");
		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(result.createdDefault(), "fresh startup should write default split config files");

		JsonObject root = JsonParser.parseString(Files.readString(configPath(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME))).getAsJsonObject();
		JsonObject dimensions = root.getAsJsonObject("dimensions");
		JsonObject overworld = dimensions.getAsJsonObject("minecraft:overworld");
		JsonObject nether = dimensions.getAsJsonObject("minecraft:the_nether");
		JsonObject end = dimensions.getAsJsonObject("minecraft:the_end");

		assertEquals("growable", explicitBehavior(overworld, "minecraft:plains", "minecraft:wheat"), "Generated Explicit mode should mirror Threshold mode for wheat in plains");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:savanna", "minecraft:wheat"), "Generated Explicit mode should not make wheat a hot dry village crop");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:snowy_plains", "minecraft:wheat"), "Generated Explicit mode should not make wheat a snowy village crop");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:snowy_plains", "minecraft:carrots"), "Generated Explicit mode should not make carrots a snowy village crop");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:desert", "minecraft:carrots"), "Generated Explicit mode should mirror Threshold mode for carrots in hot dry villages");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:plains", "minecraft:carrots"), "Generated Explicit mode should mirror Threshold mode for carrots in plains overlap");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:snowy_plains", "minecraft:beetroots"), "Generated Explicit mode should mirror Threshold mode for beetroots in snowy villages");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:taiga", "minecraft:beetroots"), "Generated Explicit mode should mirror Threshold mode for beetroots in taiga overlap");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:taiga", "minecraft:potatoes"), "Generated Explicit mode should mirror Threshold mode for potatoes in taiga villages");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:snowy_plains", "minecraft:potatoes"), "Generated Explicit mode should mirror Threshold mode for potatoes in snowy overlap");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:plains", "minecraft:potatoes"), "Generated Explicit mode should not make potatoes a plains default crop");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:desert", "minecraft:beetroots"), "Generated Explicit mode should not make beetroots a hot dry village crop");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:desert", "minecraft:cactus"), "Generated Explicit mode should mirror Threshold mode for cactus in deserts");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:ice_spikes", "minecraft:cactus"), "Generated Explicit mode should mirror Threshold mode for cactus in icy biomes");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:savanna", "minecraft:cactus"), "Generated Explicit mode should mirror the hot dry cactus bucket");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:savanna", "minecraft:short_grass"), "Generated Explicit mode should mirror vanilla savanna short grass generation");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:savanna", "minecraft:tall_grass"), "Generated Explicit mode should mirror vanilla savanna tall grass generation");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:badlands", "minecraft:firefly_bush"), "Generated Explicit mode should mirror vanilla badlands firefly bush generation");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:desert", "minecraft:melon_stem"), "Generated Explicit mode should not make melons a desert village crop");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:savanna", "minecraft:melon_stem"), "Generated Explicit mode should not make melons a savanna village crop");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:plains", "minecraft:melon_stem"), "Generated Explicit mode should not make melons a plains default crop");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:desert", "minecraft:pumpkin_stem"), "Generated Explicit mode should mirror broad vanilla pumpkin patch generation");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:savanna", "minecraft:acacia_sapling"), "Generated Explicit mode should mirror acacia tree generation in savannas");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:desert", "minecraft:acacia_sapling"), "Generated Explicit mode should mirror the hot dry tree bucket");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:birch_forest", "minecraft:birch_sapling"), "Generated Explicit mode should mirror birch tree generation");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:taiga", "minecraft:birch_sapling"), "Generated Explicit mode should not make birch a taiga default tree");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:cherry_grove", "minecraft:bush"), "Generated Explicit mode should mirror Threshold mode for bush in cherry groves");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:birch_forest", "minecraft:bush"), "Generated Explicit mode should mirror Threshold mode for bush in birch forests");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:jungle", "minecraft:cocoa"), "Generated Explicit mode should mirror Threshold mode for cocoa in jungles");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:jungle", "minecraft:fern"), "Generated Explicit mode should mirror vanilla jungle fern generation");
		assertEquals("bonemeal-required", explicitBehavior(overworld, "minecraft:plains", "minecraft:cocoa"), "Generated Explicit mode should mirror Threshold mode for cocoa in ordinary wet biomes");
		assertEquals("growable", explicitBehavior(nether, "minecraft:nether_wastes", "minecraft:nether_wart"), "Generated Explicit mode should mirror Threshold mode for nether wart in Nether climates");
		assertEquals("growable", explicitBehavior(nether, "minecraft:nether_wastes", "minecraft:bush"), "Generated Explicit mode should mirror Threshold mode for bush in Nether dimensions");
		assertEquals("growable", explicitBehavior(end, "minecraft:the_end", "minecraft:chorus_flower"), "Generated Explicit mode should mirror Threshold mode for chorus in End climates");
		assertEquals("growable", explicitBehavior(end, "minecraft:the_end", "minecraft:bush"), "Generated Explicit mode should mirror Threshold mode for bush in End dimensions");
	}

	private static void assertGeneratedDimensionRulesDocumentCropBehavior(JsonObject dimensions, String dimensionId) {
		JsonObject crops = dimensions.getAsJsonObject(dimensionId).getAsJsonObject("crops");
		for (Block block : GrowableBlockClassifier.defaultGrowableBlocks()) {
			String cropId = BuiltInRegistries.BLOCK.getKey(block).toString();
			assertTrue(crops.has(cropId), "generated Threshold mode config should include " + cropId + " in " + dimensionId);
			JsonObject cropRule = crops.getAsJsonObject(cropId);
			boolean alwaysGrowable = "growable".equals(cropRule.get("default_behavior").getAsString())
					&& cropRule.getAsJsonArray("climate_rules").size() == 0;
			assertTrue(alwaysGrowable || cropRule.getAsJsonArray("climate_rules").size() > 0,
					"generated Threshold mode config should include climate rules or an always-growable default for " + cropId + " in " + dimensionId);
		}
	}

	private static String explicitBehavior(JsonObject dimensionRules, String biomeId, String cropId) {
		String defaultBehavior = dimensionRules.get("default_behavior").getAsString();
		JsonObject biomes = dimensionRules.getAsJsonObject("biomes");
		if (!biomes.has(biomeId)) {
			return defaultBehavior;
		}
		JsonObject crops = biomes.getAsJsonObject(biomeId);
		if (!crops.has(cropId)) {
			return defaultBehavior;
		}
		return crops.get(cropId).getAsString();
	}

	private static void assertDefaultRulesCoverTrackedVanillaGrowables(ThresholdModeRules rules, String dimensionName) {
		for (Block block : GrowableBlockClassifier.defaultGrowableBlocks()) {
			Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
			assertTrue(rules.cropRules().containsKey(blockId), dimensionName + " default rules should include tracked vanilla growable " + blockId);
			ThresholdCropRule rule = rules.cropRules().get(blockId);
			assertTrue(rule.defaultBehavior() == CropBehavior.GROWABLE || !rule.climateRules().isEmpty(),
					dimensionName + " default rules should include climate rules or an always-growable default for " + blockId);
		}
	}

	private static List<ClimateSample> representativeVanillaClimateSamples() {
		return List.of(
				new ClimateSample(Level.OVERWORLD, "frozen peaks", -0.7F, true),
				new ClimateSample(Level.OVERWORLD, "snowy taiga", -0.5F, true),
				new ClimateSample(Level.OVERWORLD, "snowy slopes", -0.3F, true),
				new ClimateSample(Level.OVERWORLD, "grove", -0.2F, true),
				new ClimateSample(Level.OVERWORLD, "snowy plains", 0.0F, true),
				new ClimateSample(Level.OVERWORLD, "snowy beach", 0.05F, true),
				new ClimateSample(Level.OVERWORLD, "taiga", 0.2F, true),
				new ClimateSample(Level.OVERWORLD, "old growth spruce taiga", 0.25F, true),
				new ClimateSample(Level.OVERWORLD, "old growth pine taiga", 0.3F, true),
				new ClimateSample(Level.OVERWORLD, "meadow", 0.5F, true),
				new ClimateSample(Level.OVERWORLD, "the void", 0.5F, false),
				new ClimateSample(Level.OVERWORLD, "birch forest", 0.6F, true),
				new ClimateSample(Level.OVERWORLD, "forest", 0.7F, true),
				new ClimateSample(Level.OVERWORLD, "plains", 0.8F, true),
				new ClimateSample(Level.OVERWORLD, "mushroom fields", 0.9F, true),
				new ClimateSample(Level.OVERWORLD, "jungle", 0.95F, true),
				new ClimateSample(Level.OVERWORLD, "stony peaks", 1.0F, true),
				new ClimateSample(Level.OVERWORLD, "desert", 2.0F, false),
				new ClimateSample(Level.NETHER, "nether", 2.0F, false),
				new ClimateSample(Level.END, "end", 0.5F, false)
		);
	}

	private static void assertHasBehavior(ThresholdModeRules rules, ClimateSample sample, CropBehavior behavior) {
		for (Identifier cropId : rules.cropRules().keySet()) {
			if (rules.resolve(cropId, Holder.direct(biome(sample.temperature(), sample.hasPrecipitation()))) == behavior) {
				return;
			}
		}
		throw new AssertionError(sample.name() + " should have at least one " + behavior.serializedName() + " default crop");
	}

	private static CropBehavior resolve(ThresholdModeRules rules, String cropId, float temperature, boolean hasPrecipitation) {
		return rules.resolve(id(cropId), Holder.direct(biome(temperature, hasPrecipitation)));
	}

	private static Biome biome(float temperature, boolean hasPrecipitation) {
		return new Biome.BiomeBuilder()
				.hasPrecipitation(hasPrecipitation)
				.temperature(temperature)
				.downfall(hasPrecipitation ? 1.0F : 0.0F)
				.specialEffects(new BiomeSpecialEffects.Builder()
						.waterColor(0)
						.build())
				.mobSpawnSettings(MobSpawnSettings.EMPTY)
				.generationSettings(BiomeGenerationSettings.EMPTY)
				.build();
	}

	private record ClimateSample(ResourceKey<Level> dimension, String name, float temperature, boolean hasPrecipitation) {
	}

	private static void writeConfig(Path directory, String fileName, String content) throws IOException {
		Path configPath = configPath(directory, fileName);
		Files.createDirectories(configPath.getParent());
		Files.writeString(configPath, content, StandardCharsets.UTF_8);
	}

	private static Path configDirectory(Path directory) {
		return directory.resolve(CropBiomeLimiterConfigLoader.CONFIG_DIRECTORY_NAME);
	}

	private static Path configPath(Path directory, String fileName) {
		return configDirectory(directory).resolve(fileName);
	}

	private static Identifier id(String value) {
		return Identifier.parse(value);
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertNotNull(Object value, String message) {
		if (value == null) {
			throw new AssertionError(message);
		}
	}

	private static void assertBefore(String value, String earlier, String later, String message) {
		int earlierIndex = value.indexOf(earlier);
		int laterIndex = value.indexOf(later);
		if (earlierIndex < 0 || laterIndex < 0 || earlierIndex >= laterIndex) {
			throw new AssertionError(message + " Expected " + earlier + " before " + later + " in " + value);
		}
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " Expected: " + expected + ", actual: " + actual);
		}
	}
}
