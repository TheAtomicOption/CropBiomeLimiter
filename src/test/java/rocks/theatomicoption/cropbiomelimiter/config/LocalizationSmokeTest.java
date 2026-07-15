package rocks.theatomicoption.cropbiomelimiter.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import rocks.theatomicoption.cropbiomelimiter.commands.RegistrySnapshotCommand;

public final class LocalizationSmokeTest {
	private LocalizationSmokeTest() {
	}

	public static void run() throws IOException {
		JsonObject translations = translations();
		adminCommandMessagesAreLocalized(translations);
		schemaDisplayStringsExposeTranslationKeys(translations);
		viewerStringsAreLocalized(translations);
	}

	private static void adminCommandMessagesAreLocalized(JsonObject translations) {
		for (String key : List.of(
				RegistrySnapshotCommand.EXPORT_SUCCESS_KEY,
				RegistrySnapshotCommand.EXPORT_FAILURE_KEY,
				RegistrySnapshotCommand.EXPORT_COUNT_DIMENSION_ONE_KEY,
				RegistrySnapshotCommand.EXPORT_COUNT_DIMENSION_MANY_KEY,
				RegistrySnapshotCommand.EXPORT_COUNT_BIOME_ONE_KEY,
				RegistrySnapshotCommand.EXPORT_COUNT_BIOME_MANY_KEY,
				RegistrySnapshotCommand.EXPORT_COUNT_CROP_ONE_KEY,
				RegistrySnapshotCommand.EXPORT_COUNT_CROP_MANY_KEY,
				RegistrySnapshotCommand.RELOAD_SUCCESS_KEY,
				RegistrySnapshotCommand.RELOAD_RESTORED_DEFAULTS_KEY,
				RegistrySnapshotCommand.RELOAD_RESTORED_DEFAULTS_WITH_PATH_KEY,
				RegistrySnapshotCommand.RELOAD_WARNINGS_ONE_KEY,
				RegistrySnapshotCommand.RELOAD_WARNINGS_MANY_KEY,
				RegistrySnapshotCommand.RELOAD_FAILURE_KEY
		)) {
			assertTranslation(translations, key);
		}
	}

	private static void schemaDisplayStringsExposeTranslationKeys(JsonObject translations) {
		for (RuleMode mode : RuleMode.values()) {
			assertTranslation(translations, ConfigAppSchema.translationKey(mode));
		}
		for (CropBehavior behavior : CropBehavior.values()) {
			assertTranslation(translations, ConfigAppSchema.translationKey(behavior));
		}
		for (PrecipitationRequirement requirement : PrecipitationRequirement.values()) {
			assertTranslation(translations, ConfigAppSchema.translationKey(requirement));
		}

		String schemaJson = ConfigAppSchema.metadataJson().toString();
		assertTrue(schemaJson.contains("\"translation_key\""), "config app schema metadata should expose translation keys beside display names");
	}

	private static void viewerStringsAreLocalized(JsonObject translations) {
		for (String key : List.of(
				"viewer.cropbiomelimiter.category.crop_atlas",
				"viewer.cropbiomelimiter.category.biome_atlas",
				"viewer.cropbiomelimiter.behavior_count",
				"viewer.cropbiomelimiter.temperature",
				"viewer.cropbiomelimiter.temperature_label",
				"viewer.cropbiomelimiter.temperature_range",
				"viewer.cropbiomelimiter.temperature_ranges",
				"viewer.cropbiomelimiter.no_climate_ranges",
				"viewer.cropbiomelimiter.precipitation",
				"viewer.cropbiomelimiter.precipitation_yes",
				"viewer.cropbiomelimiter.precipitation_no",
				"viewer.cropbiomelimiter.precipitation_required",
				"viewer.cropbiomelimiter.precipitation_forbidden",
				"viewer.cropbiomelimiter.precipitation_ignored",
				"viewer.cropbiomelimiter.precipitation_mixed",
				"viewer.cropbiomelimiter.climate_rule"
		)) {
			assertTranslation(translations, key);
		}
	}

	private static JsonObject translations() throws IOException {
		try (InputStream stream = LocalizationSmokeTest.class.getClassLoader().getResourceAsStream("assets/cropbiomelimiter/lang/en_us.json")) {
			if (stream == null) {
				throw new AssertionError("en_us localization should be packaged in the test runtime classpath");
			}
			return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static void assertTranslation(JsonObject translations, String key) {
		assertTrue(translations.has(key), "en_us localization should include " + key);
		assertTrue(!translations.get(key).getAsString().isBlank(), "en_us localization should not leave " + key + " blank");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
