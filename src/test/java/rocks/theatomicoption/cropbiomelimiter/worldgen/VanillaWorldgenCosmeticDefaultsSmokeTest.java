package rocks.theatomicoption.cropbiomelimiter.worldgen;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RotatedBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfigLoader;

public final class VanillaWorldgenCosmeticDefaultsSmokeTest {
	private static final Identifier SAVANNA = id("minecraft:savanna");
	private static final Identifier SHORT_GRASS = id("minecraft:short_grass");
	private static final Identifier TALL_GRASS = id("minecraft:tall_grass");

	private static final Set<Identifier> OVERWORLD_BIOMES = Set.of(
			id("minecraft:the_void"),
			id("minecraft:plains"),
			id("minecraft:sunflower_plains"),
			id("minecraft:snowy_plains"),
			id("minecraft:ice_spikes"),
			id("minecraft:desert"),
			id("minecraft:swamp"),
			id("minecraft:mangrove_swamp"),
			id("minecraft:forest"),
			id("minecraft:flower_forest"),
			id("minecraft:birch_forest"),
			id("minecraft:dark_forest"),
			id("minecraft:pale_garden"),
			id("minecraft:old_growth_birch_forest"),
			id("minecraft:old_growth_pine_taiga"),
			id("minecraft:old_growth_spruce_taiga"),
			id("minecraft:taiga"),
			id("minecraft:snowy_taiga"),
			id("minecraft:savanna"),
			id("minecraft:savanna_plateau"),
			id("minecraft:windswept_hills"),
			id("minecraft:windswept_gravelly_hills"),
			id("minecraft:windswept_forest"),
			id("minecraft:windswept_savanna"),
			id("minecraft:jungle"),
			id("minecraft:sparse_jungle"),
			id("minecraft:bamboo_jungle"),
			id("minecraft:badlands"),
			id("minecraft:eroded_badlands"),
			id("minecraft:wooded_badlands"),
			id("minecraft:meadow"),
			id("minecraft:cherry_grove"),
			id("minecraft:grove"),
			id("minecraft:snowy_slopes"),
			id("minecraft:frozen_peaks"),
			id("minecraft:jagged_peaks"),
			id("minecraft:stony_peaks"),
			id("minecraft:river"),
			id("minecraft:frozen_river"),
			id("minecraft:beach"),
			id("minecraft:snowy_beach"),
			id("minecraft:stony_shore"),
			id("minecraft:warm_ocean"),
			id("minecraft:lukewarm_ocean"),
			id("minecraft:deep_lukewarm_ocean"),
			id("minecraft:ocean"),
			id("minecraft:deep_ocean"),
			id("minecraft:cold_ocean"),
			id("minecraft:deep_cold_ocean"),
			id("minecraft:frozen_ocean"),
			id("minecraft:deep_frozen_ocean"),
			id("minecraft:mushroom_fields"),
			id("minecraft:dripstone_caves"),
			id("minecraft:lush_caves"),
			id("minecraft:deep_dark")
	);

	private static final Set<Identifier> AUDITED_COSMETIC_BLOCKS = Set.of(
			blockId(Blocks.AZALEA),
			blockId(Blocks.FLOWERING_AZALEA),
			blockId(Blocks.MOSS_BLOCK),
			blockId(Blocks.MOSS_CARPET),
			blockId(Blocks.PALE_MOSS_BLOCK),
			blockId(Blocks.PALE_MOSS_CARPET),
			blockId(Blocks.PALE_HANGING_MOSS),
			blockId(Blocks.HANGING_ROOTS),
			blockId(Blocks.SHORT_GRASS),
			blockId(Blocks.TALL_GRASS),
			blockId(Blocks.FERN),
			blockId(Blocks.LARGE_FERN),
			blockId(Blocks.BUSH),
			blockId(Blocks.SHORT_DRY_GRASS),
			blockId(Blocks.TALL_DRY_GRASS),
			blockId(Blocks.WILDFLOWERS),
			blockId(Blocks.FIREFLY_BUSH),
			blockId(Blocks.CACTUS_FLOWER)
	);

	private VanillaWorldgenCosmeticDefaultsSmokeTest() {
	}

