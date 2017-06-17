package rocks.theatomicoption.CropBiomeLimiter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static rocks.theatomicoption.CropBiomeLimiter.CropBiomeLimiter.config;

/**
 * TO do: extract some of this logic into a separate class.
 *
 */
public class CropGrowthEventHandler {

    /**
     * Block Crop growth in Biomes disallowed by config.
     * */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCropGrowEvent(BlockEvent.CropGrowEvent.Pre event) {
        if(event.getWorld().isRemote){ return; } // run only on logical server


        Block block = event.getState().getBlock();
        boolean canGrow = false;

        canGrow = CropBiomeLogic.canGrowExplicit(event.getWorld(),event.getPos());

        if(canGrow){
            event.setResult(Event.Result.DEFAULT);
            FMLLog.info("canGrow true");
        }
        else {
            event.setResult(Event.Result.DENY);
            FMLLog.info("canGrow false");
        }

    }

     /**
     *
     * Determines what happens when player uses bonemeal on a plant.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBonemealEvent(BonemealEvent event) {
        if (event.getWorld().isRemote) { return; } // run only on logical server. something something do this with SideProxy instead because reasons?
        if (!config.affectsBonemeal) {return;}

        boolean canGrow = CropBiomeLogic.canGrowExplicit(event.getWorld(),event.getPos());

        if(canGrow){
            event.setResult(Event.Result.DEFAULT);
            //FMLLog.info("canGrow bonemeal true");
        }
        else {
            event.setResult(Event.Result.ALLOW);
            if(config.chatInfo) {
                event.getEntityPlayer().addChatComponentMessage(new TextComponentString("This plant can't grow in this climate."));
            }
            //FMLLog.info("canGrow bonemeal false");
        }

    }

    /**
     *
     * Activates when Crop block is planted.
     */
    @SubscribeEvent
    public void onPlantCrop(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) { return; } // run only on logical server. something something do this with SideProxy instead because reasons?
        if(!config.affectsBlockPlacement) { return; }


        //Doesn't appear to exactly be a flag for whether something is a crop...
        //if the block is part of or extends the BlockCrops class, assume it's a crop.
        // not sure how reliable this actually is, but it seems to work in limited testing.
        // FMLLog.info("testing if block placed extends class blockcrops:");
        if(!BlockCrops.class.isAssignableFrom(event.getPlacedBlock().getBlock().getClass())) {
            //FMLLog.info("class was " + event.getPlacedBlock().getBlock().getClass().getName());
            //FMLLog.info("superclass was " + event.getPlacedBlock().getBlock().getClass().getSuperclass().getName());
            return; }
        else {

            boolean canGrow = CropBiomeLogic.canGrowExplicit(event.getWorld(),event.getPos());
            if(!canGrow){
                //If the BlockCrop can't grow here, prevent placing it.
                FMLLog.info("canGrow false. Cancelling block placement.");
                if(config.chatInfo) {
                    event.getPlayer().addChatComponentMessage(new TextComponentString("This plant can't grow in this climate."));
                }
                event.setCanceled(true);
                //FMLLog.info("canGrow bonemeal true");
            }
            else{
                //FMLLog.info("can grow true");
            }

        }
    }

}
