package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rocks.theatomicoption.cropbiomelimiter.logic.CropDecisionService;
import rocks.theatomicoption.cropbiomelimiter.logic.GrowableBlockClassifier;

public final class CoreBehaviorSmokeTest {
	private CoreBehaviorSmokeTest() {
	}

	public static void run() {
		classifierRecognizesGrowablesAndIgnoresUnsafeInputs();
		cropBehaviorsControlActionSurfaces();
		explicitModeUsesBiomeCells();
		thresholdModeUsesTemperatureAndPrecipitation();
		decisionServiceAppliesConfiguredBehaviorToActions();
		defaultThresholdConfigAllowsAndWarnsWrongClimateCactusPlacement();
		decisionServiceWithersOnlyPlantingAllowedNaturalGrowthDenials();
		creativeModeBypassesPlayerActionRestrictions();
		decisionServiceHonorsTogglesExclusionsAndFallbacks();
	}

	private static void classifierRecognizesGrowablesAndIgnoresUnsafeInputs() {
		GrowableBlockClassifier classifier = new GrowableBlockClassifier();
		assertTrue(!classifier.isTrackedGrowable(null), "classifier should ignore null block states");
		assertTrue(!classifier.isTrackedGrowable(Blocks.STONE.defaultBlockState()), "classifier should ignore ordinary non-growable blocks");
		assertTrue(classifier.isTrackedGrowable(Blocks.WHEAT.defaultBlockState()), "classifier should track crop blocks");
		assertTrue(classifier.isTrackedGrowable(Blocks.OAK_SAPLING.defaultBlockState()), "classifier should track saplings");
		assertTrue(classifier.isTrackedGrowable(Blocks.BROWN_MUSHROOM.defaultBlockState()), "classifier should track mushrooms");
		assertTrue(!classifier.isTrackedGrowable(Blocks.DEAD_BUSH.defaultBlockState()), "classifier should not track dead bush");
		for (Block block : GrowableBlockClassifier.defaultGrowableBlocks()) {
			assertTrue(classifier.isTrackedGrowable(block.defaultBlockState()), "classifier should track default growable " + block);
		}
	}

	private static void cropBehaviorsControlActionSurfaces() {
		assertTrue(CropBehavior.GROWABLE.allowsPlanting(), "growable should allow planting");
		assertTrue(CropBehavior.GROWABLE.allowsNaturalGrowth(), "growable should allow natural growth");
		assertTrue(CropBehavior.GROWABLE.allowsBonemeal(), "growable should allow bone meal");

		assertTrue(CropBehavior.BONEMEAL_REQUIRED.allowsPlanting(), "bonemeal-required should allow planting");
		assertTrue(!CropBehavior.BONEMEAL_REQUIRED.allowsNaturalGrowth(), "bonemeal-required should deny natural growth");
		assertTrue(CropBehavior.BONEMEAL_REQUIRED.allowsBonemeal(), "bonemeal-required should allow bone meal");

		assertTrue(!CropBehavior.UNPLANTABLE.allowsPlanting(), "unplantable should deny planting");
		assertTrue(!CropBehavior.UNPLANTABLE.allowsNaturalGrowth(), "unplantable should deny natural growth");
		assertTrue(!CropBehavior.UNPLANTABLE.allowsBonemeal(), "unplantable should deny bone meal");
	}

