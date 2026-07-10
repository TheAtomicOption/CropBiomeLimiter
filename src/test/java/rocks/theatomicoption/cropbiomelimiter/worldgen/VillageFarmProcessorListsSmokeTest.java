package rocks.theatomicoption.cropbiomelimiter.worldgen;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class VillageFarmProcessorListsSmokeTest {
	private static final RandomSource WORST_CASE_RANDOM = new HighFloatRandomSource();

	private VillageFarmProcessorListsSmokeTest() {
	}

	public static void run() {
		keepsVanillaProcessorsWhenDisabledOrUnrelated();
		usesColdToWarmVillageCropBuckets();
		replacesFarmProcessorsWhenEnabled();
		alwaysReplacesWheatWhenBucketExcludesWheat();
	}

	private static void keepsVanillaProcessorsWhenDisabledOrUnrelated() {
		List<StructureProcessor> vanillaProcessors = List.of();
		assertSame(vanillaProcessors, VillageFarmProcessorLists.customProcessorsFor(ProcessorLists.FARM_DESERT, vanillaProcessors, false),
				"disabled village farm generation should leave vanilla processors unchanged");
		assertSame(vanillaProcessors, VillageFarmProcessorLists.customProcessorsFor(ProcessorLists.EMPTY, vanillaProcessors, true),
				"unrelated processor lists should leave vanilla processors unchanged");
	}

	private static void usesColdToWarmVillageCropBuckets() {
		assertCropBlocks(List.of(Blocks.BEETROOTS, Blocks.POTATOES), ProcessorLists.FARM_SNOWY, "snowy farms should use cold crops");
		assertCropBlocks(List.of(Blocks.BEETROOTS, Blocks.POTATOES), ProcessorLists.FARM_TAIGA, "taiga farms should use cold and cool crops");
		assertCropBlocks(List.of(Blocks.POTATOES, Blocks.CARROTS), ProcessorLists.FARM_PLAINS, "plains farms should bridge cool and warm crops");
		assertCropBlocks(List.of(Blocks.CARROTS), ProcessorLists.FARM_SAVANNA, "savanna farms should use the warmest food crop");
		assertCropBlocks(List.of(Blocks.CARROTS), ProcessorLists.FARM_DESERT, "desert farms should use the warmest food crop");
	}

	private static void replacesFarmProcessorsWhenEnabled() {
		List<StructureProcessor> vanillaProcessors = List.of();
		List<StructureProcessor> customProcessors = VillageFarmProcessorLists.customProcessorsFor(ProcessorLists.FARM_DESERT, vanillaProcessors, true);
		assertTrue(customProcessors != vanillaProcessors, "enabled village farm generation should replace farm processors");
		assertEquals(1, customProcessors.size(), "custom village farm lists should use one rule processor");
	}

	private static void alwaysReplacesWheatWhenBucketExcludesWheat() {
		assertWheatReplacementInvariant(ProcessorLists.FARM_SNOWY);
		assertWheatReplacementInvariant(ProcessorLists.FARM_TAIGA);
		assertWheatReplacementInvariant(ProcessorLists.FARM_PLAINS);
		assertWheatReplacementInvariant(ProcessorLists.FARM_SAVANNA);
		assertWheatReplacementInvariant(ProcessorLists.FARM_DESERT);
	}

	private static void assertWheatReplacementInvariant(ResourceKey<StructureProcessorList> key) {
		List<Block> allowedCrops = VillageFarmProcessorLists.customCropBlocksFor(key).orElseThrow();
		if (allowedCrops.contains(Blocks.WHEAT)) {
			return;
		}

		BlockState outputState = worstCaseWheatOutput(key);
		assertTrue(!outputState.is(Blocks.WHEAT), key + " should not leave wheat behind when wheat is outside the crop bucket");
		assertTrue(allowedCrops.contains(outputState.getBlock()), key + " should replace wheat with a bucket crop");
	}

	private static BlockState worstCaseWheatOutput(ResourceKey<StructureProcessorList> key) {
		List<StructureProcessor> processors = VillageFarmProcessorLists.customProcessorsFor(key, List.of(), true);
		assertEquals(1, processors.size(), key + " should use one rule processor");
		assertTrue(processors.get(0) instanceof RuleProcessor, key + " should use a rule processor");

		BlockState wheatState = Blocks.WHEAT.defaultBlockState();
		for (ProcessorRule rule : processorRules((RuleProcessor) processors.get(0))) {
			if (rule.test(wheatState, Blocks.AIR.defaultBlockState(), BlockPos.ZERO, BlockPos.ZERO, BlockPos.ZERO, WORST_CASE_RANDOM)) {
				return rule.getOutputState();
			}
		}
		return wheatState;
	}

	@SuppressWarnings("unchecked")
	private static List<ProcessorRule> processorRules(RuleProcessor processor) {
		try {
			Field rulesField = RuleProcessor.class.getDeclaredField("rules");
			rulesField.setAccessible(true);
			return (List<ProcessorRule>) rulesField.get(processor);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("could not inspect rule processor", exception);
		}
	}

	private static void assertCropBlocks(List<Block> expected, ResourceKey<StructureProcessorList> key, String message) {
		List<Block> actual = VillageFarmProcessorLists.customCropBlocksFor(key).orElseThrow();
		assertEquals(expected, actual, message);
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertSame(Object expected, Object actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + " Expected the same instance.");
		}
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " Expected: " + expected + ", actual: " + actual);
		}
	}

	private static final class HighFloatRandomSource implements RandomSource {
		@Override
		public RandomSource fork() {
			return this;
		}

		@Override
		public PositionalRandomFactory forkPositional() {
			throw new AssertionError("positional random should not be used by village farm crop rules");
		}

		@Override
		public void setSeed(long seed) {
		}

		@Override
		public int nextInt() {
			throw new AssertionError("integer random should not be used by village farm crop rules");
		}

		@Override
		public int nextInt(int bound) {
			throw new AssertionError("integer random should not be used by village farm crop rules");
		}

		@Override
		public long nextLong() {
			throw new AssertionError("long random should not be used by village farm crop rules");
		}

		@Override
		public boolean nextBoolean() {
			throw new AssertionError("boolean random should not be used by village farm crop rules");
		}

		@Override
		public float nextFloat() {
			return 0.99999994F;
		}

		@Override
		public double nextDouble() {
			throw new AssertionError("double random should not be used by village farm crop rules");
		}

		@Override
		public double nextGaussian() {
			throw new AssertionError("gaussian random should not be used by village farm crop rules");
		}
	}
}
