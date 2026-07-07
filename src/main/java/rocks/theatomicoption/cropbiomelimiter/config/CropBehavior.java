package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Locale;
import java.util.Optional;

public enum CropBehavior {
	GROWABLE("growable", true, true, true),
	BONEMEAL_REQUIRED("bonemeal-required", true, false, true),
	UNPLANTABLE("unplantable", false, false, false);

	private final String serializedName;
	private final boolean allowsPlanting;
	private final boolean allowsNaturalGrowth;
	private final boolean allowsBonemeal;

	CropBehavior(String serializedName, boolean allowsPlanting, boolean allowsNaturalGrowth, boolean allowsBonemeal) {
		this.serializedName = serializedName;
		this.allowsPlanting = allowsPlanting;
		this.allowsNaturalGrowth = allowsNaturalGrowth;
		this.allowsBonemeal = allowsBonemeal;
	}

	public String serializedName() {
		return serializedName;
	}

	public boolean allowsPlanting() {
		return allowsPlanting;
	}

	public boolean allowsNaturalGrowth() {
		return allowsNaturalGrowth;
	}

	public boolean allowsBonemeal() {
		return allowsBonemeal;
	}

	public static CropBehavior fromSerializedName(String value) {
		return tryFromSerializedName(value).orElse(GROWABLE);
	}

	public static Optional<CropBehavior> tryFromSerializedName(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		for (CropBehavior behavior : values()) {
			if (behavior.serializedName.equals(normalized)) {
				return Optional.of(behavior);
			}
		}
		return Optional.empty();
	}
}