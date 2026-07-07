import assert from "node:assert/strict";
import {
	cycleBehavior,
	resolveThresholdBehavior,
	collectDimensions,
	collectCrops,
	collectBiomes,
	copyDimensionModeAndRules,
	validateConfigBundle,
	DEFAULT_BIOMES,
	DEFAULT_EXPLICIT,
	DEFAULT_THRESHOLD,
	compactId,
	matchesBiomeFilter,
	sortBiomes
} from "./app.js";

assert.equal(cycleBehavior("growable"), "bonemeal-required");
assert.equal(cycleBehavior("bonemeal-required"), "unplantable");
assert.equal(cycleBehavior("unplantable"), "growable");
assert.equal(cycleBehavior("not-a-behavior"), "bonemeal-required");

const general = {
	fallback_mode: "threshold",
	dimensions: {
		"minecraft:overworld": "explicit",
		"modded:moon": "threshold"
	}
};
const explicit = {
	fallback: { default_behavior: "growable", biomes: {} },
	dimensions: {
		"minecraft:overworld": {
			default_behavior: "growable",
			biomes: {
				"minecraft:plains": {
					"minecraft:wheat": "growable"
				}
			}
		}
	}
};
const threshold = {
	fallback: {
		default_rule: { default_behavior: "growable", climate_rules: [] },
		crops: {}
	},
	dimensions: {
		"modded:moon": {
			default_rule: { default_behavior: "bonemeal-required", climate_rules: [] },
			crops: {
				"modded:moon_carrot": {
					default_behavior: "bonemeal-required",
					climate_rules: []
				}
			}
		}
	}
};
const snapshot = {
	dimensions: ["minecraft:overworld", "modded:moon"],
	biomes: [
		{ id: "modded:blue_dunes", temperature: 1.8, has_precipitation: false }
	],
	crops: ["modded:moon_carrot"]
};

assert.deepEqual(collectDimensions(general, explicit, threshold, snapshot).filter((id) => id.includes("moon")), ["modded:moon"]);
assert.ok(collectCrops(explicit, threshold, snapshot).includes("minecraft:wheat"));
assert.ok(collectCrops(explicit, threshold, snapshot).includes("modded:moon_carrot"));
assert.ok(collectBiomes(explicit, snapshot).some((biome) => biome.id === "modded:blue_dunes"));

assert.equal(DEFAULT_BIOMES.length, 65);
assert.equal(new Set(DEFAULT_BIOMES.map((biome) => biome.id)).size, DEFAULT_BIOMES.length);
assert.ok(DEFAULT_BIOMES.some((biome) => biome.id === "minecraft:pale_garden"));
assert.ok(DEFAULT_BIOMES.some((biome) => biome.id === "minecraft:deep_dark"));
assert.ok(DEFAULT_BIOMES.some((biome) => biome.id === "minecraft:basalt_deltas" && biome.temperature === 2.0 && !biome.has_precipitation));
assert.ok(DEFAULT_BIOMES.some((biome) => biome.id === "minecraft:frozen_peaks" && biome.temperature === -0.7 && biome.has_precipitation));
assert.equal(collectBiomes(explicit, null).length, DEFAULT_BIOMES.length);

assert.deepEqual(DEFAULT_THRESHOLD.dimensions, {});
assert.deepEqual(DEFAULT_THRESHOLD.fallback.crops, {});
assert.equal(DEFAULT_THRESHOLD.fallback.default_rule.default_behavior, "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:desert", "minecraft:cactus"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:savanna", "minecraft:cactus"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:ice_spikes", "minecraft:cactus"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:savanna", "minecraft:short_grass"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:savanna", "minecraft:tall_grass"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:badlands", "minecraft:firefly_bush"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:savanna", "minecraft:wheat"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:snowy_plains", "minecraft:wheat"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:snowy_plains", "minecraft:carrots"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:desert", "minecraft:carrots"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:plains", "minecraft:carrots"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:snowy_plains", "minecraft:beetroots"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:taiga", "minecraft:beetroots"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:taiga", "minecraft:potatoes"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:snowy_plains", "minecraft:potatoes"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:plains", "minecraft:potatoes"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:desert", "minecraft:beetroots"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:desert", "minecraft:melon_stem"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:savanna", "minecraft:melon_stem"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:plains", "minecraft:melon_stem"), "bonemeal-required");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:savanna", "minecraft:acacia_sapling"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:desert", "minecraft:acacia_sapling"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:cherry_grove", "minecraft:bush"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:birch_forest", "minecraft:bush"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:jungle", "minecraft:cocoa"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:jungle", "minecraft:fern"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:overworld"], "minecraft:plains", "minecraft:wheat"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:the_nether"], "minecraft:nether_wastes", "minecraft:bush"), "growable");
assert.equal(explicitBehavior(DEFAULT_EXPLICIT.dimensions["minecraft:the_end"], "minecraft:the_end", "minecraft:bush"), "growable");

