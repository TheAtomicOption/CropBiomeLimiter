package rocks.theatomicoption.cropbiomelimiter.viewer.client;

import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rocks.theatomicoption.cropbiomelimiter.config.DefaultCropBiomeConfig;

public final class ClientAtlasDataSmokeTest {
	private ClientAtlasDataSmokeTest() {
	}

	public static void run() {
		Set<Identifier> crops = ClientAtlasData.crops(DefaultCropBiomeConfig.create());

		assertExcluded(crops, Blocks.GRASS_BLOCK);
		assertExcluded(crops, Blocks.ROOTED_DIRT);
		assertExcluded(crops, Blocks.NETHERRACK);
		assertExcluded(crops, Blocks.CRIMSON_NYLIUM);
		assertExcluded(crops, Blocks.WARPED_NYLIUM);

		assertIncluded(crops, Blocks.NETHER_WART);
		assertIncluded(crops, Blocks.CRIMSON_FUNGUS);
		assertIncluded(crops, Blocks.MOSS_BLOCK);
	}

	private static void assertExcluded(Set<Identifier> crops, Block block) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		if (crops.contains(id)) {
			throw new AssertionError(id + " is a terrain bonemeal target, not an atlas crop");
		}
	}

	private static void assertIncluded(Set<Identifier> crops, Block block) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		if (!crops.contains(id)) {
			throw new AssertionError(id + " should remain an atlas crop candidate");
		}
	}
}
