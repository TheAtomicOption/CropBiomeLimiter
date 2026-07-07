package rocks.theatomicoption.cropbiomelimiter.config;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class SmokeTestSuite {
	private SmokeTestSuite() {
	}

	public static void main(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		ConfigLoaderSmokeTest.run();
		CoreBehaviorSmokeTest.run();
	}
}
