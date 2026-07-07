package rocks.theatomicoption.cropbiomelimiter.mixin;

import java.util.Comparator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void cropbiomelimiter$handleDeniedNaturalGrowth(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		try {
			BlockState state = asState();
			if (!CropBiomeLimiter.cropDecisionService().canGrowNaturally(level, pos, state)
					&& !CropBiomeLimiter.cropDecisionService().shouldWitherOnSuccessfulNaturalGrowth(level, pos, state)) {
				ci.cancel();
			}
		} catch (RuntimeException exception) {
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

			BlockState currentState = level.getBlockState(pos);
			if (cropbiomelimiter$didImmaturePlantGrow(originalState, currentState)) {
				cropbiomelimiter$removeMatchingUpperHalf(level, pos, originalState);
				level.setBlock(pos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
			}
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Leaving random tick result unchanged because Crop Biome Limiter failed inside the withering mixin.", exception);
		}
	}

	private static boolean cropbiomelimiter$didImmaturePlantGrow(BlockState originalState, BlockState currentState) {
		IntegerProperty ageProperty = cropbiomelimiter$ageProperty(originalState);
		if (ageProperty == null || !cropbiomelimiter$isImmature(originalState, ageProperty)) {
			return false;
		}

		if (currentState.isAir()) {
			return false;
		}
		if (!currentState.is(originalState.getBlock())) {
			return true;
		}
		return currentState.hasProperty(ageProperty) && currentState.getValue(ageProperty) > originalState.getValue(ageProperty);
	}

	private static boolean cropbiomelimiter$isImmature(BlockState state, IntegerProperty ageProperty) {
		return state.getValue(ageProperty) < cropbiomelimiter$maxAge(state, ageProperty);
	}

	private static int cropbiomelimiter$maxAge(BlockState state, IntegerProperty ageProperty) {
		if (state.getBlock() instanceof CropBlock cropBlock) {
			return cropBlock.getMaxAge();
		}
		return ageProperty.getPossibleValues().stream()
				.max(Comparator.naturalOrder())
				.orElse(state.getValue(ageProperty));
	}

	private static IntegerProperty cropbiomelimiter$ageProperty(BlockState state) {
		for (Property<?> property : state.getProperties()) {
			if (property instanceof IntegerProperty integerProperty && "age".equals(property.getName())) {
				return integerProperty;
			}
		}
		return null;
	}

	private static void cropbiomelimiter$removeMatchingUpperHalf(ServerLevel level, BlockPos pos, BlockState originalState) {
		BlockState aboveState = level.getBlockState(pos.above());
		if (aboveState.is(originalState.getBlock())) {
			level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
		}
	}
}
