package rocks.theatomicoption.cropbiomelimiter.commands;

import java.io.IOException;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.permissions.Permissions;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class RegistrySnapshotCommand {
	public static final String EXPORT_SUCCESS_KEY = "commands.cropbiomelimiter.export_registry.success";
	public static final String EXPORT_FAILURE_KEY = "commands.cropbiomelimiter.export_registry.failure";
	public static final String EXPORT_COUNT_DIMENSION_ONE_KEY = "commands.cropbiomelimiter.export_registry.count.dimension.one";
	public static final String EXPORT_COUNT_DIMENSION_MANY_KEY = "commands.cropbiomelimiter.export_registry.count.dimension.many";
	public static final String EXPORT_COUNT_BIOME_ONE_KEY = "commands.cropbiomelimiter.export_registry.count.biome.one";
	public static final String EXPORT_COUNT_BIOME_MANY_KEY = "commands.cropbiomelimiter.export_registry.count.biome.many";
	public static final String EXPORT_COUNT_CROP_ONE_KEY = "commands.cropbiomelimiter.export_registry.count.crop.one";
	public static final String EXPORT_COUNT_CROP_MANY_KEY = "commands.cropbiomelimiter.export_registry.count.crop.many";
	public static final String RELOAD_SUCCESS_KEY = "commands.cropbiomelimiter.reload_config.success";
	public static final String RELOAD_RESTORED_DEFAULTS_KEY = "commands.cropbiomelimiter.reload_config.restored_defaults";
	public static final String RELOAD_RESTORED_DEFAULTS_WITH_PATH_KEY = "commands.cropbiomelimiter.reload_config.restored_defaults_with_path";
	public static final String RELOAD_WARNINGS_ONE_KEY = "commands.cropbiomelimiter.reload_config.warnings.one";
	public static final String RELOAD_WARNINGS_MANY_KEY = "commands.cropbiomelimiter.reload_config.warnings.many";
	public static final String RELOAD_FAILURE_KEY = "commands.cropbiomelimiter.reload_config.failure";

	private RegistrySnapshotCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal(CropBiomeLimiter.MOD_ID)
						.then(Commands.literal("export-registry")
								.requires(RegistrySnapshotCommand::canUseAdminCommands)
								.executes(context -> exportRegistry(context.getSource())))
						.then(Commands.literal("reload-config")
								.requires(RegistrySnapshotCommand::canUseAdminCommands)
								.executes(context -> reloadConfig(context.getSource())))
		));
	}

	private static boolean canUseAdminCommands(CommandSourceStack source) {
		try {
			return source.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Hiding Crop Biome Limiter admin commands because permission evaluation failed: {}", exception.toString());
			CropBiomeLimiter.LOGGER.debug("Crop Biome Limiter command permission failure details.", exception);
			return false;
		}
	}

	private static int exportRegistry(CommandSourceStack source) {
		try {
			RegistrySnapshotExporter.ExportResult result = RegistrySnapshotExporter.export(source.getServer());
			source.sendSuccess(() -> exportSuccessMessage(result), true);
			return Command.SINGLE_SUCCESS;
		} catch (IOException | RuntimeException exception) {
			CropBiomeLimiter.LOGGER.error("Crop Biome Limiter failed to export a registry snapshot.", exception);
			source.sendFailure(Component.translatable(EXPORT_FAILURE_KEY));
			return 0;
		}
	}

	private static Component exportSuccessMessage(RegistrySnapshotExporter.ExportResult result) {
		return Component.translatable(
				EXPORT_SUCCESS_KEY,
				count(result.dimensionCount(), EXPORT_COUNT_DIMENSION_ONE_KEY, EXPORT_COUNT_DIMENSION_MANY_KEY),
				count(result.biomeCount(), EXPORT_COUNT_BIOME_ONE_KEY, EXPORT_COUNT_BIOME_MANY_KEY),
				count(result.cropCount(), EXPORT_COUNT_CROP_ONE_KEY, EXPORT_COUNT_CROP_MANY_KEY),
				result.path().toString()
		);
	}

	private static Component count(int count, String singularKey, String pluralKey) {
		return Component.translatable(count == 1 ? singularKey : pluralKey, count);
	}

	private static int reloadConfig(CommandSourceStack source) {
		try {
			CropBiomeLimiter.ConfigReloadResult result = CropBiomeLimiter.reloadConfigWithResult();
			if (result.loaded()) {
				source.sendSuccess(() -> reloadSuccessMessage(result), true);
				return Command.SINGLE_SUCCESS;
			}

			source.sendFailure(Component.translatable(RELOAD_FAILURE_KEY));
			return 0;
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.error("Crop Biome Limiter failed inside the config reload command.", exception);
			source.sendFailure(Component.translatable(RELOAD_FAILURE_KEY));
			return 0;
		}
	}

	private static Component reloadSuccessMessage(CropBiomeLimiter.ConfigReloadResult result) {
		MutableComponent message = Component.translatable(RELOAD_SUCCESS_KEY);
		if (result.createdDefault()) {
			if (result.path() != null) {
				message.append(" ").append(Component.translatable(
						RELOAD_RESTORED_DEFAULTS_WITH_PATH_KEY,
						result.createdDefaultFileSummary(),
						result.path().toString()
				));
			} else {
				message.append(" ").append(Component.translatable(
						RELOAD_RESTORED_DEFAULTS_KEY,
						result.createdDefaultFileSummary()
				));
			}
		}
		if (result.hasDiagnostics()) {
			message.append(" ").append(Component.translatable(
					result.diagnosticCount() == 1 ? RELOAD_WARNINGS_ONE_KEY : RELOAD_WARNINGS_MANY_KEY,
					result.diagnosticCount()
			));
		}
		return message;
	}
}
