package rocks.theatomicoption.CropBiomeLimiter;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLLog;


import static rocks.theatomicoption.CropBiomeLimiter.CropBiomeLimiter.config;

/**
 * This class was created under the impression that the logic for whether crops
 * should be allowed to grow should be abstracted out of the event handler class.
 *
 * Additionally since vanilla biome categories seem to be quite limited, and mods like
 * Biomes'o'plenty often just use their own, this class may evolve into providing a configurable
 * heuristic based on Temperature and Rainfall instead of a strict biome to crop connections.
 *
 */
public class CropBiomeLogic {


    /**
     * Returns true if the crop at blockPos is approved to grow by the mod's configuration files.
     *
     * @param world Can
     * @param block You
     * @param blockPos See this
     * @return
     */
    public static boolean canGrowExplicit(World world, BlockPos blockPos) {

        Biome biome = world.getBiome(blockPos);
        Block block = world.getBlockState(blockPos).getBlock();

        return canGrowExplicit(biome, block, world.provider.getDimension());

/*        if (config.isExcludedBlock(block)) {return true;}

        if (config.isBlacklistBiome) {
            //FMLLog.info("Config set for Whitelist by biomeType, blacklist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType whitelist.");
                canGrow = true;
                resultFound = true;
            }
            if (config.isListedCropBiome(block, biome)) {
                //FMLLog.info("Biometype is white listed, but crop %s is blacklisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                canGrow = false; //"ALLOW" uses a bonemeal but doesn't grow crop.
                resultFound = true;
            }
            if (!resultFound) { canGrow = false; } //crops not listed in this biomeType can't grow here.
        } else {
            //FMLLog.info("Config set for Blacklist by biomeType, Whitelist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType blacklist.");
                canGrow = false; //"ALLOW" uses a bonemeal but doesn't grow crop.
                resultFound = true;
            }
            if (config.isListedCropBiome(block, biome)) {
                //FMLLog.info("Biome type is black listed, but can grow crop %s because it's whitelisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
               canGrow = true;
                resultFound = true;
            }
            if (!resultFound) { canGrow = true; } //crops not listed in the black listed biomeType or whitelisted by biome can grow.
        }


        //if listed in blacklist or not listed in whitelist, this mod always returns canGrow = true.
        if (config.isBlacklistDim) {
            if (config.isListedDimension(world.provider.getDimension())) {
                canGrow = true;
            }
        } else {
            if (!config.isListedDimension((world.provider.getDimension()))) {
                canGrow = true;
            }
        }

        return canGrow;*/
    }

    public static boolean canGrowExplicit(Biome biome, Block block, int dimension) {
        boolean canGrow = false;
        boolean resultFound = false;

        if (config.isExcludedBlock(block)) {return true;}

        if (config.isBlacklistBiome) {
            //FMLLog.info("Config set for Whitelist by biomeType, blacklist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType whitelist.");
                canGrow = true;
                resultFound = true;
            }
            if (config.isListedCropBiome(block, biome)) {
                //FMLLog.info("Biometype is white listed, but crop %s is blacklisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                canGrow = false; //"ALLOW" uses a bonemeal but doesn't grow crop.
                resultFound = true;
            }
            if (!resultFound) { canGrow = false; } //crops not listed in this biomeType can't grow here.
        } else {
            //FMLLog.info("Config set for Blacklist by biomeType, Whitelist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType blacklist.");
                canGrow = false; //"ALLOW" uses a bonemeal but doesn't grow crop.
                resultFound = true;
            }
            if (config.isListedCropBiome(block, biome)) {
                //FMLLog.info("Biome type is black listed, but can grow crop %s because it's whitelisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                canGrow = true;
                resultFound = true;
            }
            if (!resultFound) { canGrow = true; } //crops not listed in the black listed biomeType or whitelisted by biome can grow.
        }


        //if listed in blacklist or not listed in whitelist, this mod always returns canGrow = true.
        if (config.isBlacklistDim) {
            if (config.isListedDimension(dimension)) {
                canGrow = true;
            }
        } else {
            if (!config.isListedDimension(dimension)) {
                canGrow = true;
            }
        }

        return canGrow;

    }

    public boolean canGrowHeuristic(World world, Block block, BlockPos blockPos) {
        if (config.isExcludedBlock(block)) {return true;}

        /**
         * Temperature decreases by 0.00166667 per block above 64. At y=128 this is a -0.10666688 difference
         * Rainfall does not vary with height.
         */

        float biomeTemp = world.getBiome(blockPos).getTemperature();
        float biomeRain = world.getBiome(blockPos).getRainfall();

        //FMLLog.info("Temperature is:" + world.getBiome(blockPos).getTemperature());
        //FMLLog.info("Rainfall is:" + world.getBiome(blockPos).getRainfall());
        //FMLLog.info("Explicit biome not found. Defaulting to temp/rain");

        if(biomeTemp > 0.15 && biomeTemp <= .9 && biomeRain > 0 && biomeRain < .9){

            return true;
        }
        else {
            //FMLLog.info("This plant can't grow in this climate!");
            return false;

        }
    }
}