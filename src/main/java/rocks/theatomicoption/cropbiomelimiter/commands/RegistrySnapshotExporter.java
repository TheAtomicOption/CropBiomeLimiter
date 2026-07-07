package rocks.theatomicoption.cropbiomelimiter.commands;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.config.ConfigAppSchema;
import rocks.theatomicoption.cropbiomelimiter.logic.GrowableBlockClassifier;

public final class RegistrySnapshotExporter {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private RegistrySnapshotExporter() {
	}

	public static ExportResult export(MinecraftServer server) throws IOException {
		Path output = server.getWorldPath(LevelResource.ROOT).resolve(ConfigAppSchema.REGISTRY_SNAPSHOT_FILE_NAME);
		Files.createDirectories(output.getParent());
		Snapshot snapshot = createSnapshot(server);

		try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
			GSON.toJson(snapshot.root(), writer);
		}

		return new ExportResult(output, snapshot.dimensionCount(), snapshot.biomeCount(), snapshot.cropCount());
	}

	private static Snapshot createSnapshot(MinecraftServer server) {
		JsonArray dimensions = dimensions(server);
		JsonArray biomes = biomes(server);
		JsonArray crops = crops();
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", ConfigAppSchema.SCHEMA_VERSION);
		root.addProperty("generated_by", CropBiomeLimiter.MOD_ID);
		root.addProperty("generated_at", Instant.now().toString());
		root.add("config_schema", ConfigAppSchema.metadataJson());
		root.add("modes", ConfigAppSchema.modeNamesJson());
		root.add("behaviors", ConfigAppSchema.behaviorNamesJson());
		root.add("precipitation_requirements", ConfigAppSchema.precipitationRequirementNamesJson());
		root.add("counts", counts(dimensions, biomes, crops));
		root.add("dimensions", dimensions);
		root.add("biomes", biomes);
		root.add("crops", crops);
		return new Snapshot(root, dimensions.size(), biomes.size(), crops.size());
	}

	private static JsonObject counts(JsonArray dimensions, JsonArray biomes, JsonArray crops) {
		JsonObject counts = new JsonObject();
		counts.addProperty("dimensions", dimensions.size());
		counts.addProperty("biomes", biomes.size());
		counts.addProperty("crops", crops.size());
		return counts;
	}

	private static JsonArray dimensions(MinecraftServer server) {
		JsonArray dimensions = new JsonArray();
		server.levelKeys().stream()
				.map(ResourceKey::identifier)
				.map(Object::toString)
				.sorted()
				.forEach(dimensions::add);
		return dimensions;
	}

	private static JsonArray biomes(MinecraftServer server) {
		JsonArray biomes = new JsonArray();
		server.registryAccess().lookup(Registries.BIOME).ifPresent(registry -> addBiomes(registry, biomes));
		return biomes;
	}

	private static void addBiomes(Registry<Biome> registry, JsonArray biomes) {
		registry.entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> biomes.add(biome(entry)));
	}

	private static JsonObject biome(Map.Entry<ResourceKey<Biome>, Biome> entry) {
		Biome biome = entry.getValue();
		JsonObject value = new JsonObject();
		value.addProperty("id", entry.getKey().identifier().toString());
		value.addProperty("temperature", biome.getBaseTemperature());
		value.addProperty("has_precipitation", biome.hasPrecipitation());
		return value;
	}

	private static JsonArray crops() {
		JsonArray crops = new JsonArray();
		GrowableBlockClassifier classifier = new GrowableBlockClassifier();
		BuiltInRegistries.BLOCK.entrySet().stream()
				.filter(entry -> isTrackedGrowable(classifier, entry.getValue()))
				.sorted(Comparator.comparing(entry -> entry.getKey().identifier().toString()))
				.forEach(entry -> crops.add(entry.getKey().identifier().toString()));
		return crops;
	}

	private static boolean isTrackedGrowable(GrowableBlockClassifier classifier, Block block) {
		try {
			return classifier.isTrackedGrowable(block.defaultBlockState());
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Skipping registry snapshot crop candidate because block inspection failed: {}", block, exception);
			return false;
		}
	}

	public record ExportResult(Path path, int dimensionCount, int biomeCount, int cropCount) {
	}

	private record Snapshot(JsonObject root, int dimensionCount, int biomeCount, int cropCount) {
	}
}
