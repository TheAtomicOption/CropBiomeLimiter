package rocks.theatomicoption.cropbiomelimiter.viewer.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfig;
import rocks.theatomicoption.cropbiomelimiter.config.DefaultCropBiomeConfig;
import rocks.theatomicoption.cropbiomelimiter.config.DimensionRules;
import rocks.theatomicoption.cropbiomelimiter.config.ExplicitModeRules;
import rocks.theatomicoption.cropbiomelimiter.config.ThresholdModeRules;
import rocks.theatomicoption.cropbiomelimiter.logic.GrowableBlockClassifier;
import rocks.theatomicoption.cropbiomelimiter.viewer.AtlasBiome;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropBiomeAtlas;

public final class ClientAtlasData {
	private static final Set<Block> NON_CROP_BONEMEAL_TARGETS = Set.of(
			Blocks.GRASS_BLOCK,
			Blocks.ROOTED_DIRT,
			Blocks.NETHERRACK,
			Blocks.CRIMSON_NYLIUM,
			Blocks.WARPED_NYLIUM
	);

	private ClientAtlasData() {
	}

	public static CropBiomeAtlas create() {
		CropBiomeLimiterConfig config = CropBiomeLimiter.cropDecisionService()
				.config()
				.orElseGet(DefaultCropBiomeConfig::create);
		Minecraft minecraft = Minecraft.getInstance();
		RegistryAccess registryAccess = registryAccess(minecraft);
		Collection<ResourceKey<Level>> dimensions = dimensions(minecraft, config);
		List<AtlasBiome> biomes = registryAccess.registry(Registries.BIOME)
				.map(ClientAtlasData::biomes)
				.orElseGet(List::of);
		return CropBiomeAtlas.build(config, dimensions, crops(config), biomes);
	}

	private static RegistryAccess registryAccess(Minecraft minecraft) {
		if (minecraft.getConnection() != null) {
			return minecraft.getConnection().registryAccess();
		}
		if (minecraft.level != null) {
			return minecraft.level.registryAccess();
		}
		return RegistryAccess.EMPTY;
	}

	private static Collection<ResourceKey<Level>> dimensions(Minecraft minecraft, CropBiomeLimiterConfig config) {
		Set<ResourceKey<Level>> dimensions = new LinkedHashSet<>();
		if (minecraft.getConnection() != null) {
			dimensions.addAll(minecraft.getConnection().levels());
		}
		dimensions.addAll(config.dimensionRules().keySet());
		return dimensions;
	}

	private static List<AtlasBiome> biomes(Registry<Biome> registry) {
		List<AtlasBiome> biomes = new ArrayList<>();
		for (ResourceKey<Biome> key : registry.registryKeySet()) {
			registry.getHolder(key).ifPresent(holder -> biomes.add(new AtlasBiome(
					key.location(),
					holder,
					vanillaDimensions(holder)
			)));
		}
		return List.copyOf(biomes);
	}

	private static Set<ResourceKey<Level>> vanillaDimensions(Holder<Biome> biome) {
		Set<ResourceKey<Level>> dimensions = new LinkedHashSet<>();
		if (biome.is(BiomeTags.IS_OVERWORLD)) {
			dimensions.add(Level.OVERWORLD);
		}
		if (biome.is(BiomeTags.IS_NETHER)) {
			dimensions.add(Level.NETHER);
		}
		if (biome.is(BiomeTags.IS_END)) {
			dimensions.add(Level.END);
		}
		return Set.copyOf(dimensions);
	}

	static Set<ResourceLocation> crops(CropBiomeLimiterConfig config) {
		Set<ResourceLocation> crops = new LinkedHashSet<>();
		GrowableBlockClassifier classifier = new GrowableBlockClassifier();
		BuiltInRegistries.BLOCK.entrySet().stream()
				.filter(entry -> isTracked(classifier, entry.getValue()))
				.map(entry -> entry.getKey().location())
				.forEach(crops::add);
		addConfiguredCrops(crops, config.fallbackRules());
		config.dimensionRules().values().forEach(rules -> addConfiguredCrops(crops, rules));
		crops.removeIf(id -> !BuiltInRegistries.BLOCK.containsKey(id)
				|| NON_CROP_BONEMEAL_TARGETS.contains(BuiltInRegistries.BLOCK.get(id)));
		return Set.copyOf(crops);
	}

	private static boolean isTracked(GrowableBlockClassifier classifier, Block block) {
		if (NON_CROP_BONEMEAL_TARGETS.contains(block)) {
			return false;
		}
		try {
			return classifier.isTrackedGrowable(block.defaultBlockState());
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Skipping viewer crop candidate because block inspection failed: {}", block, exception);
			return false;
		}
	}

	private static void addConfiguredCrops(Set<ResourceLocation> crops, DimensionRules rules) {
		if (rules instanceof ExplicitModeRules explicitRules) {
			crops.addAll(explicitRules.cropBiomeRules().keySet());
		} else if (rules instanceof ThresholdModeRules thresholdRules) {
			crops.addAll(thresholdRules.cropRules().keySet());
		}
	}
}
