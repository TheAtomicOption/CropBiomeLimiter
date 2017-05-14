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

    private static final String CATEGORY_DIMENSIONS = "dimensions";
    private static final String CATEGORY_BIOME_TYPES = "biome types";
    private static final String CATEGORY_BIOME_LIST = "biome list";

    // Defaults
    private static final boolean defaultisBlacklistBiome = true;
    private static final boolean defaultisBlacklistDim = true;
    private static final int[] defaultDimensionList = new int[]{};

    private static final String[] defaultSnowyCrops = new String[]{};
    private static final String[] defaultColdCrops = new String[]{"minecraft:wheat","minecraft:potatoes"};
    private static final String[] defaultDryCrops = new String[]{"minecraft:cactus,minecraft:beetroots"};
    private static final String[] defaultLushCrops = new String[]{"minecraft:wheat","minecraft:potatoes","minecraft:carrots","minecraft:beetroots"};

    private static final String[] defaultBiomeSavanna = new String[]{};

    // Config
    public boolean isBlacklistBiome = true;
    public boolean isBlacklistDim = true;
    //private final Map<String, String[]> gardenDropConfig = new HashMap<String, String[]>();

    private final Map<String, String[]> biomeConfig = new HashMap<String, String[]>();
    private final Map<String, String[]> biomeTypeConfig = new HashMap<String, String[]>();

    public int[] dimensionList;


    public ConfigHandler(Configuration config){
        this.config = config;

        initSettings();
    }
    private void initSettings(){
        try {
            // loads config from disk
            config.load();
            initBiomeList();
            initBiomeTypes();
            initDimensions();

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
        config.addCustomCategoryComment(CATEGORY_BIOME_LIST,"Crop/Biome list");
        isBlacklistBiome = config.getBoolean("isBlacklistBiome",CATEGORY_BIOME_LIST,defaultisBlacklistBiome,
                "If true, crops/biome pairs determine where crops can't grow, and crop/BiomeTypes below determine " +
                        "where crops can when not listed here. Otherwise the behavior of both is reversed.");
        biomeConfig.put("Savanna",config.getStringList("Savanna",CATEGORY_BIOME_LIST,defaultBiomeSavanna,""));

    }
    private void initBiomeTypes(){
        config.addCustomCategoryComment(CATEGORY_BIOME_TYPES,"Crop/BiomeType list");
        biomeTypeConfig.put("SNOWY", config.getStringList("Snowy",CATEGORY_BIOME_TYPES,defaultSnowyCrops,""));
        biomeTypeConfig.put("COLD", config.getStringList("Cold",CATEGORY_BIOME_TYPES,defaultColdCrops,""));
        biomeTypeConfig.put("MEDIUM", config.getStringList("Medium/Lush",CATEGORY_BIOME_TYPES,defaultLushCrops,""));
        biomeTypeConfig.put("WARM", config.getStringList("Dry/Warm",CATEGORY_BIOME_TYPES,defaultDryCrops,""));
        //        gardenDropConfig.put("aridGarden", config.getStringList("aridGarden", "drops", new String[]{"harvestcraft:cactusfruitItem"}, ""));

    }
    private void initDimensions(){
        config.addCustomCategoryComment(CATEGORY_DIMENSIONS,
                "List of dimensions");
        isBlacklistBiome = config.getBoolean("isBlacklistBiome",CATEGORY_BIOME_LIST,defaultisBlacklistDim,
                "If true, Dimension list is a blacklist and this mod will not affect the ability of crops to grow there. " +
                        "If false only crops in listed dimensions will have their ability to grow affected by limits in this mod.");
        config.get(CATEGORY_DIMENSIONS,"Dimensions",defaultDimensionList,"List of dimensions").getIntList();
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
        //FMLLog.info("testing biome " + biome.getBiomeName() + " for crop " + crop.getRegistryName().toString());
        //for each crop listed in biome list
        for(final String s : this.biomeConfig.get(biome.getBiomeName())) {
            if (s.equals(crop.getRegistryName().toString())){
                //FMLLog.info("Crop %s is explicitly in biome %s",s,biome.getBiomeName());
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
                //FMLLog.info("%s is explicitly listed in biomeType %s",s,biome.getTempCategory().name());
                return true;
            }
        }
        return false;
    }
}
