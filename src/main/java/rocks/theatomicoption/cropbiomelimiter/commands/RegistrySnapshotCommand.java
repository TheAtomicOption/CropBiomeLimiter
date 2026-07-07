package rocks.theatomicoption.cropbiomelimiter.commands;

import java.io.IOException;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class RegistrySnapshotCommand {
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
			source.sendSuccess(() -> Component.literal(exportSuccessMessage(result)), true);
			return Command.SINGLE_SUCCESS;
		} catch (IOException | RuntimeException exception) {
			CropBiomeLimiter.LOGGER.error("Crop Biome Limiter failed to export a registry snapshot.", exception);
			source.sendFailure(Component.literal("Crop Biome Limiter could not export the registry snapshot. See the server log for details."));
			return 0;
		}
	}

	private static String exportSuccessMessage(RegistrySnapshotExporter.ExportResult result) {
		return "Crop Biome Limiter registry snapshot exported "
				+ count(result.dimensionCount(), "dimension")
				+ ", "
				+ count(result.biomeCount(), "biome")
				+ ", and "
				+ count(result.cropCount(), "growable block")
				+ " to "
				+ result.path()
				+ ".";
	}

	private static String count(int count, String singularName) {
		return count + " " + singularName + (count == 1 ? "" : "s");
	}

	private static int reloadConfig(CommandSourceStack source) {
		try {
			CropBiomeLimiter.ConfigReloadResult result = CropBiomeLimiter.reloadConfigWithResult();
			if (result.loaded()) {
				source.sendSuccess(() -> Component.literal(reloadSuccessMessage(result)), true);
				return Command.SINGLE_SUCCESS;
			}

			source.sendFailure(Component.literal("Crop Biome Limiter could not reload config. Gameplay is allowed by default; see the server log for details."));
			return 0;
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.error("Crop Biome Limiter failed inside the config reload command.", exception);
			source.sendFailure(Component.literal("Crop Biome Limiter could not reload config. Gameplay is allowed by default; see the server log for details."));
			return 0;
		}
	}

	private static String reloadSuccessMessage(CropBiomeLimiter.ConfigReloadResult result) {
		StringBuilder message = new StringBuilder("Crop Biome Limiter config reloaded.");
		if (result.createdDefault()) {
			message.append(" Restored missing default config file(s) ").append(result.createdDefaultFileSummary());
			if (result.path() != null) {
				message.append(" in ").append(result.path());
			}
			message.append(".");
		}
		if (result.hasDiagnostics()) {
			message.append(" ").append(result.diagnosticCount()).append(" warning");
			if (result.diagnosticCount() != 1) {
				message.append("s");
			}
			message.append("; see the server log.");
		}
		return message.toString();
	}
}
