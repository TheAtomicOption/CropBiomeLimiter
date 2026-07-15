package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.CropBiomeLimiterConfig;
import rocks.theatomicoption.cropbiomelimiter.config.ExplicitModeRules;
import rocks.theatomicoption.cropbiomelimiter.config.GeneralOptions;
import rocks.theatomicoption.cropbiomelimiter.config.PrecipitationRequirement;
import rocks.theatomicoption.cropbiomelimiter.config.RuleMode;
import rocks.theatomicoption.cropbiomelimiter.config.ThresholdCropRule;
import rocks.theatomicoption.cropbiomelimiter.config.ThresholdModeRules;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasComponents;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.TemperatureScale;

public final class ViewerAtlasSmokeTest {
	private ViewerAtlasSmokeTest() {
	}

	public static void run() {
		mixedModesBuildBothLookupDirections();
		customDimensionsUseTheLocalBiomeRegistry();
		largeGroupsCreateStaticPages();
		temperatureScaleSpansTheMockupPalette();
		biomeLoreUsesSemanticMinecraftColors();
	}

	private static void temperatureScaleSpansTheMockupPalette() {
		assertEquals(TemperatureScale.COLD_COLOR, TemperatureScale.colorAt(0.0F), "temperature scale should begin cold");
		assertEquals(TemperatureScale.TEMPERATE_COLOR, TemperatureScale.colorAt(0.45F), "temperature scale should pass through temperate green");
		assertEquals(TemperatureScale.HOT_COLOR, TemperatureScale.colorAt(1.0F), "temperature scale should end hot");
		assertEquals(TemperatureScale.COLD_COLOR, TemperatureScale.colorAt(-1.0F), "temperature scale should clamp below its range");
		assertEquals(TemperatureScale.HOT_COLOR, TemperatureScale.colorAt(2.0F), "temperature scale should clamp above its range");

		TemperatureDomain domain = new TemperatureDomain(-0.5F, 2.0F);
		assertEquals(TemperatureScale.COLD_COLOR, TemperatureScale.colorAtTemperature(-0.5F, domain), "loaded minimum should use the cold endpoint");
		assertEquals(TemperatureScale.TEMPERATE_COLOR, TemperatureScale.colorAtTemperature(0.625F, domain), "loaded climate midpoint should use temperate green");
		assertEquals(TemperatureScale.HOT_COLOR, TemperatureScale.colorAtTemperature(2.0F, domain), "loaded maximum should use the hot endpoint");
	}

	private static void customDimensionsUseTheLocalBiomeRegistry() {
		ResourceKey<Level> moon = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("example:moon"));
		ResourceLocation wheat = ResourceLocation.parse("minecraft:wheat");
		ExplicitModeRules fallback = new ExplicitModeRules(CropBehavior.GROWABLE, Map.of());
		CropBiomeLimiterConfig config = new CropBiomeLimiterConfig(
				GeneralOptions.defaults(),
				fallback,
				Map.of()
		);
		AtlasBiome overworldBiome = new AtlasBiome(
				ResourceLocation.parse("minecraft:plains"),
				Holder.direct(biome(-2.0F, true)),
				Set.of(Level.OVERWORLD)
		);
		AtlasBiome netherBiome = new AtlasBiome(
				ResourceLocation.parse("minecraft:basalt_deltas"),
				Holder.direct(biome(4.0F, false)),
				Set.of(Level.NETHER)
		);

		CropBiomeAtlas atlas = CropBiomeAtlas.build(
				config,
				Set.of(moon),
				List.of(wheat),
				List.of(overworldBiome, netherBiome)
		);

