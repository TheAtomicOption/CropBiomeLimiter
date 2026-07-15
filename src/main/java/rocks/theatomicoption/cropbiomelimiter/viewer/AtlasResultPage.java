package rocks.theatomicoption.cropbiomelimiter.viewer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import rocks.theatomicoption.cropbiomelimiter.config.CropBehavior;

public record AtlasResultPage<T>(
		int index,
		int count,
		Map<CropBehavior, List<T>> valuesByBehavior,
		Map<CropBehavior, Integer> totalsByBehavior
) {
	public static final int SLOTS_PER_GROUP = 6;

	public AtlasResultPage {
		if (count <= 0 || index < 0 || index >= count) {
			throw new IllegalArgumentException("Atlas page index must be within its page count");
		}
		valuesByBehavior = copyValues(valuesByBehavior);
		totalsByBehavior = copyTotals(totalsByBehavior);
	}

	public int total(CropBehavior behavior) {
		return totalsByBehavior.getOrDefault(behavior, 0);
	}

	public static <T> List<AtlasResultPage<T>> paginate(Map<CropBehavior, List<T>> source) {
		int pageCount = 1;
		for (CropBehavior behavior : CropBehavior.values()) {
			int size = source.getOrDefault(behavior, List.of()).size();
			pageCount = Math.max(pageCount, (size + SLOTS_PER_GROUP - 1) / SLOTS_PER_GROUP);
		}

		EnumMap<CropBehavior, Integer> totals = new EnumMap<>(CropBehavior.class);
		for (CropBehavior behavior : CropBehavior.values()) {
			totals.put(behavior, source.getOrDefault(behavior, List.of()).size());
		}

		List<AtlasResultPage<T>> pages = new ArrayList<>(pageCount);
		for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
			EnumMap<CropBehavior, List<T>> values = new EnumMap<>(CropBehavior.class);
			for (CropBehavior behavior : CropBehavior.values()) {
				List<T> allValues = source.getOrDefault(behavior, List.of());
				int fromIndex = Math.min(pageIndex * SLOTS_PER_GROUP, allValues.size());
				int toIndex = Math.min(fromIndex + SLOTS_PER_GROUP, allValues.size());
				values.put(behavior, allValues.subList(fromIndex, toIndex));
			}
			pages.add(new AtlasResultPage<>(pageIndex, pageCount, values, totals));
		}
		return List.copyOf(pages);
	}

	private static <T> Map<CropBehavior, List<T>> copyValues(Map<CropBehavior, List<T>> source) {
		EnumMap<CropBehavior, List<T>> copy = new EnumMap<>(CropBehavior.class);
		for (CropBehavior behavior : CropBehavior.values()) {
			copy.put(behavior, List.copyOf(source.getOrDefault(behavior, List.of())));
		}
		return Map.copyOf(copy);
	}

	private static Map<CropBehavior, Integer> copyTotals(Map<CropBehavior, Integer> source) {
		EnumMap<CropBehavior, Integer> copy = new EnumMap<>(CropBehavior.class);
		for (CropBehavior behavior : CropBehavior.values()) {
			copy.put(behavior, source.getOrDefault(behavior, 0));
		}
		return Map.copyOf(copy);
	}
}