	private static void explicitModeUsesBiomeCells() {
		Identifier wheat = id("minecraft:wheat");
		Identifier desert = id("minecraft:desert");
		Identifier snowyPlains = id("minecraft:snowy_plains");
		ExplicitModeRules rules = new ExplicitModeRules(
				CropBehavior.GROWABLE,
				Map.of(wheat, Map.of(
						desert, CropBehavior.UNPLANTABLE,
						snowyPlains, CropBehavior.BONEMEAL_REQUIRED
				))
		);

		assertEquals(CropBehavior.UNPLANTABLE, rules.resolve(wheat, biomeHolder(desert)), "Explicit mode should resolve configured biome/crop cells");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, rules.resolve(wheat, biomeHolder(snowyPlains)), "Explicit mode should support bonemeal-required cells");
		assertEquals(CropBehavior.GROWABLE, rules.resolve(wheat, biomeHolder(id("minecraft:plains"))), "Explicit mode should fall back for unconfigured biomes");
		assertEquals(CropBehavior.GROWABLE, rules.resolve(id("minecraft:carrots"), biomeHolder(desert)), "Explicit mode should fall back for unconfigured crops");
	}

	private static void thresholdModeUsesTemperatureAndPrecipitation() {
		Identifier wheat = id("minecraft:wheat");
		ThresholdCropRule wheatRule = new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						new ClimateRule(0.15F, 1.50F, PrecipitationRequirement.REQUIRED, CropBehavior.GROWABLE),
						new ClimateRule(1.50F, 3.00F, PrecipitationRequirement.FORBIDDEN, CropBehavior.UNPLANTABLE)
				)
		);
		ThresholdModeRules rules = new ThresholdModeRules(
				new ThresholdCropRule(CropBehavior.GROWABLE, List.of()),
				Map.of(wheat, wheatRule)
		);

		assertEquals(CropBehavior.GROWABLE, rules.resolve(wheat, Holder.direct(biome(0.50F, true))), "Threshold mode should match warm wet rules");
		assertEquals(CropBehavior.BONEMEAL_REQUIRED, rules.resolve(wheat, Holder.direct(biome(0.50F, false))), "Threshold mode should use crop default when precipitation does not match");
		assertEquals(CropBehavior.UNPLANTABLE, rules.resolve(wheat, Holder.direct(biome(2.00F, false))), "Threshold mode should match hot dry rules");
		assertEquals(CropBehavior.GROWABLE, rules.resolve(id("minecraft:carrots"), Holder.direct(biome(2.00F, false))), "Threshold mode should use dimension default for unconfigured crops");
	}

	private static void decisionServiceAppliesConfiguredBehaviorToActions() {
		Identifier wheat = id("minecraft:wheat");
		Identifier desert = id("minecraft:desert");
		Identifier snowyPlains = id("minecraft:snowy_plains");
		Identifier plains = id("minecraft:plains");
		ExplicitModeRules rules = new ExplicitModeRules(
				CropBehavior.GROWABLE,
				Map.of(wheat, Map.of(
						desert, CropBehavior.BONEMEAL_REQUIRED,
						snowyPlains, CropBehavior.UNPLANTABLE
				))
		);
		CropDecisionService service = decisionService(GeneralOptions.defaults(), rules);

		assertTrue(service.canPlace(Level.OVERWORLD, biomeHolder(desert), Blocks.WHEAT), "bonemeal-required should allow planting");
		assertTrue(!service.canGrowNaturally(Level.OVERWORLD, biomeHolder(desert), Blocks.WHEAT.defaultBlockState()), "bonemeal-required should deny natural growth");
		assertTrue(service.canUseBonemeal(Level.OVERWORLD, biomeHolder(desert), Blocks.WHEAT.defaultBlockState()), "bonemeal-required should allow bone meal");

		assertTrue(!service.canPlace(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.WHEAT), "unplantable should deny planting");
		assertTrue(!service.canPlace(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.WHEAT.defaultBlockState()), "unplantable should deny placement state");
		assertTrue(!service.canGrowNaturally(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.WHEAT.defaultBlockState()), "unplantable should deny natural growth");
		assertTrue(!service.canUseBonemeal(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.WHEAT.defaultBlockState()), "unplantable should deny bone meal");

		assertTrue(service.canPlace(Level.OVERWORLD, biomeHolder(plains), Blocks.WHEAT), "default growable behavior should allow planting");
		assertTrue(service.canGrowNaturally(Level.OVERWORLD, biomeHolder(plains), Blocks.WHEAT.defaultBlockState()), "default growable behavior should allow natural growth");
		assertTrue(service.canUseBonemeal(Level.OVERWORLD, biomeHolder(plains), Blocks.WHEAT.defaultBlockState()), "default growable behavior should allow bone meal");
	}

	private static void defaultThresholdConfigAllowsAndWarnsWrongClimateCactusPlacement() {
		CropDecisionService service = new CropDecisionService(DefaultCropBiomeConfig.create(), new GrowableBlockClassifier());
		Holder<Biome> hotDry = Holder.direct(biome(2.00F, false));
		Holder<Biome> coldWet = Holder.direct(biome(0.00F, true));

		assertTrue(service.canPlace(Level.OVERWORLD, hotDry, Blocks.CACTUS.defaultBlockState()), "default Threshold mode should allow cactus placement in hot dry biomes");
		assertTrue(!service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, hotDry, Blocks.CACTUS.defaultBlockState()), "default Threshold mode should not warn for cactus in hot dry biomes");
		assertTrue(service.canPlace(Level.OVERWORLD, coldWet, Blocks.CACTUS.defaultBlockState()), "default Threshold mode should allow wrong-climate cactus placement because the default behavior is bonemeal-required");
		assertTrue(service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, coldWet, Blocks.CACTUS.defaultBlockState()), "default Threshold mode should warn when wrong-climate cactus placement is allowed but natural growth is denied");
		assertTrue(service.shouldWitherOnSuccessfulNaturalGrowth(Level.OVERWORLD, coldWet, Blocks.CACTUS.defaultBlockState()), "wrong-climate cactus should wither if a denied natural growth tick succeeds");
	}

	private static void decisionServiceWithersOnlyPlantingAllowedNaturalGrowthDenials() {
		Identifier wheat = id("minecraft:wheat");
		Identifier desert = id("minecraft:desert");
		Identifier snowyPlains = id("minecraft:snowy_plains");
		Identifier plains = id("minecraft:plains");
		ExplicitModeRules rules = new ExplicitModeRules(
				CropBehavior.GROWABLE,
				Map.of(wheat, Map.of(
						desert, CropBehavior.BONEMEAL_REQUIRED,
						snowyPlains, CropBehavior.UNPLANTABLE
				))
		);
		CropDecisionService service = decisionService(GeneralOptions.defaults(), rules);

		assertTrue(service.shouldWitherOnSuccessfulNaturalGrowth(Level.OVERWORLD, biomeHolder(desert), Blocks.WHEAT.defaultBlockState()), "bonemeal-required crops should wither when a denied natural growth tick succeeds");
		assertTrue(service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, biomeHolder(desert), Blocks.WHEAT.defaultBlockState()), "bonemeal-required placement should warn the player");
		assertTrue(!service.shouldWitherOnSuccessfulNaturalGrowth(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.WHEAT.defaultBlockState()), "unplantable crops should remain placement-blocked rather than using the wither path");
		assertTrue(!service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.WHEAT.defaultBlockState()), "unplantable crops should use the blocking warning path instead of allowed-placement warnings");
		assertTrue(!service.shouldWitherOnSuccessfulNaturalGrowth(Level.OVERWORLD, biomeHolder(plains), Blocks.WHEAT.defaultBlockState()), "growable crops should not wither");
		assertTrue(!service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, biomeHolder(plains), Blocks.WHEAT.defaultBlockState()), "growable crops should not warn on placement");
		assertTrue(!service.shouldWitherOnSuccessfulNaturalGrowth(Level.OVERWORLD, biomeHolder(desert), Blocks.DEAD_BUSH.defaultBlockState()), "dead bush should not be controlled by natural growth rules");
		assertTrue(!service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, biomeHolder(desert), Blocks.DEAD_BUSH.defaultBlockState()), "dead bush placement should not warn");
		assertTrue(service.canPlace(Level.OVERWORLD, biomeHolder(snowyPlains), Blocks.DEAD_BUSH.defaultBlockState()), "dead bush placement should not be blocked by the mod");
	}

	private static void creativeModeBypassesPlayerActionRestrictions() {
		Identifier wheat = id("minecraft:wheat");
		Holder<Biome> desert = biomeHolder(id("minecraft:desert"));
		ExplicitModeRules unplantableRules = new ExplicitModeRules(CropBehavior.UNPLANTABLE, Map.of(wheat, Map.of()));
		CropDecisionService service = decisionService(GeneralOptions.defaults(), unplantableRules);

		assertTrue(!service.canPlace(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "survival placement should obey unplantable rules");
		assertTrue(!service.canUseBonemeal(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "survival bone meal should obey unplantable rules");
		assertTrue(service.canPlace(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState(), true), "creative placement should bypass crop restrictions");
		assertTrue(service.canUseBonemeal(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState(), true), "creative bone meal should bypass crop restrictions");
		assertTrue(!service.shouldWarnOnAllowedPlacement(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "unplantable creative bypass should not create allowed-placement warnings");
	}

	private static void decisionServiceHonorsTogglesExclusionsAndFallbacks() {
		Identifier wheat = id("minecraft:wheat");
		Holder<Biome> desert = biomeHolder(id("minecraft:desert"));
		ExplicitModeRules unplantableRules = new ExplicitModeRules(CropBehavior.UNPLANTABLE, Map.of(wheat, Map.of()));

		CropDecisionService toggledService = decisionService(new GeneralOptions(false, false, true, Set.of()), unplantableRules);
		assertTrue(toggledService.canPlace(Level.OVERWORLD, desert, Blocks.WHEAT), "disabled placement enforcement should allow planting");
		assertTrue(toggledService.canUseBonemeal(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "disabled bone meal enforcement should allow bone meal");
		assertTrue(!toggledService.canGrowNaturally(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "natural growth should still follow crop behavior");

		CropDecisionService excludedService = decisionService(new GeneralOptions(true, true, true, Set.of(wheat)), unplantableRules);
		assertTrue(excludedService.canPlace(Level.OVERWORLD, desert, Blocks.WHEAT), "excluded crops should allow planting");
		assertTrue(excludedService.canGrowNaturally(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "excluded crops should allow natural growth");
		assertTrue(excludedService.canUseBonemeal(Level.OVERWORLD, desert, Blocks.WHEAT.defaultBlockState()), "excluded crops should allow bone meal");

		assertTrue(excludedService.canPlace(Level.OVERWORLD, desert, Blocks.STONE), "untracked blocks should be ignored for placement");
		assertTrue(excludedService.canGrowNaturally(Level.OVERWORLD, desert, Blocks.STONE.defaultBlockState()), "untracked blocks should be ignored for natural growth");
		assertTrue(excludedService.canPlace(null, desert, Blocks.WHEAT), "missing dimension should fail open");
		assertTrue(excludedService.canUseBonemeal(Level.OVERWORLD, null, Blocks.WHEAT.defaultBlockState()), "missing biome should fail open");
		assertTrue(CropDecisionService.allowAll().canPlace(null, null, (Block) null), "allow-all service should fail open");
	}

	private static Identifier id(String value) {
		return Identifier.parse(value);
	}

	private static Holder<Biome> biomeHolder(Identifier biomeId) {
		ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, biomeId);
		return Holder.Reference.createStandAlone(null, key);
	}

	private static Biome biome(float temperature, boolean hasPrecipitation) {
		return new Biome.BiomeBuilder()
				.hasPrecipitation(hasPrecipitation)
				.temperature(temperature)
				.downfall(hasPrecipitation ? 1.0F : 0.0F)
				.specialEffects(new BiomeSpecialEffects.Builder()
						.waterColor(0)
						.build())
				.mobSpawnSettings(MobSpawnSettings.EMPTY)
				.generationSettings(BiomeGenerationSettings.EMPTY)
				.build();
	}

	private static CropDecisionService decisionService(GeneralOptions options, DimensionRules rules) {
		return new CropDecisionService(
				new CropBiomeLimiterConfig(options, rules, Map.of(Level.OVERWORLD, rules)),
				new GrowableBlockClassifier()
		);
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