assert.equal(compactId("minecraft:desert"), "desert");
assert.equal(compactId("biomesoplenty:lavender_field"), "lavender_field");
assert.equal(matchesBiomeFilter({ id: "minecraft:desert", temperature: 2.0, has_precipitation: false }, "temp >= 1.5 dry"), true);
assert.equal(matchesBiomeFilter({ id: "minecraft:ice_spikes", temperature: 0.0, has_precipitation: true }, "temp >= 1.5 dry"), false);
assert.equal(matchesBiomeFilter({ id: "biomesoplenty:lavender_field", temperature: 0.7, has_precipitation: true }, "lavender wet"), true);
assert.deepEqual(sortBiomes([
	{ id: "minecraft:desert", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:plains", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:ice_spikes", temperature: 0.0, has_precipitation: true }
], "temperature").map((biome) => biome.id), ["minecraft:ice_spikes", "minecraft:plains", "minecraft:desert"]);

const wheatRule = {
	default_behavior: "bonemeal-required",
	climate_rules: [
		{
			min_temperature: 0.15,
			max_temperature: 1.5,
			precipitation: "required",
			behavior: "growable"
		},
		{
			min_temperature: 1.5,
			max_temperature: 3.0,
			precipitation: "forbidden",
			behavior: "unplantable"
		}
	]
};

assert.equal(resolveThresholdBehavior(wheatRule, { temperature: 0.8, has_precipitation: true }), "growable");
assert.equal(resolveThresholdBehavior(wheatRule, { temperature: 0.8, has_precipitation: false }), "bonemeal-required");
assert.equal(resolveThresholdBehavior(wheatRule, { temperature: 2.0, has_precipitation: false }), "unplantable");
assert.equal(resolveThresholdBehavior(wheatRule, { temperature: null, has_precipitation: false }), "bonemeal-required");

const copyGeneral = {
	fallback_mode: "threshold",
	dimensions: {
		"minecraft:overworld": "threshold",
		"modded:moon": "explicit"
	}
};
const copyThreshold = {
	fallback: {
		default_rule: { default_behavior: "growable", climate_rules: [] },
		crops: {}
	},
	dimensions: {
		"minecraft:overworld": {
			default_rule: { default_behavior: "bonemeal-required", climate_rules: [] },
			crops: {
				"minecraft:cactus": {
					default_behavior: "unplantable",
					climate_rules: [
						{ min_temperature: 1.5, max_temperature: 3.0, precipitation: "forbidden", behavior: "growable" }
					]
				}
			}
		}
	}
};
const copyExplicit = { fallback: { default_behavior: "growable", biomes: {} }, dimensions: {} };

assert.equal(copyDimensionModeAndRules(copyGeneral, copyExplicit, copyThreshold, "minecraft:overworld", "modded:moon"), true);
assert.equal(copyGeneral.dimensions["modded:moon"], "threshold");
assert.deepEqual(
	copyThreshold.dimensions["modded:moon"].crops["minecraft:cactus"],
	copyThreshold.dimensions["minecraft:overworld"].crops["minecraft:cactus"]
);
copyThreshold.dimensions["modded:moon"].crops["minecraft:cactus"].default_behavior = "growable";
assert.equal(copyThreshold.dimensions["minecraft:overworld"].crops["minecraft:cactus"].default_behavior, "unplantable");
assert.equal(copyDimensionModeAndRules(copyGeneral, copyExplicit, copyThreshold, "modded:moon", "modded:moon"), false);

const validationMessages = validateConfigBundle(
	{
		schema_version: 99,
		fallback_mode: "mystery",
		dimensions: { "minecraft:overworld": "also-mystery" },
		affects_village_farm_generation: "yes",
		excluded_blocks: "minecraft:stone"
	},
	{
		schema_version: 1,
		fallback: { default_behavior: "not-real", biomes: {} },
		dimensions: {}
	},
	{
		schema_version: 1,
		fallback: {
			default_rule: {
				default_behavior: "growable",
				climate_rules: [
					{ min_temperature: 2.0, max_temperature: 1.0, precipitation: "maybe", behavior: "nope" }
				]
			},
			crops: {}
		},
		dimensions: {}
	},
	{
		counts: { dimensions: 2 },
		dimensions: ["minecraft:overworld"]
	}
);

assert.ok(validationMessages.some((message) => message.includes("schema_version 99")));
assert.ok(validationMessages.some((message) => message.includes("unknown mode")));
assert.ok(validationMessages.some((message) => message.includes("affects_village_farm_generation must be true or false")));
assert.ok(validationMessages.some((message) => message.includes("not a known behavior")));
assert.ok(validationMessages.some((message) => message.includes("min_temperature must be lower")));
assert.ok(validationMessages.some((message) => message.includes("count is 2")));

function explicitBehavior(rules, biome, crop) {
	return rules.biomes?.[biome]?.[crop] ?? rules.default_behavior;
}
