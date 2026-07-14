package rocks.theatomicoption.cropbiomelimiter.viewer.jei;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;
import rocks.theatomicoption.cropbiomelimiter.config.RuleMode;
import rocks.theatomicoption.cropbiomelimiter.viewer.BiomeAtlasEntry;
import rocks.theatomicoption.cropbiomelimiter.viewer.BiomeAtlasPage;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasComponents;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.AtlasItemStacks;
import rocks.theatomicoption.cropbiomelimiter.viewer.client.TemperatureScale;

public final class JeiBiomeAtlasCategory implements IRecipeCategory<BiomeAtlasPage> {
	private static final int WIDTH = 168;
	private static final int HEIGHT = 148;
	private final IDrawable icon;

	public JeiBiomeAtlasCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableItemLike(Items.COMPASS);
	}

	@Override
	public IRecipeType<BiomeAtlasPage> getRecipeType() {
		return CropBiomeLimiterJeiPlugin.BIOME_ATLAS;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("viewer.cropbiomelimiter.category.biome_atlas");
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
	public void setRecipe(IRecipeLayoutBuilder builder, BiomeAtlasPage page, IFocusGroup focuses) {
		BiomeAtlasEntry entry = page.entry();
		builder.addInputSlot(0, 0)
				.setStandardSlotBackground()
				.add(AtlasItemStacks.biome(entry.biome(), entry.temperatureDomain()));
		int groupY = entry.mode() == RuleMode.THRESHOLD ? 54 : 28;
		for (CropBehavior behavior : CropBehavior.values()) {
			addCropSlots(builder, page.results().valuesByBehavior().get(behavior), groupY + 10);
			groupY += 31;
		}
	}

	private static void addCropSlots(IRecipeLayoutBuilder builder, List<Identifier> crops, int y) {
		for (int column = 0; column < crops.size(); column++) {
			builder.addOutputSlot(column * 20, y)
					.setStandardSlotBackground()
					.add(AtlasItemStacks.crop(crops.get(column)));
		}
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, BiomeAtlasPage page, IFocusGroup focuses) {
		BiomeAtlasEntry entry = page.entry();
		addText(builder, AtlasComponents.biomeName(entry.biome().id()), 22, 1);
		addText(builder, AtlasComponents.dimensionName(entry.dimension()), 22, 12);
		int groupY = entry.mode() == RuleMode.THRESHOLD ? 54 : 28;
		if (entry.mode() == RuleMode.THRESHOLD) {
			Component temperature = AtlasComponents.temperatureValue(entry.biome().temperature());
			int valueX = TemperatureScale.valueX(0);
			addText(builder, TemperatureScale.label(), 0, 28);
			addText(builder, temperature, valueX, 28);
			addText(builder, Component.translatable(
					"viewer.cropbiomelimiter.precipitation",
					AtlasComponents.precipitationValue(entry.biome().hasPrecipitation())
			), 0, 40);
		}
		for (CropBehavior behavior : CropBehavior.values()) {
			addText(builder, AtlasComponents.behaviorWithCount(
					behavior,
					page.results().total(behavior)
			), 0, groupY);
			groupY += 31;
		}
	}

	@Override
	public void draw(
			BiomeAtlasPage page,
			IRecipeSlotsView recipeSlotsView,
			GuiGraphicsExtractor guiGraphics,
			double mouseX,
			double mouseY
	) {
		BiomeAtlasEntry entry = page.entry();
		if (entry.mode() == RuleMode.THRESHOLD) {
			TemperatureScale.drawValueBackground(
					guiGraphics,
					0,
					25,
					164,
					13,
					AtlasComponents.temperatureValue(entry.biome().temperature()),
					entry.biome().temperature(),
					entry.biome().temperature(),
					entry.temperatureDomain()
			);
		}
	}

	private static void addText(IRecipeExtrasBuilder builder, Component text, int x, int y) {
		builder.addText(text, 164, 10)
				.setTextAlignment(HorizontalAlignment.LEFT)
				.setShadow(false)
				.setPosition(x, y);
	}

	@Override
	public Identifier getIdentifier(BiomeAtlasPage page) {
		BiomeAtlasEntry entry = page.entry();
		return CropBiomeLimiter.id("biome_atlas/"
				+ entry.dimension().identifier().getNamespace() + "/"
				+ entry.dimension().identifier().getPath() + "/"
				+ entry.biome().id().getNamespace() + "/"
				+ entry.biome().id().getPath() + "/page/"
				+ (page.results().index() + 1));
	}
}
