package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Locale;
import java.util.Optional;

public enum RuleMode {
	EXPLICIT("explicit"),
	THRESHOLD("threshold");

	private final String serializedName;

	RuleMode(String serializedName) {
		this.serializedName = serializedName;
	}

	public String serializedName() {
		return serializedName;
	}

	public static RuleMode fromSerializedName(String value) {
		return tryFromSerializedName(value).orElse(THRESHOLD);
	}

	public static Optional<RuleMode> tryFromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		for (RuleMode mode : values()) {
			if (mode.serializedName.equals(normalized)) {
				return Optional.of(mode);
			}
		}
		return Optional.empty();
	}
}