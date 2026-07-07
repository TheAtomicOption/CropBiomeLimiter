export const BEHAVIORS = ["growable", "bonemeal-required", "unplantable"];
export const MODES = ["explicit", "threshold"];
export const PRECIPITATION_REQUIREMENTS = ["required", "forbidden", "ignored"];

const FILE_NAMES = {
	general: "general.json",
	explicit: "explicit-mode.json",
	threshold: "threshold-mode.json",
	snapshot: "cropbiomelimiter-registry-snapshot.json"
};

const MODE_LABELS = {
	explicit: "Explicit mode",
	threshold: "Threshold mode"
};

const BEHAVIOR_LABELS = {
	growable: "Growable",
	"bonemeal-required": "Bonemeal required",
	unplantable: "Unplantable"
};

const BEHAVIOR_TOKENS = {
	growable: "G",
	"bonemeal-required": "B",
	unplantable: "U"
};

const PRECIPITATION_LABELS = {
	required: "Required",
	forbidden: "Forbidden",
	ignored: "Ignored"
};

const DEFAULT_DIMENSIONS = [
	"minecraft:overworld",
	"minecraft:the_nether",
	"minecraft:the_end"
];

const DEFAULT_CROPS = [
	"minecraft:wheat",
	"minecraft:carrots",
	"minecraft:potatoes",
	"minecraft:beetroots",
	"minecraft:melon_stem",
	"minecraft:pumpkin_stem",
	"minecraft:cactus",
	"minecraft:sugar_cane",
	"minecraft:cocoa",
	"minecraft:nether_wart",
	"minecraft:oak_sapling",
	"minecraft:spruce_sapling",
	"minecraft:birch_sapling",
	"minecraft:jungle_sapling",
	"minecraft:acacia_sapling",
	"minecraft:dark_oak_sapling",
	"minecraft:cherry_sapling",
	"minecraft:pale_oak_sapling",
	"minecraft:mangrove_propagule",
	"minecraft:sweet_berry_bush",
	"minecraft:cave_vines"
];

