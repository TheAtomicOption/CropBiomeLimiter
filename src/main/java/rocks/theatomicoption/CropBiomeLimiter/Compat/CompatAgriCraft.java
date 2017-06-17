package rocks.theatomicoption.CropBiomeLimiter.Compat;


import com.infinityraider.agricraft.api.requirement.ICondition;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import rocks.theatomicoption.CropBiomeLimiter.CropBiomeLogic;

import java.util.List;

import static rocks.theatomicoption.CropBiomeLimiter.CropBiomeLimiter.config;

/**
 * Implements compatibility with AgriCraft?
 *
 * Not sure if this ICondition thing is what I need to interface with AgriCraft, but maybe? Agricraft is a complicated mod...
 */
public class CompatAgriCraft  implements ICondition {

    @Override
    public int getComplexity() {
        return 1;
    }

    @Override
    public boolean isMet(IBlockAccess iBlockAccess, BlockPos blockPos) {

        //this won't work because the block it sends will be the Agricraft crop block not the regular crop inside
        // ( Agricraft wraps that--not sure yet). Need to find/return the original crop block instead.
        Block a = iBlockAccess.getBlockState(blockPos).getBlock();
        return CropBiomeLogic.canGrowExplicit(iBlockAccess.getBiome(blockPos),
                iBlockAccess.getBlockState(blockPos).getBlock(),
                iBlockAccess.getWorldType().getWorldTypeID());
    }

    @Override
    public void addDescription(List<String> lines) {
        lines.add("Crop Biome Limiter conditions");
    }
}
