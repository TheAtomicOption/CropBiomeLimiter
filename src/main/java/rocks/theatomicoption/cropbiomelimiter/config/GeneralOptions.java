package rocks.theatomicoption.cropbiomelimiter.config;

import java.util.Set;

import net.minecraft.resources.Identifier;

public record GeneralOptions(
		boolean affectsBonemeal,
		boolean affectsBlockPlacement,
		boolean affectsVillageFarmGeneration,
		boolean chatInfo,
		Set<Identifier> excludedBlocks
) {
	public static GeneralOptions defaults() {
		return new GeneralOptions(true, true, true, true, Set.of());
	}

	public boolean isExcluded(Identifier blockId) {
		return excludedBlocks.contains(blockId);
	}
}