	public static void run() throws Exception {
		Map<Identifier, Set<Identifier>> naturalBlocksByBiome = naturalCosmeticBlocksByBiome();
		Set<Identifier> savannaBlocks = naturalBlocksByBiome.getOrDefault(SAVANNA, Set.of());
		assertTrue(savannaBlocks.contains(SHORT_GRASS), "vanilla worldgen audit should find short grass in savanna");
		assertTrue(savannaBlocks.contains(TALL_GRASS), "vanilla worldgen audit should find tall grass in savanna");

		JsonObject overworldDefaults = generatedExplicitOverworldDefaults();
		List<String> failures = new ArrayList<>();
		for (Map.Entry<Identifier, Set<Identifier>> entry : naturalBlocksByBiome.entrySet()) {
			Identifier biomeId = entry.getKey();
			for (Identifier blockId : entry.getValue()) {
				String behavior = explicitBehavior(overworldDefaults, biomeId, blockId);
				if (!"growable".equals(behavior)) {
					failures.add(blockId + " naturally appears in " + biomeId + " but generated Explicit mode default is " + behavior);
				}
			}
		}

		assertTrue(failures.isEmpty(), "tracked cosmetic plants should be growable where vanilla worldgen places them: " + failures);
	}

	private static Map<Identifier, Set<Identifier>> naturalCosmeticBlocksByBiome() {
		HolderLookup.Provider lookup = VanillaRegistries.createLookup();
		HolderLookup.RegistryLookup<Biome> biomes = lookup.lookupOrThrow(Registries.BIOME);
		Map<Identifier, Set<Identifier>> blocksByBiome = new LinkedHashMap<>();

		biomes.listElements()
				.sorted(Comparator.comparing(holder -> holder.key().identifier().toString()))
				.forEach(holder -> collectNaturalCosmeticBlocks(holder, blocksByBiome));

		return blocksByBiome;
	}

	private static void collectNaturalCosmeticBlocks(
			Holder.Reference<Biome> biomeHolder,
			Map<Identifier, Set<Identifier>> blocksByBiome
	) {
		Identifier biomeId = biomeHolder.key().identifier();
		if (!OVERWORLD_BIOMES.contains(biomeId)) {
			return;
		}

		Set<Identifier> blockIds = new TreeSet<>(Comparator.comparing(Identifier::toString));
		Set<ConfiguredFeature<?, ?>> seenFeatures = Collections.newSetFromMap(new IdentityHashMap<>());
		for (HolderSet<PlacedFeature> featureStep : biomeHolder.value().getGenerationSettings().features()) {
			for (Holder<PlacedFeature> placedFeature : featureStep) {
				collectPlacedFeature(placedFeature.value(), blockIds, seenFeatures);
			}
		}

		blockIds.retainAll(AUDITED_COSMETIC_BLOCKS);
		if (!blockIds.isEmpty()) {
			blocksByBiome.put(biomeId, blockIds);
		}
	}

	private static void collectPlacedFeature(
			PlacedFeature placedFeature,
			Set<Identifier> blockIds,
			Set<ConfiguredFeature<?, ?>> seenFeatures
	) {
		placedFeature.getFeatures()
				.forEach(configuredFeature -> collectConfiguredFeature(configuredFeature.value(), blockIds, seenFeatures));
	}

	private static void collectConfiguredFeature(
			ConfiguredFeature<?, ?> configuredFeature,
			Set<Identifier> blockIds,
			Set<ConfiguredFeature<?, ?>> seenFeatures
	) {
		if (!seenFeatures.add(configuredFeature)) {
			return;
		}

		collectFeatureConfiguration(configuredFeature.config(), blockIds, seenFeatures);
		configuredFeature.getSubFeatures()
				.forEach(subFeature -> collectConfiguredFeature(subFeature.value(), blockIds, seenFeatures));
	}

	private static void collectFeatureConfiguration(
			FeatureConfiguration configuration,
			Set<Identifier> blockIds,
			Set<ConfiguredFeature<?, ?>> seenFeatures
	) {
		if (configuration instanceof SimpleBlockConfiguration simpleBlock) {
			collectProvider(simpleBlock.toPlace(), blockIds);
		} else if (configuration instanceof BlockStateConfiguration blockState) {
			addBlock(blockState.state.getBlock(), blockIds);
		} else if (configuration instanceof BlockPileConfiguration blockPile) {
			collectProvider(blockPile.stateProvider, blockIds);
		} else if (configuration instanceof BlockColumnConfiguration blockColumn) {
			for (BlockColumnConfiguration.Layer layer : blockColumn.layers()) {
				collectProvider(layer.state(), blockIds);
			}
		} else if (configuration instanceof VegetationPatchConfiguration vegetationPatch) {
			collectProvider(vegetationPatch.groundState, blockIds);
			collectPlacedFeature(vegetationPatch.vegetationFeature.value(), blockIds, seenFeatures);
		} else if (configuration instanceof MultifaceGrowthConfiguration multifaceGrowth) {
			addBlock(multifaceGrowth.placeBlock, blockIds);
		} else if (configuration instanceof RandomFeatureConfiguration randomFeature) {
			for (WeightedPlacedFeature weightedFeature : randomFeature.features) {
				collectPlacedFeature(weightedFeature.feature.value(), blockIds, seenFeatures);
			}
			collectPlacedFeature(randomFeature.defaultFeature.value(), blockIds, seenFeatures);
		} else if (configuration instanceof RandomBooleanFeatureConfiguration randomBooleanFeature) {
			collectPlacedFeature(randomBooleanFeature.featureTrue.value(), blockIds, seenFeatures);
			collectPlacedFeature(randomBooleanFeature.featureFalse.value(), blockIds, seenFeatures);
		} else if (configuration instanceof SimpleRandomFeatureConfiguration simpleRandomFeature) {
			for (Holder<PlacedFeature> placedFeature : simpleRandomFeature.features) {
				collectPlacedFeature(placedFeature.value(), blockIds, seenFeatures);
			}
		} else if (configuration instanceof RootSystemConfiguration rootSystem) {
			collectProvider(rootSystem.rootStateProvider, blockIds);
			collectProvider(rootSystem.hangingRootStateProvider, blockIds);
			collectPlacedFeature(rootSystem.treeFeature.value(), blockIds, seenFeatures);
		}
	}

