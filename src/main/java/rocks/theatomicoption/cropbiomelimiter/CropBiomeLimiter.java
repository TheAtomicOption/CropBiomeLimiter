package rocks.theatomicoption.cropbiomelimiter;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theatomicoption.cropbiomelimiter.commands.RegistrySnapshotCommand;
import rocks.theatomicoption.cropbiomelimiter.config.ConfigLoadResult;
import rocks.theatomicoption.cropbiomelimiter.config.ConfigAppInstaller;
import rocks.theatomicoption.cropbiomelimiter.config.ConfigAppInstaller.ConfigAppInstallResult;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfigLoader;
import rocks.theatomicoption.cropbiomelimiter.events.PlayerActionHandler;
import rocks.theatomicoption.cropbiomelimiter.logic.CropDecisionService;
import rocks.theatomicoption.cropbiomelimiter.logic.GrowableBlockClassifier;

public class CropBiomeLimiter implements ModInitializer {
	public static final String MOD_ID = "cropbiomelimiter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static volatile CropDecisionService cropDecisionService = CropDecisionService.allowAll();

	@Override
	public void onInitialize() {
		cropDecisionService = createDecisionService();
		registerCallbacks();

		LOGGER.info("Crop Biome Limiter initialized with canonical Explicit mode and Threshold mode support.");
	}

	private static CropDecisionService createDecisionService() {
		return tryCreateDecisionService().map(DecisionServiceResult::service).orElseGet(CropDecisionService::allowAll);
	}

	public static boolean reloadConfig() {
		return reloadConfigWithResult().loaded();
	}

	public static ConfigReloadResult reloadConfigWithResult() {
		Optional<DecisionServiceResult> result = tryCreateDecisionService();
		if (result.isPresent()) {
			DecisionServiceResult decisionServiceResult = result.get();
			cropDecisionService = decisionServiceResult.service();
			return ConfigReloadResult.loaded(decisionServiceResult.loadResult());
		}

		cropDecisionService = CropDecisionService.allowAll();
		return ConfigReloadResult.failed();
	}

	private static Optional<DecisionServiceResult> tryCreateDecisionService() {
		try {
			Path minecraftConfigDirectory = FabricLoader.getInstance().getConfigDir();
			ConfigAppInstallResult appInstallResult = ConfigAppInstaller.tryInstall(minecraftConfigDirectory);
			logConfigAppResult(appInstallResult);
			ConfigLoadResult loadResult = CropBiomeLimiterConfigLoader.tryLoadConfig(minecraftConfigDirectory);
			logConfigResult(loadResult);
			return Optional.of(new DecisionServiceResult(new CropDecisionService(loadResult.config(), new GrowableBlockClassifier()), loadResult));
		} catch (RuntimeException exception) {
			LOGGER.error("Crop Biome Limiter failed to create its decision service. Gameplay will be allowed by default.", exception);
			return Optional.empty();
		}
	}

	private static void logConfigAppResult(ConfigAppInstallResult installResult) {
		try {
			if (installResult.createdDefault()) {
				LOGGER.info("Crop Biome Limiter restored missing config app file(s) {} in {}.", installResult.createdFileSummary(), installResult.path());
			}
			if (installResult.hasDiagnostics()) {
				LOGGER.warn("Crop Biome Limiter installed config app files in {} with {} warning(s).", installResult.path(), installResult.diagnosticCount());
			}
			for (String diagnostic : installResult.diagnostics()) {
				LOGGER.warn("Crop Biome Limiter config app: {}", diagnostic);
			}
		} catch (RuntimeException exception) {
			LOGGER.debug("Crop Biome Limiter could not report config app diagnostics.", exception);
		}
	}

	private static void logConfigResult(ConfigLoadResult loadResult) {
		try {
			if (loadResult.createdDefault()) {
				LOGGER.info("Crop Biome Limiter restored missing default config file(s) {} in {}.", loadResult.createdDefaultFileSummary(), loadResult.path());
			}
			if (loadResult.hasDiagnostics()) {
				LOGGER.warn("Crop Biome Limiter loaded config from {} with {} warning(s).", loadResult.path(), loadResult.diagnosticCount());
			}
			for (String diagnostic : loadResult.diagnostics()) {
				LOGGER.warn("Crop Biome Limiter config: {}", diagnostic);
			}
		} catch (RuntimeException exception) {
			LOGGER.debug("Crop Biome Limiter could not report config diagnostics.", exception);
		}
	}

	private static void registerCallbacks() {
		try {
			PlayerActionHandler.register();
		} catch (RuntimeException exception) {
			cropDecisionService = CropDecisionService.allowAll();
			LOGGER.error("Crop Biome Limiter failed to register gameplay callbacks. Gameplay will be allowed by default.", exception);
		}

		try {
			RegistrySnapshotCommand.register();
		} catch (RuntimeException exception) {
			LOGGER.error("Crop Biome Limiter failed to register admin commands. Gameplay callbacks will keep their current behavior.", exception);
		}
	}

	public static CropDecisionService cropDecisionService() {
		CropDecisionService service = cropDecisionService;
		return service == null ? CropDecisionService.allowAll() : service;
	}

	public static ResourceLocation id(String path) {
		return tryId(path).orElseGet(() -> new ResourceLocation(MOD_ID, "invalid"));
	}

	public static Optional<ResourceLocation> tryId(String path) {
		if (path == null || path.isBlank()) {
			return Optional.empty();
		}

		try {
			return Optional.of(new ResourceLocation(MOD_ID, path));
		} catch (RuntimeException exception) {
			LOGGER.debug("Ignoring invalid Crop Biome Limiter ResourceLocation path: {}", path, exception);
			return Optional.empty();
		}
	}

	private record DecisionServiceResult(CropDecisionService service, ConfigLoadResult loadResult) {
	}

	public record ConfigReloadResult(
			boolean loaded,
			Path path,
			List<String> diagnostics,
			List<String> createdDefaultFiles
	) {
		public ConfigReloadResult {
			diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
			createdDefaultFiles = createdDefaultFiles == null ? List.of() : List.copyOf(createdDefaultFiles);
		}

		public static ConfigReloadResult loaded(ConfigLoadResult loadResult) {
			return new ConfigReloadResult(true, loadResult.path(), loadResult.diagnostics(), loadResult.createdDefaultFiles());
		}

		public static ConfigReloadResult failed() {
			return new ConfigReloadResult(false, null, List.of(), List.of());
		}

		public boolean hasDiagnostics() {
			return !diagnostics.isEmpty();
		}

		public int diagnosticCount() {
			return diagnostics.size();
		}

		public boolean createdDefault() {
			return !createdDefaultFiles.isEmpty();
		}

		public String createdDefaultFileSummary() {
			return String.join(", ", createdDefaultFiles);
		}
	}
}
