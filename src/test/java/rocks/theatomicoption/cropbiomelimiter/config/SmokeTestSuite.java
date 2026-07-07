package rocks.theatomicoption.cropbiomelimiter.config;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
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
		CoreBehaviorSmokeTest.run();
		VillageFarmProcessorListsSmokeTest.run();
		VanillaWorldgenCosmeticDefaultsSmokeTest.run();
	}
}
