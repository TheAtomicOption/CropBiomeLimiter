package rocks.theatomicoption.cropbiomelimiter.viewer.rei;

import java.util.ArrayList;
import java.util.List;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.RuleMode;
import rocks.theatomicoption.cropbiomelimiter.viewer.AtlasBiome;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasComponents;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasItemStacks;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.TemperatureScale;

public final class ReiCropAtlasCategory implements DisplayCategory<ReiCropAtlasDisplay> {
	private static final int WIDTH = 168;
	private static final int HEIGHT = 148;
	private static final int TEXT_COLOR = 0xFF404040;

	@Override
	public CategoryIdentifier<? extends ReiCropAtlasDisplay> getCategoryIdentifier() {
		return ReiCropAtlasDisplay.CATEGORY;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("viewer.cropbiomelimiter.category.crop_atlas");
	}

	@Override
	public Renderer getIcon() {
		return EntryStacks.of(Items.WHEAT_SEEDS);
	}

	@Override
	public int getDisplayWidth(ReiCropAtlasDisplay display) {
		return WIDTH;
	}

	@Override
	public int getDisplayHeight() {
		return HEIGHT;
	}

	@Override
	public List<Widget> setupDisplay(ReiCropAtlasDisplay display, Rectangle bounds) {
		var page = display.page();
		var entry = page.entry();
		int x = bounds.x + 4;
		int y = bounds.y + 4;
		List<Widget> widgets = new ArrayList<>();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createSlot(new Point(x, y))
				.entry(EntryStacks.of(AtlasItemStacks.crop(entry.cropId())))
				.markInput());
		widgets.add(label(x + 22, y + 2, AtlasItemStacks.crop(entry.cropId()).getHoverName()));
		widgets.add(label(x + 22, y + 13, AtlasComponents.dimensionName(entry.dimension())));

		int groupY = entry.mode() == RuleMode.THRESHOLD ? y + 54 : y + 28;
		if (entry.mode() == RuleMode.THRESHOLD) {
			Component temperature = AtlasComponents.temperatureSummary(entry.climateRules());
			widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) ->
					TemperatureScale.drawClimateValueBackground(
							graphics, x, y + 25, 164, 13, temperature, entry.climateRules(), entry.temperatureDomain()
					)));
			widgets.add(label(x, y + 28, TemperatureScale.label()));
			widgets.add(label(TemperatureScale.valueX(x), y + 28, temperature));
			widgets.add(label(x, y + 40, Component.translatable(
					"viewer.cropbiomelimiter.precipitation",
					AtlasComponents.precipitationSummary(entry.climateRules())
			)));
			List<Component> tooltip = entry.climateRules().isEmpty()
					? List.of(Component.translatable("viewer.cropbiomelimiter.no_climate_ranges"))
					: entry.climateRules().stream().map(AtlasComponents::climateRule).toList();
			widgets.add(Widgets.createTooltip(new Rectangle(x, y + 25, 164, 25), tooltip));
		}

		for (CropBehavior behavior : CropBehavior.values()) {
			List<AtlasBiome> biomes = page.results().valuesByBehavior().get(behavior);
			widgets.add(label(x, groupY, AtlasComponents.behaviorWithCount(behavior, page.results().total(behavior))));
			for (int column = 0; column < biomes.size(); column++) {
				widgets.add(Widgets.createSlot(new Point(x + column * 20, groupY + 10))
						.entry(EntryStacks.of(AtlasItemStacks.biome(biomes.get(column), entry.temperatureDomain()))));
			}
			groupY += 31;
		}
		return widgets;
	}

	private static Widget label(int x, int y, Component text) {
		return Widgets.createLabel(new Point(x, y), text)
				.leftAligned()
				.noShadow()
				.color(TEXT_COLOR);
	}
}
