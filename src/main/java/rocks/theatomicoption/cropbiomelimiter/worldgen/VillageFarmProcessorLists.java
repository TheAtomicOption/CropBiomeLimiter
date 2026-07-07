package rocks.theatomicoption.cropbiomelimiter.worldgen;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.config.ConfigLoadResult;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfigLoader;
import rocks.theatomicoption.cropbiomelimiter.config.GeneralOptions;

public final class VillageFarmProcessorLists {
	private static volatile Boolean cachedAffectsVillageFarmGeneration;

	private VillageFarmProcessorLists() {
	}

	public static void register(
			BootstrapContext<StructureProcessorList> context,
			ResourceKey<StructureProcessorList> key,
			List<StructureProcessor> vanillaProcessors
	) {
		boolean affectsVillageFarmGeneration = isVillageFarmKey(key) && shouldAffectVillageFarmGeneration();
		List<StructureProcessor> processors = customProcessorsFor(key, vanillaProcessors, affectsVillageFarmGeneration);
		context.register(key, new StructureProcessorList(processors));
	}

	public static List<StructureProcessor> customProcessorsFor(
			ResourceKey<StructureProcessorList> key,
			List<StructureProcessor> vanillaProcessors,
			boolean affectsVillageFarmGeneration
	) {
		if (!affectsVillageFarmGeneration) {
			return vanillaProcessors;
		}
		try {
			return customProcessorsFor(key).orElse(vanillaProcessors);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not create custom village farm processors; using vanilla farm processors.");
			CropBiomeLimiter.LOGGER.debug("Custom village farm processor failure details.", exception);
			return vanillaProcessors;
		}
	}

	public static Optional<List<Block>> customCropBlocksFor(ResourceKey<StructureProcessorList> key) {
		if (ProcessorLists.FARM_SNOWY.equals(key)) {
			return Optional.of(List.of(Blocks.BEETROOTS, Blocks.POTATOES));
		}
		if (ProcessorLists.FARM_TAIGA.equals(key)) {
			return Optional.of(List.of(Blocks.BEETROOTS, Blocks.POTATOES));
		}
		if (ProcessorLists.FARM_PLAINS.equals(key)) {
			return Optional.of(List.of(Blocks.POTATOES, Blocks.CARROTS));
		}
		if (ProcessorLists.FARM_SAVANNA.equals(key) || ProcessorLists.FARM_DESERT.equals(key)) {
			return Optional.of(List.of(Blocks.CARROTS));
		}
		return Optional.empty();
	}

	private static boolean isVillageFarmKey(ResourceKey<StructureProcessorList> key) {
		return ProcessorLists.FARM_SNOWY.equals(key)
				|| ProcessorLists.FARM_TAIGA.equals(key)
				|| ProcessorLists.FARM_PLAINS.equals(key)
				|| ProcessorLists.FARM_SAVANNA.equals(key)
				|| ProcessorLists.FARM_DESERT.equals(key);
	}

	private static Optional<List<StructureProcessor>> customProcessorsFor(ResourceKey<StructureProcessorList> key) {
		if (ProcessorLists.FARM_SNOWY.equals(key)) {
			return Optional.of(farmProcessors(
					replaceWheatWith(Blocks.BEETROOTS, 0.75F),
					replaceWheatWith(Blocks.POTATOES, 0.60F)
			));
		}
		if (ProcessorLists.FARM_TAIGA.equals(key)) {
			return Optional.of(farmProcessors(
					replaceWheatWith(Blocks.BEETROOTS, 0.25F),
					replaceWheatWith(Blocks.POTATOES, 0.70F)
			));
		}
		if (ProcessorLists.FARM_PLAINS.equals(key)) {
			return Optional.of(farmProcessors(
					replaceWheatWith(Blocks.POTATOES, 0.15F),
					replaceWheatWith(Blocks.CARROTS, 0.25F)
			));
		}
		if (ProcessorLists.FARM_SAVANNA.equals(key) || ProcessorLists.FARM_DESERT.equals(key)) {
			return Optional.of(farmProcessors(replaceWheatWith(Blocks.CARROTS, 0.80F)));
		}
		return Optional.empty();
	}

	private static List<StructureProcessor> farmProcessors(ProcessorRule... rules) {
		return List.<StructureProcessor>of(new RuleProcessor(List.of(rules)));
	}

	private static ProcessorRule replaceWheatWith(Block crop, float chance) {
		return new ProcessorRule(
				new RandomBlockMatchTest(Blocks.WHEAT, chance),
				AlwaysTrueTest.INSTANCE,
				crop.defaultBlockState()
		);
	}

	private static boolean shouldAffectVillageFarmGeneration() {
		Boolean cached = cachedAffectsVillageFarmGeneration;
		if (cached != null) {
			return cached;
		}
		boolean resolved = tryReadAffectsVillageFarmGeneration();
		cachedAffectsVillageFarmGeneration = resolved;
		return resolved;
	}

	private static boolean tryReadAffectsVillageFarmGeneration() {
		try {
			Path configDirectory = FabricLoader.getInstance().getConfigDir();
			ConfigLoadResult result = CropBiomeLimiterConfigLoader.tryLoadConfig(configDirectory);
			return result.config().generalOptions().affectsVillageFarmGeneration();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not read village farm generation config; using default value.");
			CropBiomeLimiter.LOGGER.debug("Village farm generation config read failure details.", exception);
			return GeneralOptions.defaults().affectsVillageFarmGeneration();
		}
	}
}
