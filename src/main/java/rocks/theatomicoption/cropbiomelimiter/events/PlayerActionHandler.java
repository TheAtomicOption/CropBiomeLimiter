package rocks.theatomicoption.cropbiomelimiter.events;

import java.util.Optional;

import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class PlayerActionHandler {
	private static final Component CANNOT_GROW_MESSAGE = Component.literal("This plant can't grow in this climate.");

	private PlayerActionHandler() {
	}

	public static void register() {
		ItemEvents.USE_ON.register(PlayerActionHandler::onUseOnBlock);
		BlockEvents.USE_ITEM_ON.register(PlayerActionHandler::onUseItemOnBlock);
	}

	private static InteractionResult onUseOnBlock(UseOnContext context) {
		try {
			return tryOnUseOnBlock(context);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing item use because Crop Biome Limiter failed inside the player action callback.", exception);
			return null;
		}
	}

	private static InteractionResult tryOnUseOnBlock(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) {
			return null;
		}

		ItemStack stack = context.getItemInHand();
		Item item = stack.getItem();
		Player player = context.getPlayer();

		Optional<InteractionResult> bonemealResult = tryHandleBonemeal(context, level, item, player);
		if (bonemealResult.isPresent()) {
			return bonemealResult.get();
		}

		Optional<InteractionResult> placementResult = tryHandlePlacement(context, level, item, player);
		if (placementResult.isPresent()) {
			return placementResult.get();
		}

		return null;
	}

	private static InteractionResult onUseItemOnBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		try {
			return tryOnUseItemOnBlock(stack, state, level, pos, player);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing block item use because Crop Biome Limiter failed inside the block interaction callback.", exception);
			return null;
		}
	}

	private static InteractionResult tryOnUseItemOnBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
		if (level.isClientSide()) {
			return null;
		}

		Item item = stack.getItem();
		return tryHandleBonemeal(level, pos, state, item, player).orElse(null);
	}

	private static Optional<InteractionResult> tryHandleBonemeal(UseOnContext context, Level level, Item item, Player player) {
		if (item != Items.BONE_MEAL || !(level instanceof ServerLevel serverLevel)) {
			return Optional.empty();
		}

		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		return tryHandleBonemeal(serverLevel, pos, state, item, player);
	}

	private static Optional<InteractionResult> tryHandleBonemeal(Level level, BlockPos pos, BlockState state, Item item, Player player) {
		if (item != Items.BONE_MEAL || !(level instanceof ServerLevel serverLevel)) {
			return Optional.empty();
		}

		if (CropBiomeLimiter.cropDecisionService().canUseBonemeal(serverLevel, pos, state, player)) {
			return Optional.empty();
		}
		return Optional.of(blocked(level, player));
	}

	private static Optional<InteractionResult> tryHandlePlacement(UseOnContext context, Level level, Item item, Player player) {
		Optional<PlacementTarget> target = tryPlacementTarget(context, item);
		if (target.isEmpty()) {
			return Optional.empty();
		}

		PlacementTarget placementTarget = target.get();
		if (CropBiomeLimiter.cropDecisionService().canPlace(level, placementTarget.pos(), placementTarget.state(), player)) {
			return Optional.empty();
		}
		return Optional.of(blocked(level, player));
	}

	private static Optional<PlacementTarget> tryPlacementTarget(UseOnContext context, Item item) {
		Block block = placementBlock(item);
		if (block == Blocks.AIR) {
			return Optional.empty();
		}

		BlockPlaceContext placeContext = new BlockPlaceContext(context);
		if (item instanceof BlockItem blockItem) {
			placeContext = blockItem.updatePlacementContext(placeContext);
		}
		if (placeContext == null) {
			return Optional.empty();
		}
		if (!placeContext.canPlace()) {
			return Optional.empty();
		}
		BlockState state = block.getStateForPlacement(placeContext);
		if (state == null) {
			return Optional.empty();
		}

		return Optional.of(new PlacementTarget(placeContext.getClickedPos(), state));
	}

	private static Block placementBlock(Item item) {
		if (item instanceof BlockItem blockItem) {
			return blockItem.getBlock();
		}
		return Block.byItem(item);
	}

	public static InteractionResult blocked(Level level, Player player) {
		if (level == null || !level.isClientSide()) {
			notifyPlayer(player);
		}
		return InteractionResult.FAIL;
	}

	public static void warnAllowedPlacement(Level level, Player player) {
		if (level == null || !level.isClientSide()) {
			notifyPlayer(player);
		}
	}

	private static void notifyPlayer(Player player) {
		try {
			if (player != null && CropBiomeLimiter.cropDecisionService().shouldSendChatInfo()) {
				player.sendSystemMessage(CANNOT_GROW_MESSAGE);
			}
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Could not send Crop Biome Limiter chat feedback.", exception);
		}
	}

	private record PlacementTarget(BlockPos pos, BlockState state) {
	}
}
