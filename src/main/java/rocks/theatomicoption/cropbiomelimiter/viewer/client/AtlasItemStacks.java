package rocks.theatomicoption.cropbiomelimiter.viewer.client;

import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rocks.theatomicoption.cropbiomelimiter.viewer.AtlasBiome;
import rocks.theatomicoption.cropbiomelimiter.viewer.TemperatureDomain;

public final class AtlasItemStacks {
	private static final Map<String, Item> SPECIAL_CROP_ITEMS = Map.ofEntries(
			Map.entry("wheat", Items.WHEAT_SEEDS),
			Map.entry("carrots", Items.CARROT),
			Map.entry("potatoes", Items.POTATO),
			Map.entry("beetroots", Items.BEETROOT_SEEDS),
			Map.entry("pumpkin_stem", Items.PUMPKIN_SEEDS),
			Map.entry("attached_pumpkin_stem", Items.PUMPKIN_SEEDS),
			Map.entry("melon_stem", Items.MELON_SEEDS),
			Map.entry("attached_melon_stem", Items.MELON_SEEDS),
			Map.entry("torchflower_crop", Items.TORCHFLOWER_SEEDS),
			Map.entry("pitcher_crop", Items.PITCHER_POD),
			Map.entry("sweet_berry_bush", Items.SWEET_BERRIES),
			Map.entry("cave_vines", Items.GLOW_BERRIES),
			Map.entry("cave_vines_plant", Items.GLOW_BERRIES),
			Map.entry("cocoa", Items.COCOA_BEANS),
			Map.entry("nether_wart", Items.NETHER_WART),
			Map.entry("tall_seagrass", Items.SEAGRASS),
			Map.entry("kelp_plant", Items.KELP),
			Map.entry("bamboo_sapling", Items.BAMBOO),
			Map.entry("weeping_vines_plant", Items.WEEPING_VINES),
			Map.entry("twisting_vines_plant", Items.TWISTING_VINES)
	);

	private AtlasItemStacks() {
	}

	public static ItemStack crop(ResourceLocation cropId) {
		Block block = BuiltInRegistries.BLOCK.get(cropId);
		if (block == null) {
			return named(new ItemStack(Items.BARRIER), AtlasComponents.cropName(cropId));
		}
		Item special = SPECIAL_CROP_ITEMS.get(cropId.getPath());
		Item item = special == null ? block.asItem() : special;
		if (item == Items.AIR) {
			return named(new ItemStack(Items.BARRIER), block.getName());
		}
		return new ItemStack(item);
	}

	public static ItemStack biome(AtlasBiome biome, TemperatureDomain temperatureDomain) {
		ItemStack stack = new ItemStack(biomeBlock(biome.id()));
		stack.setHoverName(AtlasComponents.biomeName(biome.id()));
		setLore(stack, AtlasComponents.biomeLore(biome, temperatureDomain));
		return stack;
	}

	private static void setLore(ItemStack stack, Iterable<Component> lines) {
		ListTag lore = new ListTag();
		for (Component line : lines) {
			lore.add(StringTag.valueOf(Component.Serializer.toJson(line)));
		}
		CompoundTag display = stack.getOrCreateTagElement("display");
		display.put("Lore", lore);
	}

	private static Block biomeBlock(ResourceLocation biomeId) {
		String path = biomeId.getPath();
		if (path.contains("warped")) {
			return Blocks.WARPED_NYLIUM;
		}
		if (path.contains("crimson")) {
			return Blocks.CRIMSON_NYLIUM;
		}
		if (path.contains("basalt")) {
			return Blocks.BASALT;
		}
		if (path.contains("nether") || path.contains("soul_sand")) {
			return Blocks.NETHERRACK;
		}
		if (path.contains("end")) {
			return Blocks.END_STONE;
		}
		if (path.contains("badlands")) {
			return Blocks.RED_SAND;
		}
		if (path.contains("desert") || path.contains("beach")) {
			return Blocks.SAND;
		}
		if (path.contains("frozen") || path.contains("ice")) {
			return Blocks.PACKED_ICE;
		}
		if (path.contains("snow") || path.contains("grove")) {
			return Blocks.SNOW_BLOCK;
		}
		if (path.contains("ocean") || path.contains("river")) {
			return Blocks.PRISMARINE;
		}
		if (path.contains("mushroom")) {
			return Blocks.MYCELIUM;
		}
		if (path.contains("swamp")) {
			return Blocks.MANGROVE_ROOTS;
		}
		if (path.contains("jungle")) {
			return Blocks.JUNGLE_SAPLING;
		}
		if (path.contains("pale")) {
			return Blocks.MOSS_BLOCK;
		}
		if (path.contains("lush")) {
			return Blocks.MOSS_BLOCK;
		}
		return Blocks.GRASS_BLOCK;
	}

	private static ItemStack named(ItemStack stack, Component name) {
		stack.setHoverName(name);
		return stack;
	}
}
