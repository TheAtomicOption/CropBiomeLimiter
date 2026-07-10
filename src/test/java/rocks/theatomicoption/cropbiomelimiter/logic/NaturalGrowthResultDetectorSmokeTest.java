package rocks.theatomicoption.cropbiomelimiter.logic;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import rocks.theatomicoption.cropbiomelimiter.logic.NaturalGrowthResultDetector.GrowthSnapshot;

public final class NaturalGrowthResultDetectorSmokeTest {
	private NaturalGrowthResultDetectorSmokeTest() {
	}

	public static void main(String[] args) {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		run();
	}

	public static void run() {
		detectsAgeGrowth();
		detectsSaplingStageGrowth();
		detectsNonAirReplacementWithoutAge();
		detectsNewVerticalSameBlockGrowth();
		ignoresNoGrowthResults();
	}

	private static void detectsAgeGrowth() {
		BlockState wheat0 = Blocks.WHEAT.defaultBlockState();
		BlockState wheat1 = wheat0.setValue(CropBlock.AGE, 1);

		assertTrue(didGrow(wheat0, wheat1), "age increase should count as successful natural growth");
	}

	private static void detectsSaplingStageGrowth() {
		BlockState sapling0 = Blocks.OAK_SAPLING.defaultBlockState();
		BlockState sapling1 = sapling0.setValue(SaplingBlock.STAGE, 1);

		assertTrue(didGrow(sapling0, sapling1), "sapling stage increase should count as successful natural growth");
	}

	private static void detectsNonAirReplacementWithoutAge() {
		assertTrue(didGrow(Blocks.BROWN_MUSHROOM.defaultBlockState(), Blocks.MUSHROOM_STEM.defaultBlockState()),
				"non-air replacement should count as successful natural growth even without age");
	}

	private static void detectsNewVerticalSameBlockGrowth() {
		BlockState cactus = Blocks.CACTUS.defaultBlockState();
		GrowthSnapshot before = NaturalGrowthResultDetector.snapshot(cactus, Blocks.AIR.defaultBlockState(), Blocks.SAND.defaultBlockState());

		assertTrue(NaturalGrowthResultDetector.didGrow(before, cactus, cactus, Blocks.SAND.defaultBlockState()),
				"new same-block growth above a column plant should count as successful natural growth");
		assertTrue(NaturalGrowthResultDetector.isNewVerticalGrowthAbove(before, cactus), "new vertical growth above should be removable");

		BlockState vine = Blocks.VINE.defaultBlockState();
		GrowthSnapshot vineBefore = NaturalGrowthResultDetector.snapshot(vine, Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState());

		assertTrue(NaturalGrowthResultDetector.didGrow(vineBefore, vine, Blocks.STONE.defaultBlockState(), vine),
				"new same-block growth below a vine should count as successful natural growth");
		assertTrue(NaturalGrowthResultDetector.isNewVerticalGrowthBelow(vineBefore, vine), "new vertical growth below should be removable");
	}

	private static void ignoresNoGrowthResults() {
		BlockState cactus = Blocks.CACTUS.defaultBlockState();
		GrowthSnapshot existingColumn = NaturalGrowthResultDetector.snapshot(cactus, cactus, Blocks.SAND.defaultBlockState());
		GrowthSnapshot unknownNeighbors = NaturalGrowthResultDetector.snapshotWithoutNeighbors(cactus);

		assertTrue(!didGrow(Blocks.WHEAT.defaultBlockState(), Blocks.WHEAT.defaultBlockState()), "unchanged crop should not count as growth");
		assertTrue(!didGrow(Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7), Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7)),
				"unchanged mature crop should not count as growth");
		assertTrue(!didGrow(Blocks.BROWN_MUSHROOM.defaultBlockState(), Blocks.AIR.defaultBlockState()), "air replacement should not count as successful growth");
		assertTrue(!NaturalGrowthResultDetector.didGrow(existingColumn, cactus, cactus, Blocks.SAND.defaultBlockState()),
				"preexisting vertical column should not count as new growth");
		assertTrue(!NaturalGrowthResultDetector.didGrow(unknownNeighbors, cactus, cactus, Blocks.SAND.defaultBlockState()),
				"missing neighbor snapshot should not enable vertical growth detection");
	}

	private static boolean didGrow(BlockState originalState, BlockState currentState) {
		GrowthSnapshot snapshot = NaturalGrowthResultDetector.snapshot(
				originalState,
				Blocks.STONE.defaultBlockState(),
				Blocks.STONE.defaultBlockState()
		);
		return NaturalGrowthResultDetector.didGrow(snapshot, currentState, Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState());
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
