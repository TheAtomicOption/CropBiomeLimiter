package rocks.theatomicoption.cropbiomelimiter.config;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import rocks.theatomicoption.cropbiomelimiter.events.PlayerActionHandlerSmokeTest;
import rocks.theatomicoption.cropbiomelimiter.logic.NaturalGrowthResultDetectorSmokeTest;
import rocks.theatomicoption.cropbiomelimiter.viewer.ViewerAtlasSmokeTest;
import rocks.theatomicoption.cropbiomelimiter.viewer.ViewerIntegrationMetadataSmokeTest;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.ClientAtlasDataSmokeTest;
import rocks.theatomicoption.cropbiomelimiter.worldgen.VanillaWorldgenCosmeticDefaultsSmokeTest;
import rocks.theatomicoption.cropbiomelimiter.worldgen.VillageFarmProcessorListsSmokeTest;

public final class SmokeTestSuite {
	private SmokeTestSuite() {
	}

	public static void main(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		ConfigLoaderSmokeTest.run();
		ConfigAppInstallerSmokeTest.run();
		ConfigAppServerSmokeTest.run();
		RegistrySnapshotExporterSmokeTest.run();
		LocalizationSmokeTest.run();
		PlayerActionHandlerSmokeTest.run();
		CoreBehaviorSmokeTest.run();
		NaturalGrowthResultDetectorSmokeTest.run();
		ViewerAtlasSmokeTest.run();
		ClientAtlasDataSmokeTest.run();
		ViewerIntegrationMetadataSmokeTest.run();
		VillageFarmProcessorListsSmokeTest.run();
		VanillaWorldgenCosmeticDefaultsSmokeTest.run();
	}
}
