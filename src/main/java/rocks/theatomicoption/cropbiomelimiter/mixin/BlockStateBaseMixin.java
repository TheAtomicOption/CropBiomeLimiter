package rocks.theatomicoption.cropbiomelimiter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.logic.NaturalGrowthResultDetector;
import rocks.theatomicoption.cropbiomelimiter.logic.NaturalGrowthResultDetector.GrowthSnapshot;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	private static final ThreadLocal<GrowthSnapshot> cropbiomelimiter$growthSnapshot = new ThreadLocal<>();

	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void cropbiomelimiter$handleDeniedNaturalGrowth(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		cropbiomelimiter$growthSnapshot.remove();
		try {
			BlockState state = asState();
			boolean canGrowNaturally = CropBiomeLimiter.cropDecisionService().canGrowNaturally(level, pos, state);
			boolean shouldWither = CropBiomeLimiter.cropDecisionService().shouldWitherOnSuccessfulNaturalGrowth(level, pos, state);
			if (shouldWither) {
				cropbiomelimiter$growthSnapshot.set(NaturalGrowthResultDetector.snapshot(
						state,
						level.getBlockState(pos.above()),
						level.getBlockState(pos.below())
				));
			}
			if (!canGrowNaturally && !shouldWither) {
				ci.cancel();
			}
		} catch (RuntimeException exception) {
			cropbiomelimiter$growthSnapshot.remove();
			CropBiomeLimiter.LOGGER.debug("Allowing random tick because Crop Biome Limiter failed inside the natural growth mixin.", exception);
		}
	}

	@Inject(method = "randomTick", at = @At("TAIL"))
	private void cropbiomelimiter$replaceDeniedGrowthWithDeadBush(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		try {
			BlockState originalState = asState();
			if (!CropBiomeLimiter.cropDecisionService().shouldWitherOnSuccessfulNaturalGrowth(level, pos, originalState)) {
				return;
			}

			GrowthSnapshot snapshot = cropbiomelimiter$growthSnapshot.get();
			if (snapshot == null) {
				snapshot = NaturalGrowthResultDetector.snapshotWithoutNeighbors(originalState);
			}
			BlockState currentState = level.getBlockState(pos);
			BlockState currentAboveState = level.getBlockState(pos.above());
			BlockState currentBelowState = level.getBlockState(pos.below());
			if (NaturalGrowthResultDetector.didGrow(snapshot, currentState, currentAboveState, currentBelowState)) {
				cropbiomelimiter$removeNewVerticalGrowth(level, pos, snapshot, currentAboveState, currentBelowState);
				level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
			}
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Leaving random tick result unchanged because Crop Biome Limiter failed inside the withering mixin.", exception);
		} finally {
			cropbiomelimiter$growthSnapshot.remove();
		}
	}

	private static void cropbiomelimiter$removeNewVerticalGrowth(ServerLevel level, BlockPos pos, GrowthSnapshot snapshot, BlockState currentAboveState, BlockState currentBelowState) {
		if (NaturalGrowthResultDetector.isNewVerticalGrowthAbove(snapshot, currentAboveState)) {
			level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
		}
		if (NaturalGrowthResultDetector.isNewVerticalGrowthBelow(snapshot, currentBelowState)) {
			level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);
		}
	}
}
