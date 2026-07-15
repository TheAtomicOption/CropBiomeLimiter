package rocks.theatomicoption.cropbiomelimiter.logic;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfig;

public final class CropDecisionService {
	private final CropBiomeLimiterConfig config;
	private final GrowableBlockClassifier growableBlockClassifier;
	private final boolean allowAll;

	public CropDecisionService(CropBiomeLimiterConfig config, GrowableBlockClassifier growableBlockClassifier) {
		this(config, growableBlockClassifier, config == null || growableBlockClassifier == null);
	}

	private CropDecisionService(CropBiomeLimiterConfig config, GrowableBlockClassifier growableBlockClassifier, boolean allowAll) {
		this.config = config;
		this.growableBlockClassifier = growableBlockClassifier;
		this.allowAll = allowAll;
	}

	public static CropDecisionService allowAll() {
		return new CropDecisionService(null, null, true);
	}

	public Optional<CropBiomeLimiterConfig> config() {
		return Optional.ofNullable(config);
	}

	public boolean canGrowNaturally(ServerLevel level, BlockPos pos, BlockState state) {
		if (allowAll) {
			return true;
		}

		try {
			return tryCanGrowNaturally(level, pos, state);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Allowing natural crop growth because Crop Biome Limiter failed to evaluate a random tick.", exception);
			return true;
		}
	}

	private boolean tryCanGrowNaturally(ServerLevel level, BlockPos pos, BlockState state) {
		if (level == null || pos == null) {
			return true;
		}
		return canGrowNaturally(level.dimension(), level.getBiome(pos), state);
	}

	public boolean canGrowNaturally(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state) {
		if (allowAll) {
			return true;
		}

		try {
			if (!shouldEvaluate(state)) {
				return true;
			}
			return behaviorFor(dimension, biome, state.getBlock()).allowsNaturalGrowth();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Allowing natural crop growth because Crop Biome Limiter failed to evaluate a crop rule.", exception);
			return true;
		}
	}

	public boolean shouldWitherOnSuccessfulNaturalGrowth(ServerLevel level, BlockPos pos, BlockState state) {
		if (allowAll) {
			return false;
		}

		try {
			if (level == null || pos == null) {
				return false;
			}
			return shouldWitherOnSuccessfulNaturalGrowth(level.dimension(), level.getBiome(pos), state);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Leaving natural crop growth unchanged because Crop Biome Limiter failed to evaluate withering behavior.", exception);
			return false;
		}
	}

	public boolean shouldWitherOnSuccessfulNaturalGrowth(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state) {
		if (allowAll) {
			return false;
		}

		try {
			if (!shouldEvaluate(state) || !growableBlockClassifier.hasNaturalGrowthTick(state)) {
				return false;
			}
			CropBehavior behavior = behaviorFor(dimension, biome, state.getBlock());
			return behavior.allowsPlanting() && !behavior.allowsNaturalGrowth();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Leaving natural crop growth unchanged because Crop Biome Limiter failed to evaluate withering behavior.", exception);
			return false;
		}
	}

	public boolean shouldWarnOnAllowedPlacement(Level level, BlockPos pos, BlockState state, Player player) {
		if (allowAll) {
			return false;
		}

		try {
			if (level == null || pos == null || isCreative(player)) {
				return false;
			}
			return shouldWarnOnAllowedPlacement(level.dimension(), level.getBiome(pos), state);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Suppressing placement warning because Crop Biome Limiter failed to evaluate placement warning behavior.", exception);
			return false;
		}
	}

	public boolean shouldWarnOnAllowedPlacement(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state) {
		if (allowAll) {
			return false;
		}

		try {
			if (!config.generalOptions().affectsBlockPlacement()
					|| !shouldEvaluate(state)
					|| !growableBlockClassifier.hasNaturalGrowthTick(state)) {
				return false;
			}
			CropBehavior behavior = behaviorFor(dimension, biome, state.getBlock());
			return behavior.allowsPlanting() && !behavior.allowsNaturalGrowth();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Suppressing placement warning because Crop Biome Limiter failed to evaluate placement warning behavior.", exception);
			return false;
		}
	}

