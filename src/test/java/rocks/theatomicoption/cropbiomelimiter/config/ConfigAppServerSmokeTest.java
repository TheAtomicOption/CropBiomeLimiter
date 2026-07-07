package rocks.theatomicoption.cropbiomelimiter.config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import rocks.theatomicoption.cropbiomelimiter.configapp.ConfigAppServer;
import rocks.theatomicoption.cropbiomelimiter.configapp.ConfigAppServer.RunningServer;

public final class ConfigAppServerSmokeTest {
	private ConfigAppServerSmokeTest() {
	}

	public static void run() throws Exception {
		servesAppFilesAndWritesConfigFiles();
	}

	private static void servesAppFilesAndWritesConfigFiles() throws Exception {
		Path configDirectory = Files.createTempDirectory("cropbiomelimiter-config-app-server-test");
		Path appDirectory = configDirectory.resolve(ConfigAppInstaller.APP_DIRECTORY_NAME);
		Files.createDirectories(appDirectory);
		Files.writeString(appDirectory.resolve("index.html"), "<!doctype html><title>Config</title>", StandardCharsets.UTF_8);
		Files.writeString(configDirectory.resolve(CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME), "{\"schema_version\":1}", StandardCharsets.UTF_8);

		try (RunningServer server = ConfigAppServer.start(appDirectory, 41831, false)) {
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> indexResponse = send(client, request(server.uri()));
			assertEquals(200, indexResponse.statusCode(), "server should serve app index");
			assertTrue(indexResponse.body().contains("Config"), "server should return app file body");

			URI generalUri = server.uri().resolve("/api/files/general");
			HttpResponse<String> generalResponse = send(client, request(generalUri));
			assertEquals(200, generalResponse.statusCode(), "server should read installed general config");
			assertTrue(generalResponse.body().contains("schema_version"), "server should return config file body");

			String updatedJson = "{\"schema_version\":1,\"chat_info\":false}\n";
			HttpRequest writeRequest = HttpRequest.newBuilder(generalUri)
					.header("Content-Type", "application/json")
					.PUT(HttpRequest.BodyPublishers.ofString(updatedJson))
					.build();
			HttpResponse<String> writeResponse = send(client, writeRequest);
			assertEquals(200, writeResponse.statusCode(), "server should write installed general config");
			assertEquals(updatedJson, Files.readString(configDirectory.resolve(CropBiomeLimiterConfigLoader.GENERAL_FILE_NAME)), "server should persist config file edits");

			HttpResponse<String> deniedResponse = send(client, HttpRequest.newBuilder(server.uri().resolve("/api/files/snapshot"))
					.PUT(HttpRequest.BodyPublishers.ofString("{}"))
					.build());
			assertEquals(403, deniedResponse.statusCode(), "server should keep registry snapshots read only");
		}
	}

	private static HttpRequest request(URI uri) {
		return HttpRequest.newBuilder(uri).GET().build();
	}

	private static HttpResponse<String> send(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
		return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
