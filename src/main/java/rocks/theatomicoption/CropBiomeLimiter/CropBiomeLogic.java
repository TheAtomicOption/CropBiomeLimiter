package rocks.theatomicoption.CropBiomeLimiter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;

import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;

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



    public boolean canGrowHeuristic(World world, Block block, BlockPos blockPos) {
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