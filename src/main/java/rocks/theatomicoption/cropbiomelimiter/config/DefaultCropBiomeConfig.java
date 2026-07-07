package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class DefaultCropBiomeConfig {
	private static final float VERY_COLD = -1.0F;
	private static final float FREEZING = 0.15F;
	private static final float COOL = 0.5F;
	private static final float TEMPERATE = 0.8F;
	private static final float TROPICAL = 0.95F;
	private static final float HOT = 1.5F;
	private static final float VERY_HOT = 2.1F;

	private DefaultCropBiomeConfig() {
	}

	public static CropBiomeLimiterConfig create() {
		ThresholdCropRule defaultThresholdRule = defaultRule();
		ThresholdModeRules overworldRules = new ThresholdModeRules(defaultThresholdRule, vanillaOverworldCropRules());
		ThresholdModeRules netherRules = new ThresholdModeRules(defaultThresholdRule, vanillaNetherCropRules());
		ThresholdModeRules endRules = new ThresholdModeRules(defaultThresholdRule, vanillaEndCropRules());

		return new CropBiomeLimiterConfig(
				GeneralOptions.defaults(),
				overworldRules,
				Map.of(
						Level.OVERWORLD, overworldRules,
						Level.NETHER, netherRules,
						Level.END, endRules
				)
		);
	}

	public static Identifier id(String value) {
		return tryId(value).orElseGet(() -> CropBiomeLimiter.id("invalid"));
	}

	public static Optional<Identifier> tryId(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Identifier.parse(value));
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Ignoring invalid configured identifier: {}", value, exception);
			return Optional.empty();
		}
	}

	private static Map<Identifier, ThresholdCropRule> vanillaOverworldCropRules() {
		Map<Identifier, ThresholdCropRule> rules = new LinkedHashMap<>();
		putOverworldCropRules(rules, true);
		putNetherCropRules(rules, false);
		putEndCropRules(rules, false);
		return Map.copyOf(rules);
	}

	private static Map<Identifier, ThresholdCropRule> vanillaNetherCropRules() {
		Map<Identifier, ThresholdCropRule> rules = new LinkedHashMap<>();
		putOverworldCropRules(rules, false);
		putNetherCropRules(rules, true);
		putEndCropRules(rules, false);
		return Map.copyOf(rules);
	}

	private static Map<Identifier, ThresholdCropRule> vanillaEndCropRules() {
		Map<Identifier, ThresholdCropRule> rules = new LinkedHashMap<>();
		putOverworldCropRules(rules, false);
		putNetherCropRules(rules, false);
		putEndCropRules(rules, true);
		return Map.copyOf(rules);
	}

	private static void putOverworldCropRules(Map<Identifier, ThresholdCropRule> rules, boolean growableInDimension) {
		ThresholdCropRule wheat = dimensionRule(wheatRule(), growableInDimension);
		ThresholdCropRule carrots = dimensionRule(carrotRule(), growableInDimension);
		ThresholdCropRule potatoes = dimensionRule(potatoRule(), growableInDimension);
		ThresholdCropRule beetroots = dimensionRule(beetrootRule(), growableInDimension);
		ThresholdCropRule pumpkin = dimensionRule(pumpkinRule(), growableInDimension);
		ThresholdCropRule melon = dimensionRule(melonRule(), growableInDimension);
		ThresholdCropRule oakTree = dimensionRule(oakTreeRule(), growableInDimension);
		ThresholdCropRule birchTree = dimensionRule(birchTreeRule(), growableInDimension);
		ThresholdCropRule cherryTree = dimensionRule(cherryTreeRule(), growableInDimension);
		ThresholdCropRule spruceTree = dimensionRule(spruceTreeRule(), growableInDimension);
		ThresholdCropRule acaciaTree = dimensionRule(acaciaTreeRule(), growableInDimension);
		ThresholdCropRule paleGarden = dimensionRule(paleGardenRule(), growableInDimension);
		ThresholdCropRule forestWet = dimensionRule(forestWetRule(), growableInDimension);
		ThresholdCropRule warmWet = dimensionRule(warmWetRule(), growableInDimension);
		ThresholdCropRule tropicalWet = dimensionRule(tropicalWetRule(), growableInDimension);
		ThresholdCropRule hotDry = dimensionRule(desertDryRule(), growableInDimension);
		ThresholdCropRule alwaysGrowable = alwaysGrowableRule();
		ThresholdCropRule sugarCane = dimensionRule(sugarCaneRule(), growableInDimension);
		ThresholdCropRule mushroom = dimensionRule(overworldMushroomRule(), growableInDimension);
		ThresholdCropRule aquatic = dimensionRule(aquaticWetRule(), growableInDimension);
		ThresholdCropRule lush = dimensionRule(lushWetRule(), growableInDimension);
		ThresholdCropRule wetBrush = dimensionRule(wetBrushRule(), growableInDimension);
		ThresholdCropRule fern = dimensionRule(fernRule(), growableInDimension);

		put(rules, "minecraft:wheat", wheat);
		put(rules, "minecraft:carrots", carrots);
		put(rules, "minecraft:potatoes", potatoes);
		put(rules, "minecraft:beetroots", beetroots);
		put(rules, "minecraft:pumpkin_stem", pumpkin);
		put(rules, "minecraft:attached_pumpkin_stem", pumpkin);
		put(rules, "minecraft:torchflower_crop", warmWet);
		put(rules, "minecraft:wildflowers", birchTree);

		put(rules, "minecraft:melon_stem", melon);
		put(rules, "minecraft:attached_melon_stem", melon);
		put(rules, "minecraft:pitcher_crop", warmWet);
		put(rules, "minecraft:mangrove_propagule", warmWet);
		put(rules, "minecraft:bamboo_sapling", tropicalWet);
		put(rules, "minecraft:bamboo", tropicalWet);

		put(rules, "minecraft:cocoa", tropicalWet);
		put(rules, "minecraft:jungle_sapling", tropicalWet);

		put(rules, "minecraft:cactus", hotDry);
		put(rules, "minecraft:cactus_flower", hotDry);
		put(rules, "minecraft:acacia_sapling", acaciaTree);
		put(rules, "minecraft:short_dry_grass", hotDry);
		put(rules, "minecraft:tall_dry_grass", hotDry);
		put(rules, "minecraft:bush", alwaysGrowable);

		put(rules, "minecraft:sugar_cane", sugarCane);

		put(rules, "minecraft:sweet_berry_bush", spruceTree);
		put(rules, "minecraft:spruce_sapling", spruceTree);
		put(rules, "minecraft:firefly_bush", wetBrush);

		put(rules, "minecraft:oak_sapling", oakTree);
		put(rules, "minecraft:birch_sapling", birchTree);
		put(rules, "minecraft:cherry_sapling", cherryTree);
		put(rules, "minecraft:azalea", lush);
		put(rules, "minecraft:flowering_azalea", lush);

		put(rules, "minecraft:dark_oak_sapling", forestWet);
		put(rules, "minecraft:pale_oak_sapling", paleGarden);
		put(rules, "minecraft:pale_moss_block", paleGarden);
		put(rules, "minecraft:pale_moss_carpet", paleGarden);
		put(rules, "minecraft:pale_hanging_moss", paleGarden);
		put(rules, "minecraft:hanging_roots", forestWet);

		put(rules, "minecraft:brown_mushroom", mushroom);
		put(rules, "minecraft:red_mushroom", mushroom);

		put(rules, "minecraft:seagrass", aquatic);
		put(rules, "minecraft:tall_seagrass", aquatic);
		put(rules, "minecraft:kelp", aquatic);
		put(rules, "minecraft:kelp_plant", aquatic);

		put(rules, "minecraft:cave_vines", lush);
		put(rules, "minecraft:cave_vines_plant", lush);
		put(rules, "minecraft:vine", lush);
		put(rules, "minecraft:glow_lichen", lush);
		put(rules, "minecraft:big_dripleaf", lush);
		put(rules, "minecraft:big_dripleaf_stem", lush);
		put(rules, "minecraft:small_dripleaf", lush);
		put(rules, "minecraft:moss_block", lush);
		put(rules, "minecraft:moss_carpet", lush);
		put(rules, "minecraft:short_grass", wetBrush);
		put(rules, "minecraft:tall_grass", wetBrush);
		put(rules, "minecraft:fern", fern);
		put(rules, "minecraft:large_fern", spruceTree);
	}

	private static void putNetherCropRules(Map<Identifier, ThresholdCropRule> rules, boolean growableInDimension) {
		ThresholdCropRule nether = dimensionRule(netherRule(), growableInDimension);
		put(rules, "minecraft:nether_wart", nether);
		put(rules, "minecraft:crimson_fungus", nether);
		put(rules, "minecraft:warped_fungus", nether);
		put(rules, "minecraft:crimson_roots", nether);
		put(rules, "minecraft:warped_roots", nether);
		put(rules, "minecraft:weeping_vines", nether);
		put(rules, "minecraft:weeping_vines_plant", nether);
		put(rules, "minecraft:twisting_vines", nether);
		put(rules, "minecraft:twisting_vines_plant", nether);
	}

	private static void putEndCropRules(Map<Identifier, ThresholdCropRule> rules, boolean growableInDimension) {
		ThresholdCropRule end = dimensionRule(endRule(), growableInDimension);
		put(rules, "minecraft:chorus_flower", end);
		put(rules, "minecraft:chorus_plant", end);
	}

	private static void put(Map<Identifier, ThresholdCropRule> rules, String cropId, ThresholdCropRule rule) {
		rules.put(id(cropId), rule);
	}

	private static ThresholdCropRule dimensionRule(ThresholdCropRule rule, boolean growableInDimension) {
		if (growableInDimension) {
			return rule;
		}

		return new ThresholdCropRule(CropBehavior.BONEMEAL_REQUIRED, rule.climateRules().stream()
				.map(DefaultCropBiomeConfig::bonemealRequired)
				.toList());
	}

	private static ClimateRule bonemealRequired(ClimateRule rule) {
		return new ClimateRule(
				rule.minTemperatureInclusive(),
				rule.maxTemperatureExclusive(),
				rule.precipitation(),
				CropBehavior.BONEMEAL_REQUIRED
		);
	}

	private static ThresholdCropRule wheatRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TROPICAL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule carrotRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(TEMPERATE, VERY_HOT, PrecipitationRequirement.IGNORED)
				)
		);
	}

	private static ThresholdCropRule potatoRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, TEMPERATE, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule beetrootRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, COOL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule pumpkinRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, VERY_HOT, PrecipitationRequirement.IGNORED)
				)
		);
	}

	private static ThresholdCropRule melonRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(TROPICAL, HOT, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule oakTreeRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(FREEZING, TROPICAL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule birchTreeRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TROPICAL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule cherryTreeRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TEMPERATE, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule spruceTreeRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, COOL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule acaciaTreeRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(TEMPERATE, VERY_HOT, PrecipitationRequirement.FORBIDDEN)
				)
		);
	}

	private static ThresholdCropRule paleGardenRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TEMPERATE, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule forestWetRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TROPICAL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule warmWetRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(TEMPERATE, HOT, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule tropicalWetRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(TROPICAL, HOT, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule desertDryRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(HOT, VERY_HOT, PrecipitationRequirement.FORBIDDEN)
				)
		);
	}

	private static ThresholdCropRule sugarCaneRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, VERY_HOT, PrecipitationRequirement.IGNORED)
				)
		);
	}

	private static ThresholdCropRule overworldMushroomRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, HOT, PrecipitationRequirement.REQUIRED),
						growable(COOL, TEMPERATE, PrecipitationRequirement.FORBIDDEN)
				)
		);
	}

	private static ThresholdCropRule aquaticWetRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, HOT, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule lushWetRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TROPICAL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule wetBrushRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, HOT, PrecipitationRequirement.REQUIRED),
						growable(TEMPERATE, VERY_HOT, PrecipitationRequirement.FORBIDDEN)
				)
		);
	}

	private static ThresholdCropRule fernRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, COOL, PrecipitationRequirement.REQUIRED),
						growable(TROPICAL, HOT, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule netherRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(HOT, VERY_HOT, PrecipitationRequirement.FORBIDDEN)
				)
		);
	}

	private static ThresholdCropRule endRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(COOL, TEMPERATE, PrecipitationRequirement.FORBIDDEN)
				)
		);
	}

	private static ThresholdCropRule defaultRule() {
		return new ThresholdCropRule(CropBehavior.BONEMEAL_REQUIRED, List.of());
	}

	private static ThresholdCropRule alwaysGrowableRule() {
		return new ThresholdCropRule(CropBehavior.GROWABLE, List.of());
	}

	private static ClimateRule growable(float minTemperatureInclusive, float maxTemperatureExclusive, PrecipitationRequirement precipitation) {
		return new ClimateRule(minTemperatureInclusive, maxTemperatureExclusive, precipitation, CropBehavior.GROWABLE);
	}
}