	private static void collectProvider(BlockStateProvider provider, Set<Identifier> blockIds) {
		if (provider instanceof SimpleStateProvider) {
			addBlockState((BlockState) field(provider, "state"), blockIds);
		} else if (provider instanceof WeightedStateProvider) {
			WeightedList<BlockState> weightedList = cast(field(provider, "weightedList"));
			for (Weighted<BlockState> weighted : weightedList.unwrap()) {
				addBlockState(weighted.value(), blockIds);
			}
		} else if (provider instanceof NoiseThresholdProvider) {
			addBlockState((BlockState) field(provider, "defaultState"), blockIds);
			addBlockStates(cast(field(provider, "lowStates")), blockIds);
			addBlockStates(cast(field(provider, "highStates")), blockIds);
		} else if (provider instanceof NoiseProvider) {
			addBlockStates(cast(field(provider, "states")), blockIds);
		} else if (provider instanceof RandomizedIntStateProvider) {
			collectProvider((BlockStateProvider) field(provider, "source"), blockIds);
		} else if (provider instanceof RotatedBlockProvider) {
			addBlock((Block) field(provider, "block"), blockIds);
		} else {
			addSampledProviderState(provider, blockIds);
		}
	}

	private static void addSampledProviderState(BlockStateProvider provider, Set<Identifier> blockIds) {
		try {
			addBlockState(provider.getState(null, RandomSource.create(0L), net.minecraft.core.BlockPos.ZERO), blockIds);
		} catch (RuntimeException ignored) {
			// Unknown provider shapes are ignored by this audit unless they expose states through known fields.
		}
	}

	private static void addBlockStates(List<BlockState> states, Set<Identifier> blockIds) {
		for (BlockState state : states) {
			addBlockState(state, blockIds);
		}
	}

	private static void addBlockState(BlockState state, Set<Identifier> blockIds) {
		if (state != null) {
			addBlock(state.getBlock(), blockIds);
		}
	}

	private static void addBlock(Block block, Set<Identifier> blockIds) {
		blockIds.add(blockId(block));
	}

	private static JsonObject generatedExplicitOverworldDefaults() throws Exception {
		Path directory = Files.createTempDirectory("cropbiomelimiter-vanilla-worldgen-defaults-test");
		CropBiomeLimiterConfigLoader.tryLoadConfig(directory);
		Path explicitConfig = directory
				.resolve(CropBiomeLimiterConfigLoader.CONFIG_DIRECTORY_NAME)
				.resolve(CropBiomeLimiterConfigLoader.EXPLICIT_MODE_FILE_NAME);
		JsonObject root = JsonParser.parseString(Files.readString(explicitConfig)).getAsJsonObject();
		return root.getAsJsonObject("dimensions").getAsJsonObject("minecraft:overworld");
	}

	private static String explicitBehavior(JsonObject dimensionRules, Identifier biomeId, Identifier blockId) {
		String defaultBehavior = dimensionRules.get("default_behavior").getAsString();
		JsonObject biomes = dimensionRules.getAsJsonObject("biomes");
		if (!biomes.has(biomeId.toString())) {
			return defaultBehavior;
		}
		JsonObject crops = biomes.getAsJsonObject(biomeId.toString());
		if (!crops.has(blockId.toString())) {
			return defaultBehavior;
		}
		return crops.get(blockId.toString()).getAsString();
	}

	private static Object field(Object target, String name) {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException exception) {
				type = type.getSuperclass();
			} catch (ReflectiveOperationException exception) {
				throw new AssertionError("Could not read field " + name + " from " + target.getClass().getName(), exception);
			}
		}

		throw new AssertionError("Could not find field " + name + " on " + target.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	private static <T> T cast(Object value) {
		return (T) value;
	}

	private static Identifier blockId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	private static Identifier id(String value) {
		return Identifier.parse(value);
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
