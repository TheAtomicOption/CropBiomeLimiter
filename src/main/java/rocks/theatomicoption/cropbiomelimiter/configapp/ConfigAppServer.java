package rocks.theatomicoption.cropbiomelimiter.configapp;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class ConfigAppServer {
	private static final int DEFAULT_PORT = 41731;
	private static final int MAX_PORT_ATTEMPTS = 100;
	private static final int MAX_CONFIG_BYTES = 4 * 1024 * 1024;
	private static final Map<String, String> CONFIG_FILES = Map.of(
			"general", "general.json",
			"explicit", "explicit-mode.json",
			"threshold", "threshold-mode.json",
			"snapshot", "cropbiomelimiter-registry-snapshot.json"
	);
	private static final List<String> WRITABLE_CONFIG_KEYS = List.of("general", "explicit", "threshold");

	private ConfigAppServer() {
	}

	public static void main(String[] args) throws Exception {
		Path appDirectory = args.length > 0 ? Path.of(args[0]) : Path.of(".");
		RunningServer server = start(appDirectory, DEFAULT_PORT, true);
		System.out.println();
		System.out.println("Crop Biome Limiter Config App is ready.");
		System.out.println("Opening " + server.uri());
		System.out.println("Keep this window open while using the app. Close it when you are done.");
		System.out.println();
		Thread.currentThread().join();
	}

	public static RunningServer start(Path appDirectory, int preferredPort, boolean openBrowser) throws IOException {
		Path normalizedAppDirectory = appDirectory.toAbsolutePath().normalize();
		Path configDirectory = Optional.ofNullable(normalizedAppDirectory.getParent()).orElse(normalizedAppDirectory);
		if (!Files.isDirectory(normalizedAppDirectory)) {
			throw new IOException("Config app directory does not exist: " + normalizedAppDirectory);
		}
		int port = freePort(preferredPort);
		HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
		RunningServer runningServer = new RunningServer(server, normalizedAppDirectory, configDirectory, URI.create("http://127.0.0.1:" + port + "/index.html"));
		server.createContext("/api/status", runningServer::handleStatus);
		server.createContext("/api/files", runningServer::handleConfigFile);
		server.createContext("/", runningServer::handleStaticFile);
		server.setExecutor(Executors.newCachedThreadPool(runnable -> {
			Thread thread = new Thread(runnable, "cropbiomelimiter-config-app");
			thread.setDaemon(true);
			return thread;
		}));
		server.start();
		if (openBrowser) {
			runningServer.tryOpenBrowser();
		}
		return runningServer;
	}

	private static int freePort(int preferredPort) throws IOException {
		for (int port = preferredPort; port < preferredPort + MAX_PORT_ATTEMPTS; port++) {
			try (java.net.ServerSocket socket = new java.net.ServerSocket()) {
				socket.setReuseAddress(false);
				socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
				return port;
			} catch (IOException ignored) {
			}
		}
		throw new IOException("No available local port was found.");
	}

	public static final class RunningServer implements AutoCloseable {
		private final HttpServer server;
		private final Path appDirectory;
		private final Path configDirectory;
		private final URI uri;

		private RunningServer(HttpServer server, Path appDirectory, Path configDirectory, URI uri) {
			this.server = server;
			this.appDirectory = appDirectory;
			this.configDirectory = configDirectory;
			this.uri = uri;
		}

		public URI uri() {
			return uri;
		}

		public Path configDirectory() {
			return configDirectory;
		}

		@Override
		public void close() {
			server.stop(0);
		}

		private void tryOpenBrowser() {
			try {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					Desktop.getDesktop().browse(uri);
				}
			} catch (IOException | RuntimeException exception) {
				System.err.println("Could not open the browser automatically. Open this URL manually: " + uri);
			}
		}

		private void handleStatus(HttpExchange exchange) throws IOException {
			try {
				if (!methodIs(exchange, "GET")) {
					sendText(exchange, 405, "Method Not Allowed", "Only GET requests are supported.");
					return;
				}
				String body = "{"
						+ "\"appDirectory\":\"" + jsonEscape(appDirectory.toString()) + "\","
						+ "\"configDirectory\":\"" + jsonEscape(configDirectory.toString()) + "\","
						+ "\"files\":[\"general\",\"explicit\",\"threshold\",\"snapshot\"]"
						+ "}";
				sendBytes(exchange, 200, "OK", body.getBytes(StandardCharsets.UTF_8), "application/json; charset=utf-8");
			} catch (RuntimeException exception) {
				sendText(exchange, 500, "Internal Server Error", "Could not read helper status.");
			}
		}

		private void handleConfigFile(HttpExchange exchange) throws IOException {
			try {
				String key = configKey(exchange.getRequestURI().getPath());
				if (!CONFIG_FILES.containsKey(key)) {
					sendText(exchange, 404, "Not Found", "Unknown config file.");
					return;
				}

				if (methodIs(exchange, "GET")) {
					readConfigFile(exchange, key);
					return;
				}

				if (methodIs(exchange, "PUT")) {
					writeConfigFile(exchange, key);
					return;
				}

				sendText(exchange, 405, "Method Not Allowed", "Only GET and PUT requests are supported.");
			} catch (RuntimeException exception) {
				sendText(exchange, 500, "Internal Server Error", "Could not handle config file request.");
			}
		}

		private void readConfigFile(HttpExchange exchange, String key) throws IOException {
			Path path = configPath(key);
			if (!Files.exists(path)) {
				sendText(exchange, 404, "Not Found", "Config file does not exist.");
				return;
			}
			byte[] body = Files.readAllBytes(path);
			sendBytes(exchange, 200, "OK", body, "application/json; charset=utf-8");
		}

		private void writeConfigFile(HttpExchange exchange, String key) throws IOException {
			if (!WRITABLE_CONFIG_KEYS.contains(key)) {
				sendText(exchange, 403, "Forbidden", "This config file is read only.");
				return;
			}

			byte[] body = exchange.getRequestBody().readNBytes(MAX_CONFIG_BYTES + 1);
			if (body.length > MAX_CONFIG_BYTES) {
				sendText(exchange, 413, "Payload Too Large", "Config file is too large.");
				return;
			}

			Files.createDirectories(configDirectory);
			Path target = configPath(key);
			Path temp = Files.createTempFile(configDirectory, CONFIG_FILES.get(key), ".tmp");
			try {
				Files.write(temp, body, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				try {
					Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
				} catch (IOException exception) {
					Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
				}
			} finally {
				Files.deleteIfExists(temp);
			}
			sendText(exchange, 200, "OK", "{\"saved\":true}");
		}

		private Path configPath(String key) {
			return configDirectory.resolve(CONFIG_FILES.get(key)).normalize();
		}

		private String configKey(String path) {
			String prefix = "/api/files/";
			if (!path.startsWith(prefix)) {
				return "";
			}
			return path.substring(prefix.length()).toLowerCase(Locale.ROOT);
		}

		private void handleStaticFile(HttpExchange exchange) throws IOException {
			try {
				if (!methodIs(exchange, "GET") && !methodIs(exchange, "HEAD")) {
					sendText(exchange, 405, "Method Not Allowed", "Only GET requests are supported.");
					return;
				}

				Path target = staticPath(exchange.getRequestURI().getPath());
				if (target == null || !Files.isRegularFile(target)) {
					sendText(exchange, 404, "Not Found", "Not found.");
					return;
				}

				byte[] body = Files.readAllBytes(target);
				sendBytes(exchange, 200, "OK", methodIs(exchange, "HEAD") ? new byte[0] : body, contentType(target));
			} catch (RuntimeException exception) {
				sendText(exchange, 500, "Internal Server Error", "Could not serve config app file.");
			}
		}

		private Path staticPath(String requestPath) {
			String path = requestPath == null || requestPath.isBlank() || requestPath.equals("/") ? "/index.html" : requestPath;
			String decoded = URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8);
			Path target = appDirectory.resolve(decoded.replace('/', java.io.File.separatorChar)).normalize();
			return target.startsWith(appDirectory) ? target : null;
		}

		private static boolean methodIs(HttpExchange exchange, String method) {
			return method.equalsIgnoreCase(exchange.getRequestMethod());
		}

		private static String contentType(Path path) {
			String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
			if (name.endsWith(".html")) {
				return "text/html; charset=utf-8";
			}
			if (name.endsWith(".css")) {
				return "text/css; charset=utf-8";
			}
			if (name.endsWith(".js")) {
				return "text/javascript; charset=utf-8";
			}
			if (name.endsWith(".json")) {
				return "application/json; charset=utf-8";
			}
			if (name.endsWith(".cmd") || name.endsWith(".sh") || name.endsWith(".command")) {
				return "text/plain; charset=utf-8";
			}
			return "application/octet-stream";
		}

		private static void sendText(HttpExchange exchange, int statusCode, String statusText, String body) throws IOException {
			sendBytes(exchange, statusCode, statusText, body.getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
		}

		private static void sendBytes(HttpExchange exchange, int statusCode, String statusText, byte[] body, String contentType) throws IOException {
			exchange.getResponseHeaders().set("Content-Type", contentType);
			exchange.getResponseHeaders().set("Cache-Control", "no-store");
			exchange.sendResponseHeaders(statusCode, body.length);
			try (OutputStream output = exchange.getResponseBody()) {
				output.write(body);
			}
		}

		private static String jsonEscape(String value) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < value.length(); i++) {
				char character = value.charAt(i);
				switch (character) {
					case '"' -> builder.append("\\\"");
					case '\\' -> builder.append("\\\\");
					case '\b' -> builder.append("\\b");
					case '\f' -> builder.append("\\f");
					case '\n' -> builder.append("\\n");
					case '\r' -> builder.append("\\r");
					case '\t' -> builder.append("\\t");
					default -> {
						if (character < 0x20) {
							builder.append(String.format("\\u%04x", (int) character));
						} else {
							builder.append(character);
						}
					}
				}
			}
			return builder.toString();
		}
	}
}
