package rocks.theatomicoption.cropbiomelimiter.config;

import java.nio.file.Path;
import java.util.List;

public record ConfigLoadResult(
		CropBiomeLimiterConfig config,
		Path path,
		List<String> diagnostics,
		List<String> createdDefaultFiles
) {
	public ConfigLoadResult {
		diagnostics = List.copyOf(diagnostics);
		createdDefaultFiles = List.copyOf(createdDefaultFiles);
	}

	public ConfigLoadResult(CropBiomeLimiterConfig config, Path path, List<String> diagnostics, boolean createdDefault) {
		this(config, path, diagnostics, createdDefault ? List.of("unknown") : List.of());
	}

	public boolean hasDiagnostics() {
		return !diagnostics.isEmpty();
	}

	public boolean createdDefault() {
		return !createdDefaultFiles.isEmpty();
	}

	public int diagnosticCount() {
		return diagnostics.size();
	}

	public String createdDefaultFileSummary() {
		return String.join(", ", createdDefaultFiles);
	}
}
