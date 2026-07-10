package rocks.theatomicoption.cropbiomelimiter.config;

import java.nio.file.Path;

import rocks.theatomicoption.cropbiomelimiter.commands.RegistrySnapshotExporter;

public final class RegistrySnapshotExporterSmokeTest {
	private RegistrySnapshotExporterSmokeTest() {
	}

	public static void run() {
		writesSnapshotWhereConfigAppReadsIt();
	}

	private static void writesSnapshotWhereConfigAppReadsIt() {
		Path configDirectory = Path.of("config");
		Path expected = configDirectory
				.resolve(ConfigAppSchema.CONFIG_DIRECTORY_NAME)
				.resolve(ConfigAppSchema.REGISTRY_SNAPSHOT_FILE_NAME);

		assertEquals(expected, RegistrySnapshotExporter.snapshotPath(configDirectory), "registry snapshot should be exported beside installed config app files");
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " Expected: " + expected + ", actual: " + actual);
		}
	}
}