	public boolean canUseBonemeal(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		if (allowAll) {
			return true;
		}

		try {
			return tryCanUseBonemeal(level, pos, state, player);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing bone meal use because Crop Biome Limiter failed to evaluate the crop rule.", exception);
			return true;
		}
	}

	private boolean tryCanUseBonemeal(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		if (level == null || pos == null) {
			return true;
		}
		return canUseBonemeal(level.dimension(), level.getBiome(pos), state, isCreative(player));
	}

	public boolean canUseBonemeal(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state) {
		return canUseBonemeal(dimension, biome, state, false);
	}

	public boolean canUseBonemeal(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state, boolean creativeMode) {
		if (allowAll) {
			return true;
		}

		try {
			if (creativeMode) {
				return true;
			}
			if (!config.generalOptions().affectsBonemeal() || !shouldEvaluate(state)) {
				return true;
			}
			return behaviorFor(dimension, biome, state.getBlock()).allowsBonemeal();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing bone meal use because Crop Biome Limiter failed to evaluate the crop rule.", exception);
			return true;
		}
	}

	public boolean shouldSendChatInfo() {
		if (allowAll) {
			return false;
		}

		try {
			return config.generalOptions().chatInfo();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Suppressing Crop Biome Limiter chat feedback because config evaluation failed.", exception);
			return false;
		}
	}

	public boolean canPlace(Level level, BlockPos pos, Block block, Player player) {
		if (allowAll) {
			return true;
		}

		try {
			return tryCanPlace(level, pos, block, player);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing crop placement because Crop Biome Limiter failed to evaluate the crop rule.", exception);
			return true;
		}
	}

	public boolean canPlace(Level level, BlockPos pos, BlockState state, Player player) {
		if (allowAll) {
			return true;
		}

		try {
			if (level == null || pos == null) {
				return true;
			}
			return canPlace(level.dimension(), level.getBiome(pos), state, isCreative(player));
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing crop placement because Crop Biome Limiter failed to evaluate the crop rule.", exception);
			return true;
		}
	}

	private boolean tryCanPlace(Level level, BlockPos pos, Block block, Player player) {
		if (level == null || pos == null || block == null) {
			return true;
		}
		return canPlace(level.dimension(), level.getBiome(pos), block.defaultBlockState(), isCreative(player));
	}

	public boolean canPlace(ResourceKey<Level> dimension, Holder<Biome> biome, Block block) {
		return block == null ? true : canPlace(dimension, biome, block.defaultBlockState());
	}

	public boolean canPlace(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state) {
		return canPlace(dimension, biome, state, false);
	}

	public boolean canPlace(ResourceKey<Level> dimension, Holder<Biome> biome, BlockState state, boolean creativeMode) {
		if (allowAll) {
			return true;
		}

		try {
			if (creativeMode) {
				return true;
			}
			if (!config.generalOptions().affectsBlockPlacement()) {
				return true;
			}

			if (!shouldEvaluate(state)) {
				return true;
			}
			return behaviorFor(dimension, biome, state.getBlock()).allowsPlanting();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing crop placement because Crop Biome Limiter failed to evaluate the crop rule.", exception);
			return true;
		}
	}

	private boolean shouldEvaluate(BlockState state) {
		if (state == null) {
			return false;
		}

		ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		if (blockId == null) {
			return false;
		}

		return !config.generalOptions().isExcluded(blockId) && growableBlockClassifier.isTrackedGrowable(state);
	}

	private CropBehavior behaviorFor(ResourceKey<Level> dimension, Holder<Biome> biome, Block crop) {
		if (dimension == null || biome == null) {
			return CropBehavior.GROWABLE;
		}

		ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop);
		if (cropId == null) {
			return CropBehavior.GROWABLE;
		}

		return config.resolve(dimension, cropId, biome);
	}

	private static boolean isCreative(Player player) {
		return player != null && player.isCreative();
	}
}
