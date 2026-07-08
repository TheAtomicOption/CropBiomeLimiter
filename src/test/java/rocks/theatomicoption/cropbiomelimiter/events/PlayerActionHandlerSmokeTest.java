package rocks.theatomicoption.cropbiomelimiter.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class PlayerActionHandlerSmokeTest {
	private PlayerActionHandlerSmokeTest() {
	}

	public static void run() throws IOException {
		JsonObject translations = translations();
		assertEquals("message.cropbiomelimiter.blocked", PlayerActionHandler.BLOCKED_MESSAGE_KEY, "blocked message should use a stable translation key");
		assertEquals("message.cropbiomelimiter.allowed_placement_warning", PlayerActionHandler.ALLOWED_PLACEMENT_WARNING_KEY, "allowed placement warning should use a stable translation key");
		assertTranslation(translations, PlayerActionHandler.BLOCKED_MESSAGE_KEY, "This plant can't grow in this climate.");
		assertTranslation(translations, PlayerActionHandler.ALLOWED_PLACEMENT_WARNING_KEY, "The young plant begins to wilt in this foreign biome.");
	}

	private static JsonObject translations() throws IOException {
		try (InputStream stream = PlayerActionHandlerSmokeTest.class.getClassLoader().getResourceAsStream("assets/cropbiomelimiter/lang/en_us.json")) {
			if (stream == null) {
				throw new AssertionError("en_us localization should be packaged in the test runtime classpath");
			}
			return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static void assertTranslation(JsonObject translations, String key, String expected) {
		assertTrue(translations.has(key), "en_us localization should include " + key);
		assertEquals(expected, translations.get(key).getAsString(), "en_us localization should match the expected English fallback");
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
