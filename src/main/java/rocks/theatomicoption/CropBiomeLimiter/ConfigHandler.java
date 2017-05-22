package rocks.theatomicoption.CropBiomeLimiter;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;

import net.minecraftforge.fml.common.FMLLog;

/**
 *
 */
public class ConfigHandler {

    private final Configuration config;

    private static final String CATEGORY_BIOME_TYPES = "biome types";
    private static final String CATEGORY_BIOME = "biomes";

    // Defaults
    private static final boolean defaultisBlacklistBiome = true;
    private static final boolean defaultisBlacklistDim = true;
    private static final boolean defaultChatInfo = true;
    private static final boolean defaultAffectsBonemeal = true;
    private static final boolean defaultAffectsBlockPlacement = true;

    private static final int[] defaultDimensionList = new int[]{};

    private static final String[] defaultFarmland = new String[]{"minecraft:farmland"};

    private static final String[] defaultColdCrops = new String[]{"minecraft:wheat","minecraft:potatoes","minecraft:pumpkin_stem","minecraft:pumpkin",
            "minecraft:grass","minecraft:sapling"};
    private static final String[] defaultDryCrops = new String[]{"minecraft:cactus","minecraft:beetroots","minecraft:melon",
            "minecraft:grass","minecraft:sapling"};
    private static final String[] defaultLushCrops = new String[]{"minecraft:wheat","minecraft:potatoes","minecraft:carrots","minecraft:beetroots",
            "minecraft:melon","minecraft:melon_stem","minecraft:pumpkin_stem","minecraft:pumpkin","minecraft:cocoa",
            "minecraft:grass","minecraft:sapling","minecraft:brown_mushroom","minecraft:red_mushroom"};

    private static final String[] defaultBiome = new String[]{};
    private static final String[] defaultExclusions = new String[]{};

    // Config
    public boolean isBlacklistBiome = true;
    public boolean isBlacklistDim = true;

    public boolean affectsBonemeal = true;
    public boolean affectsBlockPlacement = true;
    public boolean chatInfo = true;

    private String[] farmBlocks = new String[]{};
    private String[] exclusions = new String[]{};
    public int[] dimensionList;

    private final Map<String, String[]> biomeConfig = new HashMap<String, String[]>();
    private final Map<String, String[]> biomeTypeConfig = new HashMap<String, String[]>();




    public ConfigHandler(Configuration config){
        this.config = config;

        initSettings();
    }

    private void initSettings(){
        try {
            // loads config from disk
            config.load();


            exclusions = config.getStringList("Excluded Blocks","General",defaultExclusions,
                    "List of specific blocks to exclude from ability-to-grow logic in. Available in case this mod conflicts with something." );
            affectsBonemeal = config.getBoolean("Affects Bonemeal","General",defaultAffectsBonemeal,
                    "If true, crops which can't grow also can't be forced to grow with bonemeal." );
            affectsBlockPlacement = config.getBoolean("Affects Block Placement","General",defaultAffectsBlockPlacement,
                    "If true, crops which can't grow are also blocked from being planted. This may be the only way to stop crops added by " +
                            "mods which override or don't properly implement the Forge event CropGrowEvent.Pre" );
            farmBlocks = config.getStringList("Farm Blocks","General",defaultFarmland,
                    "Only things that grow on farm blocks will be affected by this mod.");
            chatInfo = config.getBoolean("Info about failed planting to chat","General",defaultChatInfo,
                    "If true, players are informed in chat when they attempt to place a block in a disallowed biome.");

            isBlacklistBiome = config.getBoolean("isBlacklistBiome","General",defaultisBlacklistDim,
                    "If true, Dimension list is a blacklist and this mod will not affect the ability of crops to grow in listed dimensions. " +
                            "If false only crops in listed dimensions will have their ability to grow affected by limits in this mod.");
            dimensionList = config.get("General","Dimensions",defaultDimensionList,"list of dimensions for Dimension whitelist/blacklist").getIntList();

            initBiomeList();
            initBiomeTypes();

        } catch (Exception e){
            //Failed Reading/writing, do nothing
        } finally {
            //Save config if config has changed
            if(config.hasChanged()){
                config.save();
            }
        }
    }

