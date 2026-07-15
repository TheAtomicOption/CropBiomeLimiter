package rocks.theatomicoption.cropbiomelimiter.viewer.client;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import rocks.theatomicoption.cropbiomelimiter.config.ClimateRule;
import rocks.theatomicoption.cropbiomelimiter.viewer.TemperatureDomain;

public final class TemperatureScale {
	public static final int COLD_COLOR = 0xFF84BDE2;
	public static final int TEMPERATE_COLOR = 0xFF7DBB6D;
	public static final int HOT_COLOR = 0xFFE1A340;
	private static final int BORDER_COLOR = 0xFF5F5F5F;
	private static final int VALUE_GAP = 6;
	private static final int BACKGROUND_OUTSET = 4;
	private static final float TEMPERATE_STOP = 0.45F;
	private static final float COOL_TEXT_STOP = 1.0F / 3.0F;
	private static final float TEMPERATE_TEXT_STOP = 2.0F / 3.0F;

	private TemperatureScale() {
	}

	public static Component label() {
		return Component.translatable("viewer.cropbiomelimiter.temperature_label");
	}

	public static int valueX(int lineX) {
		return lineX + Minecraft.getInstance().font.width(label()) + VALUE_GAP;
	}

	public static void drawClimateValueBackground(
			GuiGraphics graphics,
			int lineX,
			int y,
			int lineWidth,
			int height,
			Component value,
			List<ClimateRule> rules,
			TemperatureDomain domain
	) {
		if (rules.isEmpty()) {
			return;
		}
		float minimum = rules.stream()
				.map(ClimateRule::minTemperatureInclusive)
				.min(Float::compare)
				.orElseThrow();
		float maximum = rules.stream()
				.map(ClimateRule::maxTemperatureExclusive)
				.max(Float::compare)
				.orElseThrow();
		drawValueBackground(graphics, lineX, y, lineWidth, height, value, minimum, maximum, domain);
	}

	public static void drawValueBackground(
			GuiGraphics graphics,
			int lineX,
			int y,
			int lineWidth,
			int height,
			Component value,
			float minimum,
			float maximum,
			TemperatureDomain domain
	) {
		int x = valueX(lineX) - BACKGROUND_OUTSET;
		int width = Math.min(
				Minecraft.getInstance().font.width(value) + BACKGROUND_OUTSET * 2,
				lineX + lineWidth - x
		);
		draw(graphics, x, y, width, height, minimum, maximum, domain);
	}

	public static void draw(
			GuiGraphics graphics,
			int x,
			int y,
			int width,
			int height,
			float minimum,
			float maximum,
			TemperatureDomain domain
	) {
		if (width <= 0 || height <= 0) {
			return;
		}
		for (int offset = 0; offset < width; offset++) {
			float amount = width == 1 ? 0.0F : offset / (float) (width - 1);
			float temperature = minimum + (maximum - minimum) * amount;
			graphics.fill(x + offset, y, x + offset + 1, y + height, colorAtTemperature(temperature, domain));
		}
		if (width >= 2 && height >= 2) {
			graphics.renderOutline(x, y, width, height, BORDER_COLOR);
		}
	}

	public static int colorAtTemperature(float temperature, TemperatureDomain domain) {
		return colorAt(domain.position(temperature));
	}

	public static ChatFormatting textColor(float temperature, TemperatureDomain domain) {
		float position = domain.position(temperature);
		if (position < COOL_TEXT_STOP) {
			return ChatFormatting.YELLOW;
		}
		if (position < TEMPERATE_TEXT_STOP) {
			return ChatFormatting.GREEN;
		}
		return ChatFormatting.GOLD;
	}

	public static int colorAt(float position) {
		float clamped = Math.max(0.0F, Math.min(1.0F, position));
		if (clamped <= TEMPERATE_STOP) {
			return interpolate(COLD_COLOR, TEMPERATE_COLOR, clamped / TEMPERATE_STOP);
		}
		return interpolate(TEMPERATE_COLOR, HOT_COLOR, (clamped - TEMPERATE_STOP) / (1.0F - TEMPERATE_STOP));
	}

	private static int interpolate(int start, int end, float amount) {
		int alpha = interpolateChannel(start >>> 24, end >>> 24, amount);
		int red = interpolateChannel(start >>> 16, end >>> 16, amount);
		int green = interpolateChannel(start >>> 8, end >>> 8, amount);
		int blue = interpolateChannel(start, end, amount);
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static int interpolateChannel(int start, int end, float amount) {
		return Math.round((start & 0xFF) + ((end & 0xFF) - (start & 0xFF)) * amount);
	}
}
