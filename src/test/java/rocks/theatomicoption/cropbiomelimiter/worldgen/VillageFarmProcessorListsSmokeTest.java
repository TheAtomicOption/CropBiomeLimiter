package rocks.theatomicoption.cropbiomelimiter.worldgen;

import java.util.List;

import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class VillageFarmProcessorListsSmokeTest {
	private VillageFarmProcessorListsSmokeTest() {
	}

	public static void run() {
		keepsVanillaProcessorsWhenDisabledOrUnrelated();
		usesColdToWarmVillageCropBuckets();
		replacesFarmProcessorsWhenEnabled();
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
}