export const DEFAULT_BIOMES = [
	{ id: "minecraft:the_void", temperature: 0.5, has_precipitation: false },
	{ id: "minecraft:plains", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:sunflower_plains", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:snowy_plains", temperature: 0.0, has_precipitation: true },
	{ id: "minecraft:ice_spikes", temperature: 0.0, has_precipitation: true },
	{ id: "minecraft:desert", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:swamp", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:mangrove_swamp", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:forest", temperature: 0.7, has_precipitation: true },
	{ id: "minecraft:flower_forest", temperature: 0.7, has_precipitation: true },
	{ id: "minecraft:birch_forest", temperature: 0.6, has_precipitation: true },
	{ id: "minecraft:dark_forest", temperature: 0.7, has_precipitation: true },
	{ id: "minecraft:pale_garden", temperature: 0.7, has_precipitation: true },
	{ id: "minecraft:old_growth_birch_forest", temperature: 0.6, has_precipitation: true },
	{ id: "minecraft:old_growth_pine_taiga", temperature: 0.3, has_precipitation: true },
	{ id: "minecraft:old_growth_spruce_taiga", temperature: 0.25, has_precipitation: true },
	{ id: "minecraft:taiga", temperature: 0.25, has_precipitation: true },
	{ id: "minecraft:snowy_taiga", temperature: -0.5, has_precipitation: true },
	{ id: "minecraft:savanna", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:savanna_plateau", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:windswept_hills", temperature: 0.2, has_precipitation: true },
	{ id: "minecraft:windswept_gravelly_hills", temperature: 0.2, has_precipitation: true },
	{ id: "minecraft:windswept_forest", temperature: 0.2, has_precipitation: true },
	{ id: "minecraft:windswept_savanna", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:jungle", temperature: 0.95, has_precipitation: true },
	{ id: "minecraft:sparse_jungle", temperature: 0.95, has_precipitation: true },
	{ id: "minecraft:bamboo_jungle", temperature: 0.95, has_precipitation: true },
	{ id: "minecraft:badlands", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:eroded_badlands", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:wooded_badlands", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:meadow", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:cherry_grove", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:grove", temperature: -0.2, has_precipitation: true },
	{ id: "minecraft:snowy_slopes", temperature: -0.3, has_precipitation: true },
	{ id: "minecraft:frozen_peaks", temperature: -0.7, has_precipitation: true },
	{ id: "minecraft:jagged_peaks", temperature: -0.7, has_precipitation: true },
	{ id: "minecraft:stony_peaks", temperature: 1.0, has_precipitation: true },
	{ id: "minecraft:river", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:frozen_river", temperature: 0.0, has_precipitation: true },
	{ id: "minecraft:beach", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:snowy_beach", temperature: 0.05, has_precipitation: true },
	{ id: "minecraft:stony_shore", temperature: 0.2, has_precipitation: true },
	{ id: "minecraft:warm_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:lukewarm_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:deep_lukewarm_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:deep_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:cold_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:deep_cold_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:frozen_ocean", temperature: 0.0, has_precipitation: true },
	{ id: "minecraft:deep_frozen_ocean", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:mushroom_fields", temperature: 0.9, has_precipitation: true },
	{ id: "minecraft:dripstone_caves", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:lush_caves", temperature: 0.5, has_precipitation: true },
	{ id: "minecraft:deep_dark", temperature: 0.8, has_precipitation: true },
	{ id: "minecraft:nether_wastes", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:warped_forest", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:crimson_forest", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:soul_sand_valley", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:basalt_deltas", temperature: 2.0, has_precipitation: false },
	{ id: "minecraft:the_end", temperature: 0.5, has_precipitation: false },
	{ id: "minecraft:end_highlands", temperature: 0.5, has_precipitation: false },
	{ id: "minecraft:end_midlands", temperature: 0.5, has_precipitation: false },
	{ id: "minecraft:small_end_islands", temperature: 0.5, has_precipitation: false },
	{ id: "minecraft:end_barrens", temperature: 0.5, has_precipitation: false }
];

const DEFAULT_GENERAL = {
	schema_version: 1,
	affects_bonemeal: true,
	affects_block_placement: true,
	chat_info: true,
	excluded_blocks: [],
	fallback_mode: "threshold",
	dimensions: {
		"minecraft:overworld": "threshold",
		"minecraft:the_nether": "threshold",
		"minecraft:the_end": "threshold"
	}
};

const DEFAULT_EXPLICIT = {
	schema_version: 1,
	fallback: {
		default_behavior: "bonemeal-required",
		biomes: {}
	},
	dimensions: {}
};

const DEFAULT_THRESHOLD = {
	schema_version: 1,
	fallback: {
		default_rule: {
			default_behavior: "bonemeal-required",
			climate_rules: []
		},
		crops: {}
	},
	dimensions: {}
};

const state = {
	general: clone(DEFAULT_GENERAL),
	explicit: clone(DEFAULT_EXPLICIT),
	threshold: clone(DEFAULT_THRESHOLD),
	snapshot: null,
	selectedDimension: "minecraft:overworld",
	cropFilter: "",
	biomeFilter: "",
	status: "Defaults loaded",
	importMessages: [],
	helper: {
		available: false,
		configDirectory: "",
		message: "Manual import/export"
	}
};

let refs = {};

export function cycleBehavior(value) {
	const current = BEHAVIORS.includes(value) ? value : "growable";
	return BEHAVIORS[(BEHAVIORS.indexOf(current) + 1) % BEHAVIORS.length];
}

export function resolveThresholdBehavior(rule, biome) {
	const normalizedRule = normalizeThresholdRule(rule);
	const temperature = Number(biome?.temperature);
	const hasPrecipitation = Boolean(biome?.has_precipitation);
	if (!Number.isFinite(temperature)) {
		return normalizedRule.default_behavior;
	}

	for (const climateRule of normalizedRule.climate_rules) {
		const min = numberOr(climateRule.min_temperature, Number.NEGATIVE_INFINITY);
		const max = numberOr(climateRule.max_temperature, Number.POSITIVE_INFINITY);
		if (temperature >= min && temperature < max && precipitationMatches(climateRule.precipitation, hasPrecipitation)) {
			return behaviorOr(climateRule.behavior);
		}
	}

	return normalizedRule.default_behavior;
}

export function collectDimensions(general, explicit, threshold, snapshot) {
	const values = new Set(DEFAULT_DIMENSIONS);
	addKeys(values, general?.dimensions);
	addKeys(values, explicit?.dimensions);
	addKeys(values, threshold?.dimensions);
	for (const dimension of arrayOr(snapshot?.dimensions)) {
		if (typeof dimension === "string") {
			values.add(dimension);
		} else if (typeof dimension?.id === "string") {
			values.add(dimension.id);
		}
	}
	return sortIds(values);
}

export function collectCrops(explicit, threshold, snapshot) {
	const values = new Set(DEFAULT_CROPS);
	for (const crop of arrayOr(snapshot?.crops)) {
		if (typeof crop === "string") {
			values.add(crop);
		} else if (typeof crop?.id === "string") {
			values.add(crop.id);
		}
	}

	collectExplicitCrops(values, explicit?.fallback);
	for (const rules of Object.values(explicit?.dimensions ?? {})) {
		collectExplicitCrops(values, rules);
	}

	addKeys(values, threshold?.fallback?.crops);
	for (const rules of Object.values(threshold?.dimensions ?? {})) {
		addKeys(values, rules?.crops);
	}

	return sortIds(values);
}

export function collectBiomes(explicit, snapshot) {
	const values = new Map(DEFAULT_BIOMES.map((biome) => [biome.id, clone(biome)]));
	for (const biome of arrayOr(snapshot?.biomes)) {
		const normalized = normalizeBiome(biome);
		if (normalized) {
			values.set(normalized.id, normalized);
		}
	}

	collectExplicitBiomes(values, explicit?.fallback);
	for (const rules of Object.values(explicit?.dimensions ?? {})) {
		collectExplicitBiomes(values, rules);
	}

	return [...values.values()].sort((left, right) => left.id.localeCompare(right.id));
}

export function copyDimensionModeAndRules(general, explicit, threshold, sourceDimension, targetDimension) {
	if (!sourceDimension || !targetDimension || sourceDimension === targetDimension) {
		return false;
	}

	general.dimensions ??= {};
	const mode = modeOr(general.dimensions[sourceDimension] ?? general.fallback_mode);
	general.dimensions[targetDimension] = mode;

	if (mode === "explicit") {
		explicit.dimensions ??= {};
		explicit.dimensions[targetDimension] = clone(explicit.dimensions?.[sourceDimension] ?? {
			default_behavior: explicit.fallback?.default_behavior ?? "bonemeal-required",
			biomes: {}
		});
		return true;
	}

	threshold.dimensions ??= {};
	threshold.dimensions[targetDimension] = clone(threshold.dimensions?.[sourceDimension] ?? {
		default_rule: threshold.fallback?.default_rule ?? DEFAULT_THRESHOLD.fallback.default_rule,
		crops: {}
	});
	return true;
}

export function validateConfigBundle(general, explicit, threshold, snapshot) {
	const messages = [];
	validateGeneral(general, messages);
	validateExplicit(explicit, messages);
	validateThreshold(threshold, messages);
	validateSnapshot(snapshot, messages);
	return messages;
}

function init() {
	refs = {
		fileInput: document.getElementById("fileInput"),
		statusText: document.getElementById("statusText"),
		loadInstalled: document.getElementById("loadInstalled"),
		saveInstalled: document.getElementById("saveInstalled"),
		downloadGeneral: document.getElementById("downloadGeneral"),
		downloadExplicit: document.getElementById("downloadExplicit"),
		downloadThreshold: document.getElementById("downloadThreshold"),
		downloadAll: document.getElementById("downloadAll"),
		dimensionSelect: document.getElementById("dimensionSelect"),
		modeSelect: document.getElementById("modeSelect"),
		copyDimensionSelect: document.getElementById("copyDimensionSelect"),
		copyDimensionRules: document.getElementById("copyDimensionRules"),
		affectsBonemeal: document.getElementById("affectsBonemeal"),
		affectsPlacement: document.getElementById("affectsPlacement"),
		chatInfo: document.getElementById("chatInfo"),
		excludedBlocks: document.getElementById("excludedBlocks"),
		validationList: document.getElementById("validationList"),
		dimensionTabs: document.getElementById("dimensionTabs"),
		cropFilter: document.getElementById("cropFilter"),
		biomeFilter: document.getElementById("biomeFilter"),
		resetDimension: document.getElementById("resetDimension"),
		explicitPanel: document.getElementById("explicitPanel"),
		thresholdPanel: document.getElementById("thresholdPanel"),
		explicitDefault: document.getElementById("explicitDefault"),
		thresholdDefault: document.getElementById("thresholdDefault"),
		bulkMin: document.getElementById("bulkMin"),
		bulkMax: document.getElementById("bulkMax"),
		bulkPrecipitation: document.getElementById("bulkPrecipitation"),
		bulkBehavior: document.getElementById("bulkBehavior"),
		applyThresholdVisible: document.getElementById("applyThresholdVisible"),
		explicitGrid: document.getElementById("explicitGrid"),
		thresholdGrid: document.getElementById("thresholdGrid")
	};

	fillBehaviorSelect(refs.explicitDefault);
	fillBehaviorSelect(refs.thresholdDefault);
	fillRequirementSelect(refs.bulkPrecipitation);
	fillBehaviorSelect(refs.bulkBehavior);
	refs.bulkBehavior.value = "growable";
	bindEvents();
	render();
	connectHelper();
}

function bindEvents() {
	refs.loadInstalled.addEventListener("click", async () => {
		await loadInstalledConfig();
		render();
	});
	refs.saveInstalled.addEventListener("click", async () => {
		await saveInstalledConfig();
	});
	refs.fileInput.addEventListener("change", async () => {
		await loadFiles(refs.fileInput.files);
		refs.fileInput.value = "";
		render();
	});
	refs.downloadGeneral.addEventListener("click", () => downloadJson(FILE_NAMES.general, state.general));
	refs.downloadExplicit.addEventListener("click", () => downloadJson(FILE_NAMES.explicit, state.explicit));
	refs.downloadThreshold.addEventListener("click", () => downloadJson(FILE_NAMES.threshold, state.threshold));
	refs.downloadAll.addEventListener("click", () => downloadAllConfig());
	refs.dimensionSelect.addEventListener("change", () => {
		state.selectedDimension = refs.dimensionSelect.value;
		render();
	});
	refs.modeSelect.addEventListener("change", () => {
		state.general.dimensions[state.selectedDimension] = refs.modeSelect.value;
		ensureModeRules(state.selectedDimension, refs.modeSelect.value);
		render();
	});
	refs.copyDimensionRules.addEventListener("click", () => {
		const sourceDimension = state.selectedDimension;
		const targetDimension = refs.copyDimensionSelect.value;
		const mode = currentMode();
		if (copyDimensionModeAndRules(state.general, state.explicit, state.threshold, sourceDimension, targetDimension)) {
			state.importMessages = [];
			state.selectedDimension = targetDimension;
			state.status = `Copied ${MODE_LABELS[mode]} rules from ${sourceDimension} to ${targetDimension}`;
			render();
		}
	});
	refs.affectsBonemeal.addEventListener("change", () => {
		state.general.affects_bonemeal = refs.affectsBonemeal.checked;
	});
	refs.affectsPlacement.addEventListener("change", () => {
		state.general.affects_block_placement = refs.affectsPlacement.checked;
	});
	refs.chatInfo.addEventListener("change", () => {
		state.general.chat_info = refs.chatInfo.checked;
	});
	refs.excludedBlocks.addEventListener("input", () => {
		state.general.excluded_blocks = splitIds(refs.excludedBlocks.value);
	});
	refs.cropFilter.addEventListener("input", () => {
		state.cropFilter = refs.cropFilter.value.trim().toLowerCase();
		renderMode();
	});
	refs.biomeFilter.addEventListener("input", () => {
		state.biomeFilter = refs.biomeFilter.value.trim().toLowerCase();
		renderMode();
	});
	refs.explicitDefault.addEventListener("change", () => {
		ensureExplicitRules(state.selectedDimension).default_behavior = refs.explicitDefault.value;
		renderExplicitGrid();
	});
	refs.thresholdDefault.addEventListener("change", () => {
		ensureThresholdRules(state.selectedDimension).default_rule.default_behavior = refs.thresholdDefault.value;
		renderThresholdGrid();
	});
	refs.resetDimension.addEventListener("click", () => {
		resetDimensionRules(state.selectedDimension);
		render();
	});
	refs.applyThresholdVisible.addEventListener("click", () => {
		applyThresholdTemplateToVisible();
		renderThresholdGrid();
	});
}

async function connectHelper() {
	if (typeof fetch !== "function" || typeof window === "undefined" || window.location.protocol === "file:") {
		renderHelperControls();
		return;
	}

	try {
		const response = await fetch("/api/status", { cache: "no-store" });
		if (!response.ok) {
			throw new Error(`helper status ${response.status}`);
		}
		const status = await response.json();
		state.helper = {
			available: true,
			configDirectory: status.configDirectory ?? "",
			message: "Connected to installed config folder"
		};
		await loadInstalledConfig();
		render();
	} catch (error) {
		console.debug("Config helper API is not available.", error);
		state.helper = {
			available: false,
			configDirectory: "",
			message: "Manual import/export"
		};
		renderHelperControls();
	}
}

function renderHelperControls() {
	refs.loadInstalled.hidden = !state.helper.available;
	refs.saveInstalled.hidden = !state.helper.available;
}

async function loadInstalledConfig() {
	if (!state.helper.available) {
		return;
	}

	const loaded = [];
	const messages = [];
	await loadInstalledFile("general", FILE_NAMES.general, (json) => {
		state.general = normalizeGeneral(json);
	}, loaded, messages);
	await loadInstalledFile("explicit", FILE_NAMES.explicit, (json) => {
		state.explicit = normalizeExplicit(json);
	}, loaded, messages);
	await loadInstalledFile("threshold", FILE_NAMES.threshold, (json) => {
		state.threshold = normalizeThreshold(json);
	}, loaded, messages);
	await loadInstalledFile("snapshot", FILE_NAMES.snapshot, (json) => {
		state.snapshot = json;
	}, loaded, messages, true);

	state.importMessages = messages;
	if (loaded.length > 0) {
		state.status = `Loaded ${loaded.length} installed config file${loaded.length === 1 ? "" : "s"}`;
	} else {
		state.status = "Installed config files not found";
	}
}

async function loadInstalledFile(key, fileName, apply, loaded, messages, optional = false) {
	try {
		const response = await fetch(`/api/files/${key}`, { cache: "no-store" });
		if (response.status === 404 && optional) {
			return;
		}
		if (!response.ok) {
			throw new Error(`HTTP ${response.status}`);
		}
		const json = await response.json();
		messages.push(...validateLoadedFile(fileName, json));
		apply(json);
		loaded.push(fileName);
	} catch (error) {
		messages.push(`${fileName}: could not load from installed config folder`);
		console.error(error);
	}
}

async function saveInstalledConfig() {
	if (!state.helper.available) {
		return;
	}

	const messages = [];
	let saved = 0;
	saved += await saveInstalledFile("general", FILE_NAMES.general, state.general, messages);
	saved += await saveInstalledFile("explicit", FILE_NAMES.explicit, state.explicit, messages);
	saved += await saveInstalledFile("threshold", FILE_NAMES.threshold, state.threshold, messages);

	state.importMessages = messages;
	state.status = `Saved ${saved} installed config file${saved === 1 ? "" : "s"}`;
	render();
}

async function saveInstalledFile(key, fileName, value, messages) {
	try {
		const response = await fetch(`/api/files/${key}`, {
			method: "PUT",
			headers: { "Content-Type": "application/json" },
			body: `${JSON.stringify(value, null, 2)}\n`
		});
		if (!response.ok) {
			throw new Error(`HTTP ${response.status}`);
		}
		return 1;
	} catch (error) {
		messages.push(`${fileName}: could not save to installed config folder`);
		console.error(error);
		return 0;
	}
}

async function loadFiles(files) {
	let loaded = 0;
	const messages = [];
	for (const file of Array.from(files ?? [])) {
		let json = null;
		try {
			json = JSON.parse(await file.text());
		} catch (error) {
			messages.push(`${file.name}: invalid JSON`);
			console.error(error);
			continue;
		}

		try {
			messages.push(...validateLoadedFile(file.name, json));
			applyLoadedFile(file.name, json);
			loaded++;
		} catch (error) {
			messages.push(`${file.name}: ${error.message}`);
			console.error(error);
		}
	}
	state.importMessages = messages;
	if (loaded > 0) {
		state.status = `Loaded ${loaded} JSON file${loaded === 1 ? "" : "s"}`;
	} else if (messages.length > 0) {
		state.status = "No files loaded";
	} else {
		state.status = "No files selected";
	}
}

function applyLoadedFile(name, json) {
	const lowerName = name.toLowerCase();
	if (lowerName === FILE_NAMES.general) {
		state.general = normalizeGeneral(json);
		return;
	}
	if (lowerName === FILE_NAMES.explicit) {
		state.explicit = normalizeExplicit(json);
		return;
	}
	if (lowerName === FILE_NAMES.threshold) {
		state.threshold = normalizeThreshold(json);
		return;
	}
	if (lowerName === FILE_NAMES.snapshot || json?.config_schema || json?.counts) {
		state.snapshot = json;
		return;
	}
	throw new Error(`Unknown JSON file: ${name}`);
}

function render() {
	const dimensions = collectDimensions(state.general, state.explicit, state.threshold, state.snapshot);
	if (!dimensions.includes(state.selectedDimension)) {
		state.selectedDimension = dimensions[0] ?? "minecraft:overworld";
	}
	renderStatus();
	renderSettings(dimensions);
	renderValidation();
	renderTabs(dimensions);
	renderMode();
}

function renderStatus() {
	const snapshotCount = state.snapshot ? `${arrayOr(state.snapshot.biomes).length} biomes, ${arrayOr(state.snapshot.crops).length} crops` : "vanilla fallback lists";
	const helperStatus = state.helper.available ? `connected to ${state.helper.configDirectory}` : state.helper.message;
	refs.statusText.textContent = `${state.status} - ${snapshotCount} - ${helperStatus}`;
}

function renderSettings(dimensions) {
	renderHelperControls();
	setOptions(refs.dimensionSelect, dimensions, state.selectedDimension);
	const copyTargets = dimensions.filter((dimension) => dimension !== state.selectedDimension);
	setOptions(refs.copyDimensionSelect, copyTargets, copyTargets[0] ?? "");
	refs.copyDimensionSelect.disabled = copyTargets.length === 0;
	refs.copyDimensionRules.disabled = copyTargets.length === 0;
	refs.modeSelect.value = currentMode();
	refs.affectsBonemeal.checked = Boolean(state.general.affects_bonemeal);
	refs.affectsPlacement.checked = Boolean(state.general.affects_block_placement);
	refs.chatInfo.checked = Boolean(state.general.chat_info);
	refs.excludedBlocks.value = arrayOr(state.general.excluded_blocks).join(", ");
}

function renderValidation() {
	const messages = [
		...state.importMessages,
		...validateConfigBundle(state.general, state.explicit, state.threshold, state.snapshot)
	];
	refs.validationList.hidden = messages.length === 0;
	if (messages.length === 0) {
		refs.validationList.replaceChildren();
		return;
	}

	const visibleMessages = messages.slice(0, 10);
	const items = visibleMessages.map((message) => {
		const div = document.createElement("div");
		div.className = "validation-item";
		div.textContent = message;
		return div;
	});
	if (messages.length > visibleMessages.length) {
		const div = document.createElement("div");
		div.className = "validation-item";
		div.textContent = `${messages.length - visibleMessages.length} more validation messages`;
		items.push(div);
	}
	refs.validationList.replaceChildren(...items);
}

function renderTabs(dimensions) {
	refs.dimensionTabs.replaceChildren(...dimensions.map((dimension) => {
		const button = document.createElement("button");
		button.type = "button";
		button.className = `tab${dimension === state.selectedDimension ? " active" : ""}`;
		button.textContent = dimension;
		button.title = dimension;
		button.addEventListener("click", () => {
			state.selectedDimension = dimension;
			render();
		});
		return button;
	}));
}

function renderMode() {
	const mode = currentMode();
	refs.explicitPanel.hidden = mode !== "explicit";
	refs.thresholdPanel.hidden = mode !== "threshold";
	if (mode === "explicit") {
		renderExplicitGrid();
	} else {
		renderThresholdGrid();
	}
}

function renderExplicitGrid() {
	const rules = ensureExplicitRules(state.selectedDimension);
	refs.explicitDefault.value = behaviorOr(rules.default_behavior);
	const crops = filteredCrops();
	const biomes = filteredBiomes();
	if (crops.length === 0 || biomes.length === 0) {
		refs.explicitGrid.replaceChildren(emptyState("No matching crops or biomes"));
		return;
	}

	const table = document.createElement("table");
	const thead = document.createElement("thead");
	const headRow = document.createElement("tr");
	headRow.appendChild(th("Crop / Biome", "crop-head"));
	for (const biome of biomes) {
		const cell = th("", "biome-head");
		const button = idButton(biome.id, () => cycleExplicitColumn(rules, biome.id, crops, biomes));
		button.title = `${biome.id} - apply next behavior to this biome`;
		cell.appendChild(button);
		headRow.appendChild(cell);
	}
	thead.appendChild(headRow);

	const tbody = document.createElement("tbody");
	for (const crop of crops) {
		const row = document.createElement("tr");
		const cropHeader = th("", "crop-head");
		const cropButton = idButton(crop, () => cycleExplicitRow(rules, crop, biomes));
		cropButton.title = `${crop} - apply next behavior to this crop`;
		cropHeader.appendChild(cropButton);
		row.appendChild(cropHeader);

		for (const biome of biomes) {
			const behavior = explicitBehavior(rules, crop, biome.id);
			const cell = document.createElement("td");
			const button = behaviorButton(behavior);
			button.addEventListener("click", () => {
				setExplicitCell(rules, crop, biome.id, cycleBehavior(behavior));
				renderExplicitGrid();
			});
			cell.appendChild(button);
			row.appendChild(cell);
		}
		tbody.appendChild(row);
	}
	table.append(thead, tbody);
	refs.explicitGrid.replaceChildren(table);
}

function renderThresholdGrid() {
	const rules = ensureThresholdRules(state.selectedDimension);
	refs.thresholdDefault.value = behaviorOr(rules.default_rule.default_behavior);
	const crops = filteredCrops();
	const biomes = filteredBiomes();
	if (crops.length === 0) {
		refs.thresholdGrid.replaceChildren(emptyState("No matching crops"));
		return;
	}

	const table = document.createElement("table");
	table.className = "threshold-grid";
	const thead = document.createElement("thead");
	const headRow = document.createElement("tr");
	["Crop", "Default", "Min", "Max", "Precipitation", "Behavior", "Reset"].forEach((label, index) => {
		headRow.appendChild(th(label, index === 0 ? "threshold-crop" : "threshold-control"));
	});
	for (const biome of biomes) {
		headRow.appendChild(th(shortId(biome.id), "preview-cell"));
	}
	thead.appendChild(headRow);

	const tbody = document.createElement("tbody");
	for (const crop of crops) {
		const row = document.createElement("tr");
		const rule = thresholdRuleFor(rules, crop);
		const climateRule = displayClimateRule(rule);

		const cropCell = th("", "threshold-crop");
		const cropButton = idButton(crop, () => {
			ensureThresholdCropRule(rules, crop).default_behavior = cycleBehavior(thresholdRuleFor(rules, crop).default_behavior);
			renderThresholdGrid();
		});
		cropButton.title = `${crop} - cycle crop default`;
		cropCell.appendChild(cropButton);
		row.appendChild(cropCell);

		row.appendChild(controlCell(behaviorSelect(rule.default_behavior, (value) => {
			ensureThresholdCropRule(rules, crop).default_behavior = value;
			renderThresholdGrid();
		})));
		row.appendChild(controlCell(numberInput(climateRule.min_temperature, (value) => {
			firstClimateRule(ensureThresholdCropRule(rules, crop)).min_temperature = value;
			renderThresholdGrid();
		})));
		row.appendChild(controlCell(numberInput(climateRule.max_temperature, (value) => {
			firstClimateRule(ensureThresholdCropRule(rules, crop)).max_temperature = value;
			renderThresholdGrid();
		})));
		row.appendChild(controlCell(requirementSelect(climateRule.precipitation, (value) => {
			firstClimateRule(ensureThresholdCropRule(rules, crop)).precipitation = value;
			renderThresholdGrid();
		})));
		row.appendChild(controlCell(behaviorSelect(climateRule.behavior, (value) => {
			firstClimateRule(ensureThresholdCropRule(rules, crop)).behavior = value;
			renderThresholdGrid();
		})));

		const resetCell = document.createElement("td");
		const resetButton = document.createElement("button");
		resetButton.type = "button";
		resetButton.className = "small-action";
		resetButton.textContent = "Reset";
		resetButton.addEventListener("click", () => {
			delete rules.crops[crop];
			renderThresholdGrid();
		});
		resetCell.appendChild(resetButton);
		row.appendChild(resetCell);

		for (const biome of biomes) {
			const preview = document.createElement("td");
			preview.className = "preview-cell";
			const behavior = resolveThresholdBehavior(rule, biome);
			const badge = document.createElement("span");
			badge.className = `preview-badge state-${behavior}`;
			badge.textContent = BEHAVIOR_TOKENS[behavior];
			badge.title = `${BEHAVIOR_LABELS[behavior]} in ${biome.id}`;
			preview.appendChild(badge);
			row.appendChild(preview);
		}
		tbody.appendChild(row);
	}
	table.append(thead, tbody);
	refs.thresholdGrid.replaceChildren(table);
}

function currentMode() {
	return modeOr(state.general.dimensions?.[state.selectedDimension] ?? state.general.fallback_mode);
}

function ensureModeRules(dimension, mode) {
	if (mode === "explicit") {
		ensureExplicitRules(dimension);
	} else {
		ensureThresholdRules(dimension);
	}
}

function ensureExplicitRules(dimension) {
	state.explicit.dimensions ??= {};
	state.explicit.dimensions[dimension] ??= {
		default_behavior: state.explicit.fallback?.default_behavior ?? "bonemeal-required",
		biomes: {}
	};
	state.explicit.dimensions[dimension].biomes ??= {};
	state.explicit.dimensions[dimension].default_behavior = behaviorOr(state.explicit.dimensions[dimension].default_behavior);
	return state.explicit.dimensions[dimension];
}

function ensureThresholdRules(dimension) {
	state.threshold.dimensions ??= {};
	state.threshold.dimensions[dimension] ??= {
		default_rule: clone(state.threshold.fallback?.default_rule ?? DEFAULT_THRESHOLD.fallback.default_rule),
		crops: {}
	};
	state.threshold.dimensions[dimension].default_rule = normalizeThresholdRule(state.threshold.dimensions[dimension].default_rule);
	state.threshold.dimensions[dimension].crops ??= {};
	return state.threshold.dimensions[dimension];
}

function ensureThresholdCropRule(rules, crop) {
	rules.crops ??= {};
	rules.crops[crop] ??= normalizeThresholdRule(rules.default_rule);
	rules.crops[crop] = normalizeThresholdRule(rules.crops[crop]);
	return rules.crops[crop];
}

function thresholdRuleFor(rules, crop) {
	return normalizeThresholdRule(rules.crops?.[crop] ?? rules.default_rule);
}

function firstClimateRule(rule) {
	rule.climate_rules ??= [];
	if (rule.climate_rules.length === 0) {
		rule.climate_rules.push({
			min_temperature: numberOr(refs.bulkMin?.value, 0.15),
			max_temperature: numberOr(refs.bulkMax?.value, 1.5),
			precipitation: requirementOr(refs.bulkPrecipitation?.value ?? "required"),
			behavior: behaviorOr(refs.bulkBehavior?.value ?? "growable")
		});
	}
	return rule.climate_rules[0];
}

function displayClimateRule(rule) {
	return rule.climate_rules?.[0] ?? {
		min_temperature: numberOr(refs.bulkMin?.value, 0.15),
		max_temperature: numberOr(refs.bulkMax?.value, 1.5),
		precipitation: requirementOr(refs.bulkPrecipitation?.value ?? "required"),
		behavior: behaviorOr(refs.bulkBehavior?.value ?? "growable")
	};
}

function resetDimensionRules(dimension) {
	const mode = currentMode();
	if (mode === "explicit") {
		delete state.explicit.dimensions?.[dimension];
		ensureExplicitRules(dimension);
	} else {
		delete state.threshold.dimensions?.[dimension];
		ensureThresholdRules(dimension);
	}
	state.importMessages = [];
	state.status = `${MODE_LABELS[mode]} reset for ${dimension}`;
}

function applyThresholdTemplateToVisible() {
	const rules = ensureThresholdRules(state.selectedDimension);
	const crops = filteredCrops();
	for (const crop of crops) {
		rules.crops[crop] = {
			default_behavior: behaviorOr(refs.thresholdDefault.value),
			climate_rules: [{
				min_temperature: numberOr(refs.bulkMin.value, 0.15),
				max_temperature: numberOr(refs.bulkMax.value, 1.5),
				precipitation: requirementOr(refs.bulkPrecipitation.value),
				behavior: behaviorOr(refs.bulkBehavior.value)
			}]
		};
	}
	state.importMessages = [];
	state.status = `Updated ${crops.length} Threshold mode crop rules`;
	renderStatus();
}

function explicitBehavior(rules, crop, biome) {
	return behaviorOr(rules.biomes?.[biome]?.[crop] ?? rules.default_behavior);
}

function setExplicitCell(rules, crop, biome, behavior) {
	rules.biomes ??= {};
	rules.biomes[biome] ??= {};
	rules.biomes[biome][crop] = behaviorOr(behavior);
}

function cycleExplicitRow(rules, crop, biomes) {
	const next = cycleBehavior(explicitBehavior(rules, crop, biomes[0]?.id));
	for (const biome of biomes) {
		setExplicitCell(rules, crop, biome.id, next);
	}
	renderExplicitGrid();
}

function cycleExplicitColumn(rules, biome, crops) {
	const next = cycleBehavior(explicitBehavior(rules, crops[0], biome));
	for (const crop of crops) {
		setExplicitCell(rules, crop, biome, next);
	}
	renderExplicitGrid();
}

function filteredCrops() {
	const crops = collectCrops(state.explicit, state.threshold, state.snapshot);
	return state.cropFilter ? crops.filter((crop) => crop.toLowerCase().includes(state.cropFilter)) : crops;
}

function filteredBiomes() {
	const biomes = collectBiomes(state.explicit, state.snapshot);
	return state.biomeFilter ? biomes.filter((biome) => biome.id.toLowerCase().includes(state.biomeFilter)) : biomes;
}

function fillBehaviorSelect(select) {
	setOptions(select, BEHAVIORS, "bonemeal-required", BEHAVIOR_LABELS);
}

function fillRequirementSelect(select) {
	setOptions(select, PRECIPITATION_REQUIREMENTS, "required", PRECIPITATION_LABELS);
}

function behaviorSelect(value, onChange) {
	const select = document.createElement("select");
	setOptions(select, BEHAVIORS, behaviorOr(value), BEHAVIOR_LABELS);
	select.addEventListener("change", () => onChange(select.value));
	return select;
}

function requirementSelect(value, onChange) {
	const select = document.createElement("select");
	setOptions(select, PRECIPITATION_REQUIREMENTS, requirementOr(value), PRECIPITATION_LABELS);
	select.addEventListener("change", () => onChange(select.value));
	return select;
}

function numberInput(value, onChange) {
	const input = document.createElement("input");
	input.type = "number";
	input.step = "0.05";
	input.value = numberOr(value, 0).toFixed(2);
	input.addEventListener("change", () => onChange(numberOr(input.value, 0)));
	return input;
}

function behaviorButton(behavior) {
	const button = document.createElement("button");
	button.type = "button";
	button.className = `behavior-cell state-${behavior}`;
	button.textContent = BEHAVIOR_TOKENS[behavior];
	button.title = BEHAVIOR_LABELS[behavior];
	return button;
}

function idButton(value, onClick) {
	const button = document.createElement("button");
	button.type = "button";
	button.className = "id-button";
	button.textContent = value;
	button.addEventListener("click", onClick);
	return button;
}

function th(text, className) {
	const cell = document.createElement("th");
	cell.scope = "col";
	cell.className = className;
	cell.textContent = text;
	return cell;
}

function controlCell(control) {
	const cell = document.createElement("td");
	cell.appendChild(control);
	return cell;
}

function emptyState(text) {
	const div = document.createElement("div");
	div.className = "empty-state";
	div.textContent = text;
	return div;
}

function setOptions(select, values, selectedValue, labels = {}) {
	select.replaceChildren(...values.map((value) => {
		const option = document.createElement("option");
		option.value = value;
		option.textContent = labels[value] ?? value;
		return option;
	}));
	select.value = values.includes(selectedValue) ? selectedValue : values[0] ?? "";
}

function downloadJson(fileName, value) {
	const blob = new Blob([`${JSON.stringify(value, null, 2)}\n`], { type: "application/json" });
	const url = URL.createObjectURL(blob);
	const link = document.createElement("a");
	link.href = url;
	link.download = fileName;
	document.body.appendChild(link);
	link.click();
	link.remove();
	URL.revokeObjectURL(url);
}

function downloadAllConfig() {
	downloadJson(FILE_NAMES.general, state.general);
	downloadJson(FILE_NAMES.explicit, state.explicit);
	downloadJson(FILE_NAMES.threshold, state.threshold);
}

function validateLoadedFile(name, json) {
	const lowerName = name.toLowerCase();
	if (lowerName === FILE_NAMES.general) {
		return validateConfigBundle(json, null, null, null).filter((message) => message.startsWith("general.json"));
	}
	if (lowerName === FILE_NAMES.explicit) {
		return validateConfigBundle(null, json, null, null).filter((message) => message.startsWith("explicit-mode.json"));
	}
	if (lowerName === FILE_NAMES.threshold) {
		return validateConfigBundle(null, null, json, null).filter((message) => message.startsWith("threshold-mode.json"));
	}
	if (lowerName === FILE_NAMES.snapshot || json?.config_schema || json?.counts) {
		return validateConfigBundle(null, null, null, json);
	}
	return [];
}

function validateGeneral(general, messages) {
	if (general == null) {
		return;
	}
	if (!isObject(general)) {
		messages.push("general.json: expected an object");
		return;
	}
	validateSchema("general.json", general, messages);
	if (general.fallback_mode !== undefined && !MODES.includes(general.fallback_mode)) {
		messages.push(`general.json: fallback_mode '${general.fallback_mode}' is not a known mode`);
	}
	if (general.dimensions !== undefined && !isObject(general.dimensions)) {
		messages.push("general.json: dimensions must be an object");
		return;
	}
	for (const [dimension, mode] of Object.entries(general.dimensions ?? {})) {
		if (!MODES.includes(mode)) {
			messages.push(`general.json: ${dimension} uses unknown mode '${mode}'`);
		}
	}
	if (general.excluded_blocks !== undefined && !Array.isArray(general.excluded_blocks)) {
		messages.push("general.json: excluded_blocks must be an array");
	}
}

function validateExplicit(explicit, messages) {
	if (explicit == null) {
		return;
	}
	if (!isObject(explicit)) {
		messages.push("explicit-mode.json: expected an object");
		return;
	}
	validateSchema("explicit-mode.json", explicit, messages);
	validateExplicitRules("explicit-mode.json fallback", explicit.fallback, messages);
	if (explicit.dimensions !== undefined && !isObject(explicit.dimensions)) {
		messages.push("explicit-mode.json: dimensions must be an object");
		return;
	}
	for (const [dimension, rules] of Object.entries(explicit.dimensions ?? {})) {
		validateExplicitRules(`explicit-mode.json ${dimension}`, rules, messages);
	}
}

function validateExplicitRules(path, rules, messages) {
	if (rules == null) {
		return;
	}
	if (!isObject(rules)) {
		messages.push(`${path}: rules must be an object`);
		return;
	}
	validateBehavior(path, "default_behavior", rules.default_behavior, messages);
	if (rules.biomes !== undefined && !isObject(rules.biomes)) {
		messages.push(`${path}: biomes must be an object`);
		return;
	}
	for (const [biome, cropRules] of Object.entries(rules.biomes ?? {})) {
		if (!isObject(cropRules)) {
			messages.push(`${path}: ${biome} crop rules must be an object`);
			continue;
		}
		for (const [crop, behavior] of Object.entries(cropRules)) {
			validateBehavior(path, `${biome} / ${crop}`, behavior, messages);
		}
	}
}

function validateThreshold(threshold, messages) {
	if (threshold == null) {
		return;
	}
	if (!isObject(threshold)) {
		messages.push("threshold-mode.json: expected an object");
		return;
	}
	validateSchema("threshold-mode.json", threshold, messages);
	validateThresholdRules("threshold-mode.json fallback", threshold.fallback, messages);
	if (threshold.dimensions !== undefined && !isObject(threshold.dimensions)) {
		messages.push("threshold-mode.json: dimensions must be an object");
		return;
	}
	for (const [dimension, rules] of Object.entries(threshold.dimensions ?? {})) {
		validateThresholdRules(`threshold-mode.json ${dimension}`, rules, messages);
	}
}

function validateThresholdRules(path, rules, messages) {
	if (rules == null) {
		return;
	}
	if (!isObject(rules)) {
		messages.push(`${path}: rules must be an object`);
		return;
	}
	validateThresholdRule(`${path} default_rule`, rules.default_rule, messages);
	if (rules.crops !== undefined && !isObject(rules.crops)) {
		messages.push(`${path}: crops must be an object`);
		return;
	}
	for (const [crop, rule] of Object.entries(rules.crops ?? {})) {
		validateThresholdRule(`${path} ${crop}`, rule, messages);
	}
}

function validateThresholdRule(path, rule, messages) {
	if (rule == null) {
		return;
	}
	if (!isObject(rule)) {
		messages.push(`${path}: rule must be an object`);
		return;
	}
	validateBehavior(path, "default_behavior", rule.default_behavior, messages);
	if (rule.climate_rules !== undefined && !Array.isArray(rule.climate_rules)) {
		messages.push(`${path}: climate_rules must be an array`);
		return;
	}
	for (const [index, climateRule] of arrayOr(rule.climate_rules).entries()) {
		validateClimateRule(`${path} climate_rules[${index}]`, climateRule, messages);
	}
}

function validateClimateRule(path, climateRule, messages) {
	if (!isObject(climateRule)) {
		messages.push(`${path}: climate rule must be an object`);
		return;
	}
	const min = Number(climateRule.min_temperature);
	const max = Number(climateRule.max_temperature);
	if (!Number.isFinite(min)) {
		messages.push(`${path}: min_temperature must be a number`);
	}
	if (!Number.isFinite(max)) {
		messages.push(`${path}: max_temperature must be a number`);
	}
	if (Number.isFinite(min) && Number.isFinite(max) && min >= max) {
		messages.push(`${path}: min_temperature must be lower than max_temperature`);
	}
	if (!PRECIPITATION_REQUIREMENTS.includes(climateRule.precipitation)) {
		messages.push(`${path}: precipitation '${climateRule.precipitation}' is not valid`);
	}
	validateBehavior(path, "behavior", climateRule.behavior, messages);
}

function validateSnapshot(snapshot, messages) {
	if (snapshot == null) {
		return;
	}
	if (!isObject(snapshot)) {
		messages.push("registry snapshot: expected an object");
		return;
	}
	for (const key of ["dimensions", "biomes", "crops"]) {
		if (snapshot[key] !== undefined && !Array.isArray(snapshot[key])) {
			messages.push(`registry snapshot: ${key} must be an array`);
		}
	}
	if (isObject(snapshot.counts)) {
		validateSnapshotCount(snapshot, "dimensions", messages);
		validateSnapshotCount(snapshot, "biomes", messages);
		validateSnapshotCount(snapshot, "crops", messages);
	}
}

function validateSnapshotCount(snapshot, key, messages) {
	const expected = Number(snapshot.counts?.[key]);
	if (Number.isFinite(expected) && Array.isArray(snapshot[key]) && expected !== snapshot[key].length) {
		messages.push(`registry snapshot: ${key} count is ${expected}, but ${snapshot[key].length} entries were loaded`);
	}
}

function validateSchema(fileName, value, messages) {
	if (value.schema_version !== undefined && value.schema_version !== 1) {
		messages.push(`${fileName}: schema_version ${value.schema_version} is not supported`);
	}
}

function validateBehavior(path, field, value, messages) {
	if (value !== undefined && !BEHAVIORS.includes(value)) {
		messages.push(`${path}: ${field} '${value}' is not a known behavior`);
	}
}

function normalizeGeneral(value) {
	return {
		schema_version: 1,
		affects_bonemeal: booleanOr(value?.affects_bonemeal, true),
		affects_block_placement: booleanOr(value?.affects_block_placement, true),
		chat_info: booleanOr(value?.chat_info, true),
		excluded_blocks: arrayOr(value?.excluded_blocks).filter((id) => typeof id === "string"),
		fallback_mode: modeOr(value?.fallback_mode),
		dimensions: normalizeModeMap(value?.dimensions)
	};
}

function normalizeExplicit(value) {
	return {
		schema_version: 1,
		fallback: normalizeExplicitRules(value?.fallback),
		dimensions: normalizeMap(value?.dimensions, normalizeExplicitRules)
	};
}

function normalizeExplicitRules(value) {
	const biomes = {};
	for (const [biome, cropRules] of Object.entries(value?.biomes ?? {})) {
		biomes[biome] = normalizeBehaviorMap(cropRules);
	}
	return {
		default_behavior: behaviorOr(value?.default_behavior),
		biomes
	};
}

function normalizeThreshold(value) {
	return {
		schema_version: 1,
		fallback: normalizeThresholdRules(value?.fallback),
		dimensions: normalizeMap(value?.dimensions, normalizeThresholdRules)
	};
}

function normalizeThresholdRules(value) {
	return {
		default_rule: normalizeThresholdRule(value?.default_rule),
		crops: normalizeMap(value?.crops, normalizeThresholdRule)
	};
}

function normalizeThresholdRule(value) {
	return {
		default_behavior: behaviorOr(value?.default_behavior),
		climate_rules: arrayOr(value?.climate_rules).map(normalizeClimateRule)
	};
}

function normalizeClimateRule(value) {
	return {
		min_temperature: numberOr(value?.min_temperature, -1000),
		max_temperature: numberOr(value?.max_temperature, 1000),
		precipitation: requirementOr(value?.precipitation),
		behavior: behaviorOr(value?.behavior)
	};
}

function normalizeModeMap(value) {
	const normalized = {};
	for (const [key, mode] of Object.entries(value ?? DEFAULT_GENERAL.dimensions)) {
		normalized[key] = modeOr(mode);
	}
	return normalized;
}

function normalizeBehaviorMap(value) {
	const normalized = {};
	for (const [key, behavior] of Object.entries(value ?? {})) {
		normalized[key] = behaviorOr(behavior);
	}
	return normalized;
}

function normalizeMap(value, mapper) {
	const normalized = {};
	for (const [key, inner] of Object.entries(value ?? {})) {
		normalized[key] = mapper(inner);
	}
	return normalized;
}

function normalizeBiome(value) {
	if (typeof value === "string") {
		return { id: value, temperature: null, has_precipitation: false };
	}
	if (typeof value?.id !== "string") {
		return null;
	}
	return {
		id: value.id,
		temperature: Number.isFinite(Number(value.temperature)) ? Number(value.temperature) : null,
		has_precipitation: Boolean(value.has_precipitation)
	};
}

function collectExplicitCrops(values, rules) {
	for (const cropRules of Object.values(rules?.biomes ?? {})) {
		addKeys(values, cropRules);
	}
}

function collectExplicitBiomes(values, rules) {
	for (const biome of Object.keys(rules?.biomes ?? {})) {
		if (!values.has(biome)) {
			values.set(biome, { id: biome, temperature: null, has_precipitation: false });
		}
	}
}

function precipitationMatches(requirement, hasPrecipitation) {
	return switchString(requirementOr(requirement), {
		required: hasPrecipitation,
		forbidden: !hasPrecipitation,
		ignored: true
	});
}

function switchString(key, cases) {
	return Object.prototype.hasOwnProperty.call(cases, key) ? cases[key] : cases.ignored;
}

function behaviorOr(value, fallback = "bonemeal-required") {
	return BEHAVIORS.includes(value) ? value : fallback;
}

function modeOr(value) {
	return MODES.includes(value) ? value : "threshold";
}

function requirementOr(value) {
	return PRECIPITATION_REQUIREMENTS.includes(value) ? value : "ignored";
}

function booleanOr(value, fallback) {
	return typeof value === "boolean" ? value : fallback;
}

function numberOr(value, fallback) {
	const number = Number(value);
	return Number.isFinite(number) ? number : fallback;
}

function arrayOr(value) {
	return Array.isArray(value) ? value : [];
}

function isObject(value) {
	return Boolean(value) && typeof value === "object" && !Array.isArray(value);
}

function addKeys(values, object) {
	for (const key of Object.keys(object ?? {})) {
		values.add(key);
	}
}

function sortIds(values) {
	return [...values].sort((left, right) => left.localeCompare(right));
}

function splitIds(value) {
	return value.split(",")
		.map((item) => item.trim())
		.filter(Boolean);
}

function shortId(value) {
	return value.includes(":") ? value.split(":")[1] : value;
}

function clone(value) {
	return JSON.parse(JSON.stringify(value));
}

if (typeof document !== "undefined") {
	init();
}
