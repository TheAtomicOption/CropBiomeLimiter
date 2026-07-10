package rocks.theatomicoption.cropbiomelimiter.logic;

import java.util.Comparator;
import java.util.Set;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class NaturalGrowthResultDetector {
	private static final Set<String> GROWTH_PROPERTY_NAMES = Set.of("age", "stage");

	private NaturalGrowthResultDetector() {
	}

	public static GrowthSnapshot snapshot(BlockState originalState, BlockState aboveState, BlockState belowState) {
		return new GrowthSnapshot(originalState, aboveState, belowState);
	}

	public static GrowthSnapshot snapshotWithoutNeighbors(BlockState originalState) {
		return snapshot(originalState, null, null);
	}

	public static boolean didGrow(GrowthSnapshot snapshot, BlockState currentState, BlockState currentAboveState, BlockState currentBelowState) {
		if (snapshot == null || snapshot.originalState() == null || currentState == null || currentState.isAir()) {
			return false;
		}

		BlockState originalState = snapshot.originalState();
		if (!currentState.is(originalState.getBlock())) {
			return true;
		}

		return increasedGrowthProperty(originalState, currentState)
				|| isNewVerticalGrowthAbove(snapshot, currentAboveState)
				|| isNewVerticalGrowthBelow(snapshot, currentBelowState);
	}

	public static boolean isNewVerticalGrowthAbove(GrowthSnapshot snapshot, BlockState currentAboveState) {
		return isNewVerticalGrowth(snapshot, snapshot == null ? null : snapshot.aboveState(), currentAboveState);
	}

	public static boolean isNewVerticalGrowthBelow(GrowthSnapshot snapshot, BlockState currentBelowState) {
		return isNewVerticalGrowth(snapshot, snapshot == null ? null : snapshot.belowState(), currentBelowState);
	}

	private static boolean isNewVerticalGrowth(GrowthSnapshot snapshot, BlockState previousNeighborState, BlockState currentNeighborState) {
		return snapshot != null
				&& snapshot.originalState() != null
				&& isAir(previousNeighborState)
				&& isMatchingVerticalGrowth(snapshot.originalState(), currentNeighborState);
	}

	private static boolean isMatchingVerticalGrowth(BlockState originalState, BlockState neighborState) {
		return neighborState != null
				&& !neighborState.isAir()
				&& neighborState.is(originalState.getBlock());
	}

	private static boolean isAir(BlockState state) {
		return state != null && state.isAir();
	}

	private static boolean increasedGrowthProperty(BlockState originalState, BlockState currentState) {
		for (Property<?> property : originalState.getProperties()) {
			if (isGrowthProperty(property) && currentState.hasProperty(property)) {
				IntegerProperty integerProperty = (IntegerProperty) property;
				if (originalState.getValue(integerProperty) < maxValue(originalState, integerProperty)
						&& currentState.getValue(integerProperty) > originalState.getValue(integerProperty)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isGrowthProperty(Property<?> property) {
		return property instanceof IntegerProperty && GROWTH_PROPERTY_NAMES.contains(property.getName());
	}

	private static int maxValue(BlockState state, IntegerProperty property) {
		if ("age".equals(property.getName()) && state.getBlock() instanceof CropBlock cropBlock) {
			return cropBlock.getMaxAge();
		}
		return property.getPossibleValues().stream()
				.max(Comparator.naturalOrder())
				.orElse(state.getValue(property));
	}

	public record GrowthSnapshot(BlockState originalState, BlockState aboveState, BlockState belowState) {
	}
}
