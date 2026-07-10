package rocks.theatomicoption.cropbiomelimiter.logic;

import java.util.Set;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class GrowableBlockClassifier {
	private static final Set<Block> DEFAULT_GROWABLE_BLOCKS = Set.of(
			Blocks.WHEAT,
			Blocks.CARROTS,
			Blocks.POTATOES,
			Blocks.BEETROOTS,
			Blocks.MELON_STEM,
			Blocks.PUMPKIN_STEM,
			Blocks.ATTACHED_MELON_STEM,
			Blocks.ATTACHED_PUMPKIN_STEM,
			Blocks.TORCHFLOWER_CROP,
			Blocks.PITCHER_CROP,
			Blocks.CACTUS,
			Blocks.SUGAR_CANE,
			Blocks.COCOA,
			Blocks.NETHER_WART,
			Blocks.BROWN_MUSHROOM,
			Blocks.RED_MUSHROOM,
			Blocks.OAK_SAPLING,
			Blocks.SPRUCE_SAPLING,
			Blocks.BIRCH_SAPLING,
			Blocks.JUNGLE_SAPLING,
			Blocks.ACACIA_SAPLING,
			Blocks.CHERRY_SAPLING,
			Blocks.DARK_OAK_SAPLING,
			Blocks.MANGROVE_PROPAGULE,
			Blocks.AZALEA,
			Blocks.FLOWERING_AZALEA,
			Blocks.BAMBOO_SAPLING,
			Blocks.BAMBOO,
			Blocks.SWEET_BERRY_BUSH,
			Blocks.CAVE_VINES,
			Blocks.CAVE_VINES_PLANT,
			Blocks.SEAGRASS,
			Blocks.TALL_SEAGRASS,
			Blocks.KELP,
			Blocks.KELP_PLANT,
			Blocks.VINE,
			Blocks.GLOW_LICHEN,
			Blocks.CHORUS_FLOWER,
			Blocks.CHORUS_PLANT,
			Blocks.CRIMSON_FUNGUS,
			Blocks.WARPED_FUNGUS,
			Blocks.CRIMSON_ROOTS,
			Blocks.WARPED_ROOTS,
			Blocks.WEEPING_VINES,
			Blocks.WEEPING_VINES_PLANT,
			Blocks.TWISTING_VINES,
			Blocks.TWISTING_VINES_PLANT,
			Blocks.BIG_DRIPLEAF,
			Blocks.BIG_DRIPLEAF_STEM,
			Blocks.SMALL_DRIPLEAF,
			Blocks.MOSS_BLOCK,
			Blocks.MOSS_CARPET,
			Blocks.HANGING_ROOTS,
			Blocks.GRASS,
			Blocks.TALL_GRASS,
			Blocks.FERN,
			Blocks.LARGE_FERN
	);

	public static Set<Block> defaultGrowableBlocks() {
		return DEFAULT_GROWABLE_BLOCKS;
	}

	public boolean isTrackedGrowable(BlockState state) {
		if (state == null) {
			return false;
		}

		try {
			Block block = state.getBlock();
			return DEFAULT_GROWABLE_BLOCKS.contains(block)
					|| block instanceof BonemealableBlock
					|| state.is(BlockTags.CROPS, ignored -> true);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Ignoring growable block candidate because block classification failed.", exception);
			return false;
		}
	}

	public boolean hasNaturalGrowthTick(BlockState state) {
		if (state == null) {
			return false;
		}

		try {
			return isTrackedGrowable(state) && state.isRandomlyTicking();
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Ignoring natural growth candidate because block classification failed.", exception);
			return false;
		}
	}
}
