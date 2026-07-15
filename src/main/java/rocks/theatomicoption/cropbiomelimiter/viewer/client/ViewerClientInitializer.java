package rocks.theatomicoption.cropbiomelimiter.viewer.client;

import java.lang.reflect.InvocationTargetException;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class ViewerClientInitializer implements ClientModInitializer {
	private static final String JEI_BRIDGE = "rocks.theatomicoption.cropbiomelimiter.viewer.jei.CropBiomeLimiterJeiPlugin";
	private static final String REI_BRIDGE = "rocks.theatomicoption.cropbiomelimiter.viewer.rei.CropBiomeLimiterReiClientPlugin";

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> refreshInstalledViewers());
	}

	private static void refreshInstalledViewers() {
		FabricLoader loader = FabricLoader.getInstance();
		if (loader.isModLoaded("jei")) {
			invokeRefresh(JEI_BRIDGE);
		}
		if (loader.isModLoaded("roughlyenoughitems")) {
			invokeRefresh(REI_BRIDGE);
		}
	}

	private static void invokeRefresh(String className) {
		try {
			Class.forName(className).getMethod("refreshAfterJoin").invoke(null);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not initialize an installed recipe viewer integration: {}", className, exception);
		} catch (InvocationTargetException exception) {
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter recipe viewer integration failed while building the local atlas: {}", className, exception.getCause());
		}
	}
}
