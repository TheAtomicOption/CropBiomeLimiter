package rocks.theatomicoption.CropBiomeLimiter;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy; //you probably will need this at some point... for something. need to test mod on dedicated server
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.common.config.Configuration;


@Mod(modid = CropBiomeLimiter.modId, name = CropBiomeLimiter.name, version = CropBiomeLimiter.version, acceptedMinecraftVersions = "[1.10.2]")
public class CropBiomeLimiter {

    public static final String modId = "cropbiomelimiter";
    public static final String name = "CropBiomeLimiter";
    public static final String version = "1.0.0";

    public static ConfigHandler config; //Forge Configuration field

    @Mod.Instance(modId)
    public static CropBiomeLimiter instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println(name + " is loading!");

        String configpath = event.getSuggestedConfigurationFile().getPath() + "/CropBiomeLimiter/";




        config = new ConfigHandler(new Configuration(event.getSuggestedConfigurationFile()));

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new CropGrowthEventHandler());
    }
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        config.biomeGroupDefValidation();

    }
}
