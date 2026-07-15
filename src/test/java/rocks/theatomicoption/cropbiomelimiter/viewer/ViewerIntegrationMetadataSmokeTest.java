package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ViewerIntegrationMetadataSmokeTest {
	private static final String JEI_PLUGIN =
			"rocks.theatomicoption.cropbiomelimiter.viewer.jei.CropBiomeLimiterJeiPlugin";
	private static final String REI_PLUGIN =
			"rocks.theatomicoption.cropbiomelimiter.viewer.rei.CropBiomeLimiterReiClientPlugin";

	private ViewerIntegrationMetadataSmokeTest() {
	}

	public static void run() throws IOException {
		JsonObject entrypoints = metadata().getAsJsonObject("entrypoints");
		assertEntrypoint(entrypoints, "jei_mod_plugin", JEI_PLUGIN);
		assertEntrypoint(entrypoints, "rei_client", REI_PLUGIN);
	}

	private static JsonObject metadata() throws IOException {
		try (InputStream stream = ViewerIntegrationMetadataSmokeTest.class.getClassLoader()
				.getResourceAsStream("fabric.mod.json")) {
			if (stream == null) {
				throw new AssertionError("fabric.mod.json should be packaged in the test runtime classpath");
			}
			return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static void assertEntrypoint(JsonObject entrypoints, String key, String className) {
		if (entrypoints == null || !entrypoints.has(key)) {
			throw new AssertionError("fabric.mod.json should declare the " + key + " entrypoint");
		}
		JsonArray values = entrypoints.getAsJsonArray(key);
		boolean found = values.asList().stream()
				.anyMatch(value -> value.isJsonPrimitive() && className.equals(value.getAsString()));
		if (!found) {
			throw new AssertionError(key + " should register " + className);
		}
	}
}