    private void initBiomeList(){
        //config.addCustomCategoryComment("","Crop/Biome list");
        isBlacklistBiome = config.getBoolean("isBlacklistBiome","General",defaultisBlacklistBiome,
                "If true, crops/biome pairs determine where crops can't grow, and crop/BiomeTypes below determine " +
                        "where crops can when not listed here. Otherwise the behavior of both is reversed.");

        //empty defaults
        biomeConfig.put("Biomes", config.getStringList("Savanna",CATEGORY_BIOME,defaultBiome,""));
        biomeConfig.put("Biomes", config.getStringList("Forest",CATEGORY_BIOME,defaultBiome,""));
        biomeConfig.put("Biomes", config.getStringList("Plains",CATEGORY_BIOME,defaultBiome,""));

        //find all biomes entries
        for (String b : config.getCategory(CATEGORY_BIOME).getValues().keySet()){
            String[] c = new String[]{};
            biomeConfig.put(b,config.getStringList(b, CATEGORY_BIOME, c,""));
            FMLLog.info("found biome list: " + b + " containing count of blocks: " + biomeConfig.get(b).length);

        };


    }

    private void initBiomeTypes(){
        config.addCustomCategoryComment(CATEGORY_BIOME_TYPES,"Note that for both biome and biome type lists, you must also include the stem type for plants with stems." +
                "Sapplings, grass, dirt and podzol are included so that using bonemeal on them continues to work.");
        biomeTypeConfig.put("COLD", config.getStringList("Cold",CATEGORY_BIOME_TYPES,defaultColdCrops,"Note: minecraft wiki shows a 'snowy' type, but in the vanilla code those biomes are included in 'cold' type."));
        biomeTypeConfig.put("MEDIUM", config.getStringList("Medium/Lush",CATEGORY_BIOME_TYPES,defaultLushCrops,"forest, plains, jungle etc."));
        biomeTypeConfig.put("WARM", config.getStringList("Dry/Warm",CATEGORY_BIOME_TYPES,defaultDryCrops,"desert, savanna etc."));
        //        gardenDropConfig.put("aridGarden", config.getStringList("aridGarden", "drops", new String[]{"harvestcraft:cactusfruitItem"}, ""));

    }


    /**
     *
     * returns true if dimension dim is listed in config file list of dimensions
     */
    public boolean isListedDimension(int dim){
        if(this.dimensionList == null) {return false;} //No search if list is empty
        for (final int i : this.dimensionList) {
            if (i == dim) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if provided crop block is listed under provided biome in the config
     *
     */
    public boolean isListedCropBiome(Block crop, Biome biome) {
        if(this.biomeConfig == null) {return false;} //No search if list is empty
        if(this.biomeConfig.get(biome.getBiomeName()) == null) {return false;} //No search if biome does not have a list

        //for each crop listed in biome list
        for(final String s : this.biomeConfig.get(biome.getBiomeName())) {
            if (s.equals(crop.getRegistryName().toString())){

                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if provided crop block is listed under provided biome in the config
     *
     * Vanilla biomes only come in 4 categories: WARM, COLD, MEDIUM, OCEAN. It may do to add
     * the ability to create a custom set of categories with temperature and rainfall somehow.
     * Uncertain about best method for this given the constraints of the config file.
     */
    public boolean isListedCropBiomeType(Block crop, Biome biome) {
        if(this.biomeTypeConfig == null) {return false;} //No search if list is empty
        if(this.biomeTypeConfig.get(biome.getTempCategory().name()) == null) {return false;} //No search if biome does not have a list
        //FMLLog.info("testing biome type " + biome.getTempCategory().name() + " for crop " + crop.getRegistryName().toString());
        //for each crop listed in biome list
        for(final String s : this.biomeTypeConfig.get(biome.getTempCategory().name())) {
            if (s.equals(crop.getRegistryName().toString())){
                return true;
            }
        }
        return false;
    }

    public boolean isListedFarmblck(Block farm) {
        if(this.farmBlocks == null) {return false;}

        for (final String s : this.farmBlocks) {
            if(s.equals(farm.getRegistryName().toString())){ return true; }
        }
        return false;
    }
    public boolean isExcludedBlock(Block block) {
        if(this.exclusions == null) {return false;}

        for (final String s : this.exclusions) {
            if(s.equals(block.getRegistryName().toString())) { return true;}
        }
        return false;
    }
}
