package rocks.theatomicoption.CropBiomeLimiter;

import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import rocks.theatomicoption.CropBiomeLimiter.CropBiomeLogic;

import static rocks.theatomicoption.CropBiomeLimiter.CropBiomeLimiter.config;

/**
 * TODO: extract some of this logic into a separate class.
 */
public class CropGrowthEventHandler {

    /**
     * Block Crop growth in Biomes disallowed by config.
     * */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCropGrowEvent(BlockEvent.CropGrowEvent.Pre event) {
        if(event.getWorld().isRemote){ return; } // run only on logical server

        if (config.isBlacklistDim) {
            if (config.isListedDimension(event.getWorld().provider.getDimension())) {
                return;
            }
        } else {
            if (!config.isListedDimension((event.getWorld().provider.getDimension()))) {
                return;
            }
        }


        float biomeTemp = event.getWorld().getBiome(event.getPos()).getTemperature();
        float biomeRain = event.getWorld().getBiome(event.getPos()).getRainfall();
        Biome biome = event.getWorld().getBiome(event.getPos());
        Block block = event.getState().getBlock();
        boolean resultFound = false;

        //FMLLog.info(event.getWorld().getBiome(blockPos).getBiomeName());


        if (config.isBlacklistBiome) {
            //FMLLog.info("Config set for Whitelist by biomeType, blacklist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType whitelist");
                event.setResult(Event.Result.DEFAULT);
                resultFound = true;
            }
            if (config.isListedCropBiome(block, biome)) {
                //FMLLog.info("crop %s is blacklisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                event.setResult(Event.Result.DENY);
                resultFound = true;
            }
            if (!resultFound) {
                event.setResult(Event.Result.DENY);
            }
        } else {
            //FMLLog.info("Config set for Blacklist by biomeType, Whitelist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType blacklist");
                event.setResult(Event.Result.DENY);
                resultFound = true;
            }
            if (config.isListedCropBiome(event.getState().getBlock(), biome)) {
                //FMLLog.info("Can grow crop %s because it's whitelisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                event.setResult(Event.Result.DEFAULT);
                resultFound = true;
            }
            if (!resultFound) {
                event.setResult(Event.Result.DEFAULT);
            }
        }


    }


    /**
     *
     * Determines what happens when player uses bonemeal on a plant.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBonemealEvent(BonemealEvent event)
    {
        if (event.getWorld().isRemote) { return; } // run only on logical server. something something do this with SideProxy instead because reasons?


        if (config.isBlacklistDim) {
            if (config.isListedDimension(event.getWorld().provider.getDimension())) {
                return;
            }
        } else {
            if (!config.isListedDimension((event.getWorld().provider.getDimension()))) {
                return;
            }
        }


        float biomeTemp = event.getWorld().getBiome(event.getPos()).getTemperature();
        float biomeRain = event.getWorld().getBiome(event.getPos()).getRainfall();
        Biome biome = event.getWorld().getBiome(event.getPos());
        Block block = event.getBlock().getBlock();
        boolean resultFound = false;

        //FMLLog.info(event.getWorld().getBiome(blockPos).getBiomeName());
/*        //Debug: spam Forge info window with list of biomes from the visit biomes achievement when bonemeal is used
        for(Biome b : Biome.EXPLORATION_BIOMES_LIST) {
            FMLLog.info("%s is of TempCategory %s, with temperature %f and rainfall %f",b.getBiomeName(),b.getTempCategory(),b.getTemperature(),b.getRainfall());
        }
*/



        if (config.isBlacklistBiome) {
            //FMLLog.info("Config set for Whitelist by biomeType, blacklist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType whitelist");
                event.setResult(Event.Result.DEFAULT);
                resultFound = true;
            }
            if (config.isListedCropBiome(block, biome)) {
                //FMLLog.info("crop %s is blacklisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                event.setResult(Event.Result.ALLOW); //"ALLOW" uses a bonemeal but doesn't grow crop.
                resultFound = true;
            }
            if (!resultFound) {
                event.setResult(Event.Result.ALLOW);
            }
        } else {
            //FMLLog.info("Config set for Blacklist by biomeType, Whitelist subtract by biome");
            if (config.isListedCropBiomeType(block, biome)) {
                //FMLLog.info("crop found in biomeType blacklist");
                event.setResult(Event.Result.ALLOW); //"ALLOW" uses a bonemeal but doesn't grow crop.
                resultFound = true;
            }
            if (config.isListedCropBiome(event.getBlock().getBlock(), biome)) {
                //FMLLog.info("Can grow crop %s because it's whitelisted in biome %s", block.getRegistryName().toString(), biome.getBiomeName());
                event.setResult(Event.Result.DEFAULT);
                resultFound = true;
            }
            if (!resultFound) {
                event.setResult(Event.Result.DEFAULT);
            }
        }
    }
}
