package rocks.theatomicoption.CropBiomeLimiter;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Does this really need to be a separate event handler file? who knows. Yet another reason to centralize logic
 * for whether crops work in a given biome.
 */
public class CropPlantEventHandler {

    /**
     * Block Crop growth in Biomes disallowed by config.
     * */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onCropGrowEvent(BlockEvent.CropGrowEvent.Pre event) {
        event.setResult(Event.Result.DENY);
        System.out.println("Crop tried to grow in wrong biome!");
    }
}