		assertEquals(1, atlas.cropEntries().size(), "a live custom dimension should create a crop page");
		assertEquals(2, atlas.biomeEntries().size(), "a custom dimension should expose every locally registered biome");
		assertEquals(moon, atlas.cropEntries().getFirst().dimension(), "custom dimension identity should be retained");
		assertEquals(new TemperatureDomain(-2.0F, 4.0F), atlas.cropEntries().getFirst().temperatureDomain(), "modded temperatures should define the palette domain");
	}

	private static void biomeLoreUsesSemanticMinecraftColors() {
		AtlasBiome basaltDeltas = new AtlasBiome(
				ResourceLocation.parse("minecraft:basalt_deltas"),
				Holder.direct(biome(2.0F, false)),
				Set.of(Level.NETHER)
		);
		List<Component> lore = AtlasComponents.biomeLore(
				basaltDeltas,
				new TemperatureDomain(-0.7F, 2.0F)
		);
		assertEquals(ChatFormatting.DARK_GRAY.getColor(), lore.get(0).getStyle().getColor().getValue(), "biome identifier should be dark gray");
		assertEquals(ChatFormatting.GOLD.getColor(), lore.get(1).getStyle().getColor().getValue(), "hot biome temperature should be amber");
		assertEquals(ChatFormatting.GRAY.getColor(), lore.get(2).getStyle().getColor().getValue(), "dry biome precipitation should be gray");
		assertTrue(lore.stream().noneMatch(line -> line.getStyle().isItalic()), "added biome lore should use regular text");
	}

	private static void mixedModesBuildBothLookupDirections() {
		ResourceLocation wheat = ResourceLocation.parse("minecraft:wheat");
		ResourceLocation carrots = ResourceLocation.parse("minecraft:carrots");
		ClimateRule hotDry = new ClimateRule(
				1.5F,
				3.0F,
				PrecipitationRequirement.FORBIDDEN,
				CropBehavior.GROWABLE
		);
		ExplicitModeRules overworld = new ExplicitModeRules(CropBehavior.GROWABLE, Map.of());
		ThresholdModeRules nether = new ThresholdModeRules(
				new ThresholdCropRule(CropBehavior.BONEMEAL_REQUIRED, List.of()),
				Map.of(wheat, new ThresholdCropRule(CropBehavior.BONEMEAL_REQUIRED, List.of(hotDry)))
		);
		CropBiomeLimiterConfig config = new CropBiomeLimiterConfig(
				GeneralOptions.defaults(),
				overworld,
				Map.of(Level.OVERWORLD, overworld, Level.NETHER, nether)
		);
		AtlasBiome plains = new AtlasBiome(
				ResourceLocation.parse("minecraft:plains"),
				Holder.direct(biome(0.8F, true)),
				Set.of(Level.OVERWORLD)
		);
		AtlasBiome wastes = new AtlasBiome(
				ResourceLocation.parse("minecraft:nether_wastes"),
				Holder.direct(biome(2.0F, false)),
				Set.of(Level.NETHER)
		);

		CropBiomeAtlas atlas = CropBiomeAtlas.build(
				config,
				Set.of(Level.OVERWORLD, Level.NETHER),
				List.of(wheat, carrots),
				List.of(plains, wastes)
		);

		assertEquals(4, atlas.cropEntries().size(), "atlas should create one crop record per crop and dimension");
		assertEquals(2, atlas.biomeEntries().size(), "atlas should create one biome record per biome and matching dimension");

		CropAtlasEntry explicitWheat = cropEntry(atlas, Level.OVERWORLD, wheat);
		assertEquals(RuleMode.EXPLICIT, explicitWheat.mode(), "Explicit dimensions should remain mode-aware");
		assertTrue(explicitWheat.climateRules().isEmpty(), "Explicit records should omit climate rules");
		assertEquals(List.of(plains), explicitWheat.biomesByBehavior().get(CropBehavior.GROWABLE), "Explicit results should be grouped by behavior");

		CropAtlasEntry thresholdWheat = cropEntry(atlas, Level.NETHER, wheat);
		assertEquals(RuleMode.THRESHOLD, thresholdWheat.mode(), "Threshold dimensions should remain mode-aware");
		assertEquals(List.of(hotDry), thresholdWheat.climateRules(), "Threshold records should retain their climate rules");
		assertEquals(List.of(wastes), thresholdWheat.biomesByBehavior().get(CropBehavior.GROWABLE), "Threshold results should evaluate biome climate");

		BiomeAtlasEntry netherBiome = atlas.biomeEntries().stream()
				.filter(entry -> entry.dimension().equals(Level.NETHER))
				.findFirst()
				.orElseThrow();
		assertEquals(List.of(wheat), netherBiome.cropsByBehavior().get(CropBehavior.GROWABLE), "biome lookup should contain growable crops");
		assertEquals(List.of(carrots), netherBiome.cropsByBehavior().get(CropBehavior.BONEMEAL_REQUIRED), "biome lookup should contain fallback crops");
	}

	private static void largeGroupsCreateStaticPages() {
		List<Integer> values = IntStream.range(0, 1_000).boxed().toList();
		EnumMap<CropBehavior, List<Integer>> groups = new EnumMap<>(CropBehavior.class);
		groups.put(CropBehavior.GROWABLE, values);
		groups.put(CropBehavior.BONEMEAL_REQUIRED, List.of(1, 2, 3, 4, 5));
		groups.put(CropBehavior.UNPLANTABLE, List.of());

		List<AtlasResultPage<Integer>> pages = AtlasResultPage.paginate(groups);
		assertEquals(167, pages.size(), "large groups should create enough native viewer pages");
		assertTrue(pages.stream().allMatch(page -> page.valuesByBehavior().values().stream()
				.allMatch(pageValues -> pageValues.size() <= AtlasResultPage.SLOTS_PER_GROUP)), "every visible slot page should contain at most six static values");
		assertEquals(values, pages.stream()
				.flatMap(page -> page.valuesByBehavior().get(CropBehavior.GROWABLE).stream())
				.toList(), "static pages should retain every result in order");
		assertEquals(1_000, pages.getFirst().total(CropBehavior.GROWABLE), "page headings should keep the full group count");
	}

	private static CropAtlasEntry cropEntry(CropBiomeAtlas atlas, net.minecraft.resources.ResourceKey<Level> dimension, ResourceLocation crop) {
		return atlas.cropEntries().stream()
				.filter(entry -> entry.dimension().equals(dimension) && entry.cropId().equals(crop))
				.findFirst()
				.orElseThrow();
	}

	private static Biome biome(float temperature, boolean hasPrecipitation) {
		return new Biome.BiomeBuilder()
				.hasPrecipitation(hasPrecipitation)
				.temperature(temperature)
				.downfall(hasPrecipitation ? 1.0F : 0.0F)
				.specialEffects(new BiomeSpecialEffects.Builder()
						.waterColor(0)
						.waterFogColor(0)
						.fogColor(0)
						.skyColor(0)
						.build())
				.mobSpawnSettings(MobSpawnSettings.EMPTY)
				.generationSettings(BiomeGenerationSettings.EMPTY)
				.build();
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
