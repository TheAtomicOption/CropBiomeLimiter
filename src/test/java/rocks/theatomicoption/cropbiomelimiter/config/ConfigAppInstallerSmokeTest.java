package rocks.theatomicoption.cropbiomelimiter.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rocks.theatomicoption.cropbiomelimiter.config.ConfigAppInstaller.ConfigAppInstallResult;

public final class ConfigAppInstallerSmokeTest {
	private ConfigAppInstallerSmokeTest() {
	}

	public static void run() throws IOException {
		writesConfigAppWhenMissing();
		recreatesDeletedConfigAppFiles();
		refreshesChangedConfigAppFiles();
	}

	private static void writesConfigAppWhenMissing() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-config-app-test");
		ConfigAppInstallResult result = ConfigAppInstaller.tryInstall(directory);
		Path appDirectory = appDirectory(directory);

		assertTrue(result.createdDefault(), "missing config app should be written on startup");
		assertEquals(List.of(
				"README.md",
				"index.html",
				"styles.css",
				"app.js",
				"Open Config App.cmd",
				"Open Config App.sh",
				"Open Config App.command"
		), result.createdFiles(), "startup should report all restored config app files");
		assertTrue(Files.isDirectory(appDirectory), "config app directory should exist");
		assertTrue(Files.exists(appDirectory.resolve("Open Config App.cmd")), "double-click launcher should exist");
		assertTrue(Files.exists(appDirectory.resolve("Open Config App.sh")), "Linux launcher should exist");
		assertTrue(Files.exists(appDirectory.resolve("Open Config App.command")), "macOS launcher should exist");
		assertTrue(Files.readString(appDirectory.resolve("README.md")).contains("No Python"), "README should describe non-developer launch");
		assertTrue(Files.readString(appDirectory.resolve("index.html")).contains("app.js"), "index should load the app script");

		ConfigAppInstallResult secondResult = ConfigAppInstaller.tryInstall(directory);
		assertTrue(!secondResult.createdDefault(), "existing config app should not be rewritten");
	}

	private static void recreatesDeletedConfigAppFiles() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-config-app-deleted-test");
		ConfigAppInstaller.tryInstall(directory);
		Path appScript = appDirectory(directory).resolve("app.js");
		Files.delete(appScript);

		ConfigAppInstallResult result = ConfigAppInstaller.tryInstall(directory);
		assertEquals(List.of("app.js"), result.createdFiles(), "startup should restore deleted config app files");
		assertTrue(Files.exists(appScript), "deleted config app file should be restored");
	}

	private static void refreshesChangedConfigAppFiles() throws IOException {
		Path directory = Files.createTempDirectory("cropbiomelimiter-config-app-changed-test");
		ConfigAppInstaller.tryInstall(directory);
		Path launcher = appDirectory(directory).resolve("Open Config App.cmd");
		Files.writeString(launcher, "old launcher");

		ConfigAppInstallResult result = ConfigAppInstaller.tryInstall(directory);
		assertEquals(List.of("Open Config App.cmd"), result.createdFiles(), "startup should refresh changed bundled config app files");
		assertTrue(Files.readString(launcher).contains("ConfigAppServer"), "changed launcher should be refreshed from bundled resources");
	}

	private static Path appDirectory(Path minecraftConfigDirectory) {
		return minecraftConfigDirectory
				.resolve(ConfigAppSchema.CONFIG_DIRECTORY_NAME)
				.resolve(ConfigAppInstaller.APP_DIRECTORY_NAME);
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + " Expected: " + expected + ", actual: " + actual);
		}
	}
}
