package rocks.theatomicoption.cropbiomelimiter.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class ConfigAppInstaller {
	public static final String APP_DIRECTORY_NAME = "config-app";

	private static final List<AppFile> APP_FILES = List.of(
			new AppFile("README.md"),
			new AppFile("index.html"),
			new AppFile("styles.css"),
			new AppFile("app.js"),
			new AppFile("Open Config App.cmd"),
			new AppFile("open-config-app.ps1")
	);

	private ConfigAppInstaller() {
	}

	public static ConfigAppInstallResult tryInstall(Path minecraftConfigDirectory) {
		List<String> diagnostics = new ArrayList<>();
		List<String> createdFiles = new ArrayList<>();
		Path appDirectory = appDirectory(minecraftConfigDirectory);

		tryCreateDirectory(appDirectory, diagnostics);
		for (AppFile appFile : APP_FILES) {
			Path target = appDirectory.resolve(appFile.fileName());
			if (fileExists(target, diagnostics)) {
				continue;
			}
			if (tryWriteAppFile(appFile, target, diagnostics)) {
				createdFiles.add(appFile.fileName());
			}
		}

		return new ConfigAppInstallResult(appDirectory, createdFiles, diagnostics);
	}

	private static Path appDirectory(Path minecraftConfigDirectory) {
		Path configDirectory = minecraftConfigDirectory == null
				? Path.of(ConfigAppSchema.CONFIG_DIRECTORY_NAME)
				: minecraftConfigDirectory.resolve(ConfigAppSchema.CONFIG_DIRECTORY_NAME);
		return configDirectory.resolve(APP_DIRECTORY_NAME);
	}

	private static void tryCreateDirectory(Path path, List<String> diagnostics) {
		try {
			Files.createDirectories(path);
		} catch (IOException | RuntimeException exception) {
			diagnostics.add("Could not create config app directory " + path + ".");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not create its config app directory: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter config app directory creation failure details.", exception);
		}
	}

	private static boolean fileExists(Path path, List<String> diagnostics) {
		try {
			return Files.exists(path);
		} catch (RuntimeException exception) {
			diagnostics.add("Could not inspect config app file " + path + ".");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not inspect its config app file: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter config app inspection failure details.", exception);
			return true;
		}
	}

	private static boolean tryWriteAppFile(AppFile appFile, Path target, List<String> diagnostics) {
		try (InputStream input = ConfigAppInstaller.class.getClassLoader().getResourceAsStream(appFile.resourcePath())) {
			if (input == null) {
				diagnostics.add("Bundled config app resource is missing: " + appFile.resourcePath() + ".");
				return false;
			}
			Path parent = target.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException | RuntimeException exception) {
			diagnostics.add("Could not write config app file " + target + ".");
			CropBiomeLimiter.LOGGER.warn("Crop Biome Limiter could not write its config app file: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter config app write failure details.", exception);
			return false;
		}
	}

	private record AppFile(String fileName) {
		private String resourcePath() {
			return APP_DIRECTORY_NAME + "/" + fileName;
		}
	}

	public record ConfigAppInstallResult(Path path, List<String> createdFiles, List<String> diagnostics) {
		public ConfigAppInstallResult {
			createdFiles = createdFiles == null ? List.of() : List.copyOf(createdFiles);
			diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
		}

		public boolean createdDefault() {
			return !createdFiles.isEmpty();
		}

		public boolean hasDiagnostics() {
			return !diagnostics.isEmpty();
		}

		public int diagnosticCount() {
			return diagnostics.size();
		}

		public String createdFileSummary() {
			return String.join(", ", createdFiles);
		}
	}
}
