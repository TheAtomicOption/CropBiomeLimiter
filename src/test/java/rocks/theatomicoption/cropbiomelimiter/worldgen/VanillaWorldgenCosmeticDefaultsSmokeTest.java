package rocks.theatomicoption.cropbiomelimiter.worldgen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import rocks.theatomicoption.cropbiomelimiter.config.ConfigLoadResult;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfigLoader;
import rocks.theatomicoption.cropbiomelimiter.logic.GrowableBlockClassifier;

public final class VanillaWorldgenCosmeticDefaultsSmokeTest {
	private static final Set<String> NEWER_THAN_1_20_1_BLOCKS = Set.of(
			"minecraft:bush",
			"minecraft:cactus_flower",
			"minecraft:firefly_bush",
			"minecraft:pale_hanging_moss",
			"minecraft:pale_moss_block",
			"minecraft:pale_moss_carpet",
			"minecraft:pale_oak_sapling",
			"minecraft:short_dry_grass",
			"minecraft:short_grass",
			"minecraft:tall_dry_grass",
			"minecraft:wildflowers"
	);

	private VanillaWorldgenCosmeticDefaultsSmokeTest() {
	}

	public static void run() throws Exception {
		targetVersionClassifierIncludesVanillaCosmetics();
		generatedDefaultsExcludeNewerVanillaPlants();
		generatedExplicitDefaultsKeepVanillaCosmeticsGrowable();
	}

	private static void targetVersionClassifierIncludesVanillaCosmetics() {
		Set<String> defaultBlocks = GrowableBlockClassifier.defaultGrowableBlocks().stream()
				.map(block -> BuiltInRegistries.BLOCK.getKey(block).toString())
				.collect(Collectors.toSet());

		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.GRASS).toString()), "1.20.1 defaults should track grass");
		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.TALL_GRASS).toString()), "1.20.1 defaults should track tall grass");
		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.FERN).toString()), "1.20.1 defaults should track fern");
		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.LARGE_FERN).toString()), "1.20.1 defaults should track large fern");
		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.MOSS_BLOCK).toString()), "1.20.1 defaults should track moss block");
		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.MOSS_CARPET).toString()), "1.20.1 defaults should track moss carpet");
		assertTrue(defaultBlocks.contains(BuiltInRegistries.BLOCK.getKey(Blocks.HANGING_ROOTS).toString()), "1.20.1 defaults should track hanging roots");

		for (String blockId : NEWER_THAN_1_20_1_BLOCKS) {
			assertTrue(!defaultBlocks.contains(blockId), "1.20.1 classifier defaults should not include newer block " + blockId);
		}
	}

	private static void generatedDefaultsExcludeNewerVanillaPlants() throws Exception {
		Path directory = Files.createTempDirectory("cropbiomelimiter-target-version-defaults-test");
		ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		assertTrue(result.createdDefault(), "fresh startup should write default config files");

		String thresholdJson = Files.readString(configPath(directory, CropBiomeLimiterConfigLoader.THRESHOLD_MODE_FILE_NAME));
		String explicitJson = Files.readString(configPath(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME));
		for (String blockId : NEWER_THAN_1_20_1_BLOCKS) {
			assertTrue(!thresholdJson.contains(blockId), "1.20.1 generated Threshold defaults should not include " + blockId);
			assertTrue(!explicitJson.contains(blockId), "1.20.1 generated Explicit defaults should not include " + blockId);
		}
	}

	private static void generatedExplicitDefaultsKeepVanillaCosmeticsGrowable() throws Exception {
		Path directory = Files.createTempDirectory("cropbiomelimiter-cosmetic-defaults-test");
		CropBiomeLimiterConfigLoader.tryLoadConfig(directory);

		JsonObject root = JsonParser.parseString(Files.readString(configPath(directory, CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME))).getAsJsonObject();
		JsonObject overworld = root.getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:savanna", "minecraft:grass"), "grass should remain growable in savannas");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:savanna", "minecraft:tall_grass"), "tall grass should remain growable in savannas");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:jungle", "minecraft:fern"), "ferns should remain growable in jungles");
		assertEquals("growable", explicitBehavior(overworld, "minecraft:lush_caves", "minecraft:moss_block"), "moss should remain growable in lush caves");
	}

	private static Path configPath(Path minecraftConfigDirectory, String fileName) {
		return minecraftConfigDirectory
				.resolve(CropBiomeLimiterConfigLoader.CONFIG_DIRECTORY_NAME)
				.resolve(fileName);
	}

	private static String explicitBehavior(JsonObject dimensionRules, String biomeId, String cropId) {
		JsonObject biomeRules = dimensionRules.getAsJsonObject("biomes").getAsJsonObject(biomeId);
		if (biomeRules == null || !biomeRules.has(cropId)) {
			return dimensionRules.get("default_behavior").getAsString();
		}
		return biomeRules.get(cropId).getAsString();
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " Expected " + expected + " but got " + actual + ".");
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
