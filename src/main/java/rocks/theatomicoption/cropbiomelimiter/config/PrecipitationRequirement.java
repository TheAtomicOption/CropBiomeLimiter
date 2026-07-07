package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Locale;
import java.util.Optional;

public enum PrecipitationRequirement {
	REQUIRED("required"),
	FORBIDDEN("forbidden"),
	IGNORED("ignored");

	private final String serializedName;

	PrecipitationRequirement(String serializedName) {
		this.serializedName = serializedName;
	}

	public String serializedName() {
		return serializedName;
	}

	public boolean matches(boolean hasPrecipitation) {
		return switch (this) {
			case REQUIRED -> hasPrecipitation;
			case FORBIDDEN -> !hasPrecipitation;
			case IGNORED -> true;
		};
	}

	public static PrecipitationRequirement fromSerializedName(String value) {
		return tryFromSerializedName(value).orElse(IGNORED);
	}

	public static Optional<PrecipitationRequirement> tryFromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		for (PrecipitationRequirement requirement : values()) {
			if (requirement.serializedName.equals(normalized)) {
				return Optional.of(requirement);
			}
		}
		return Optional.empty();
	}
}