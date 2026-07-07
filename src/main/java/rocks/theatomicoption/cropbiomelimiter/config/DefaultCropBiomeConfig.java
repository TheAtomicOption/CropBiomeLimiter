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
		ThresholdCropRule temperateAnnual = dimensionRule(temperateAnnualRule(), growableInDimension);
		ThresholdCropRule temperateTree = dimensionRule(temperateTreeRule(), growableInDimension);
		ThresholdCropRule coolWet = dimensionRule(coolWetRule(), growableInDimension);
		ThresholdCropRule forestWet = dimensionRule(forestWetRule(), growableInDimension);
		ThresholdCropRule warmWet = dimensionRule(warmWetRule(), growableInDimension);
		ThresholdCropRule tropicalWet = dimensionRule(tropicalWetRule(), growableInDimension);
		ThresholdCropRule hotDry = dimensionRule(hotDryRule(), growableInDimension);
		ThresholdCropRule sugarCane = dimensionRule(sugarCaneRule(), growableInDimension);
		ThresholdCropRule mushroom = dimensionRule(overworldMushroomRule(), growableInDimension);
		ThresholdCropRule aquatic = dimensionRule(aquaticWetRule(), growableInDimension);
		ThresholdCropRule lush = dimensionRule(lushWetRule(), growableInDimension);

		put(rules, "minecraft:wheat", temperateAnnual);
		put(rules, "minecraft:carrots", temperateAnnual);
		put(rules, "minecraft:potatoes", temperateAnnual);
		put(rules, "minecraft:beetroots", temperateAnnual);
		put(rules, "minecraft:pumpkin_stem", temperateAnnual);
		put(rules, "minecraft:attached_pumpkin_stem", temperateAnnual);
		put(rules, "minecraft:torchflower_crop", temperateAnnual);
		put(rules, "minecraft:wildflowers", temperateAnnual);

		put(rules, "minecraft:melon_stem", warmWet);
		put(rules, "minecraft:attached_melon_stem", warmWet);
		put(rules, "minecraft:pitcher_crop", warmWet);
		put(rules, "minecraft:mangrove_propagule", warmWet);
		put(rules, "minecraft:bamboo_sapling", warmWet);
		put(rules, "minecraft:bamboo", warmWet);

		put(rules, "minecraft:cocoa", tropicalWet);
		put(rules, "minecraft:jungle_sapling", tropicalWet);

		put(rules, "minecraft:cactus", hotDry);
		put(rules, "minecraft:cactus_flower", hotDry);
		put(rules, "minecraft:acacia_sapling", hotDry);
		put(rules, "minecraft:short_dry_grass", hotDry);
		put(rules, "minecraft:tall_dry_grass", hotDry);
		put(rules, "minecraft:bush", hotDry);

		put(rules, "minecraft:sugar_cane", sugarCane);

		put(rules, "minecraft:sweet_berry_bush", coolWet);
		put(rules, "minecraft:spruce_sapling", coolWet);
		put(rules, "minecraft:firefly_bush", coolWet);

		put(rules, "minecraft:oak_sapling", temperateTree);
		put(rules, "minecraft:birch_sapling", temperateTree);
		put(rules, "minecraft:cherry_sapling", temperateTree);
		put(rules, "minecraft:azalea", temperateTree);
		put(rules, "minecraft:flowering_azalea", temperateTree);

		put(rules, "minecraft:dark_oak_sapling", forestWet);
		put(rules, "minecraft:pale_oak_sapling", forestWet);
		put(rules, "minecraft:pale_moss_block", forestWet);
		put(rules, "minecraft:pale_moss_carpet", forestWet);
		put(rules, "minecraft:pale_hanging_moss", forestWet);
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
		put(rules, "minecraft:short_grass", temperateTree);
		put(rules, "minecraft:tall_grass", temperateTree);
		put(rules, "minecraft:fern", forestWet);
		put(rules, "minecraft:large_fern", forestWet);
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

	private static ThresholdCropRule temperateAnnualRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(FREEZING, TROPICAL, PrecipitationRequirement.REQUIRED)
				)
		);
	}

	private static ThresholdCropRule temperateTreeRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(FREEZING, TROPICAL, PrecipitationRequirement.REQUIRED)
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

	private static ThresholdCropRule coolWetRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(VERY_COLD, COOL, PrecipitationRequirement.REQUIRED)
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

	private static ThresholdCropRule hotDryRule() {
		return new ThresholdCropRule(
				CropBehavior.BONEMEAL_REQUIRED,
				List.of(
						growable(TEMPERATE, VERY_HOT, PrecipitationRequirement.FORBIDDEN)
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

	private static ClimateRule growable(float minTemperatureInclusive, float maxTemperatureExclusive, PrecipitationRequirement precipitation) {
		return new ClimateRule(minTemperatureInclusive, maxTemperatureExclusive, precipitation, CropBehavior.GROWABLE);
	}
}
