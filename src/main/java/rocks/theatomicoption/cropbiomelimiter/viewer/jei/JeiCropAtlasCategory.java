package rocks.theatomicoption.cropbiomelimiter.viewer.jei;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.RuleMode;
import rocks.theatomicoption.cropbiomelimiter.viewer.AtlasBiome;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropAtlasEntry;
import rocks.theatomicoption.cropbiomelimiter.viewer.CropAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.TemperatureDomain;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasComponents;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasItemStacks;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.TemperatureScale;

public final class JeiCropAtlasCategory implements IRecipeCategory<CropAtlasPage> {
	private static final int WIDTH = 168;
	private static final int HEIGHT = 148;
	private final IDrawable icon;

	public JeiCropAtlasCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableItemLike(Items.WHEAT_SEEDS);
	}

	@Override
	public RecipeType<CropAtlasPage> getRecipeType() {
		return CropBiomeLimiterJeiPlugin.CROP_ATLAS;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("viewer.cropbiomelimiter.category.crop_atlas");
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CropAtlasPage page, IFocusGroup focuses) {
		CropAtlasEntry entry = page.entry();
		builder.addInputSlot(0, 0)
				.setStandardSlotBackground()
				.addItemStack(AtlasItemStacks.crop(entry.cropId()));
		int groupY = entry.mode() == RuleMode.THRESHOLD ? 54 : 28;
		for (CropBehavior behavior : CropBehavior.values()) {
			addBiomeSlots(builder, page.results().valuesByBehavior().get(behavior), entry.temperatureDomain(), groupY + 10);
			groupY += 31;
		}
	}

	private static void addBiomeSlots(
			IRecipeLayoutBuilder builder,
			List<AtlasBiome> biomes,
			TemperatureDomain temperatureDomain,
			int y
	) {
		for (int column = 0; column < biomes.size(); column++) {
			builder.addSlot(RecipeIngredientRole.RENDER_ONLY, column * 20, y)
					.setStandardSlotBackground()
					.addItemStack(AtlasItemStacks.biome(biomes.get(column), temperatureDomain));
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, CropAtlasPage page, IFocusGroup focuses) {
		CropAtlasEntry entry = page.entry();
		addText(builder, AtlasItemStacks.crop(entry.cropId()).getHoverName(), 22, 1, 146, HorizontalAlignment.LEFT);
		addText(builder, AtlasComponents.dimensionName(entry.dimension()), 22, 12, 146, HorizontalAlignment.LEFT);
		int groupY = entry.mode() == RuleMode.THRESHOLD ? 54 : 28;
		if (entry.mode() == RuleMode.THRESHOLD) {
			Component temperature = AtlasComponents.temperatureSummary(entry.climateRules());
			int valueX = TemperatureScale.valueX(0);
			addText(builder, TemperatureScale.label(), 0, 28, valueX, HorizontalAlignment.LEFT);
			addText(builder, temperature, valueX, 28, 164 - valueX, HorizontalAlignment.LEFT);
			addText(builder, Component.translatable(
					"viewer.cropbiomelimiter.precipitation",
					AtlasComponents.precipitationSummary(entry.climateRules())
			), 0, 40, 164, HorizontalAlignment.LEFT);
		}
		for (CropBehavior behavior : CropBehavior.values()) {
			int count = page.results().total(behavior);
			addText(builder, AtlasComponents.behaviorWithCount(behavior, count), 0, groupY, 164, HorizontalAlignment.LEFT);
			groupY += 31;
		}
	}

	@Override
	public void draw(
			CropAtlasPage page,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphics guiGraphics,
			double mouseX,
			double mouseY
	) {
		CropAtlasEntry entry = page.entry();
		if (entry.mode() == RuleMode.THRESHOLD) {
			TemperatureScale.drawClimateValueBackground(
					guiGraphics,
					0,
					25,
					164,
					13,
					AtlasComponents.temperatureSummary(entry.climateRules()),
					entry.climateRules(),
					entry.temperatureDomain()
			);
		}
	}

	private static void addText(
			IRecipeExtrasBuilder builder,
			Component text,
			int x,
			int y,
			int width,
			HorizontalAlignment alignment
	) {
		builder.addText(text, width, 10)
				.setTextAlignment(alignment)
				.setShadow(false)
				.setPosition(x, y);
	}

	@Override
	public void getTooltip(
			ITooltipBuilder tooltip,
			CropAtlasPage page,
			IRecipeSlotsView recipeSlotsView,
			double mouseX,
			double mouseY
	) {
		CropAtlasEntry entry = page.entry();
		if (entry.mode() != RuleMode.THRESHOLD || mouseY < 24 || mouseY > 49) {
			return;
		}
		if (entry.climateRules().isEmpty()) {
			tooltip.add(Component.translatable("viewer.cropbiomelimiter.no_climate_ranges"));
			return;
		}
		for (ClimateRule rule : entry.climateRules()) {
			tooltip.add(AtlasComponents.climateRule(rule));
		}
	}

	@Override
	public ResourceLocation getRegistryName(CropAtlasPage page) {
		CropAtlasEntry entry = page.entry();
		return CropBiomeLimiter.id("crop_atlas/"
				+ entry.dimension().location().getNamespace() + "/"
				+ entry.dimension().location().getPath() + "/"
				+ entry.cropId().getNamespace() + "/"
				+ entry.cropId().getPath() + "/page/"
				+ (page.results().index() + 1));
	}
}
