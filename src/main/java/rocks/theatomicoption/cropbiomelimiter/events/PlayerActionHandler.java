package rocks.theatomicoption.cropbiomelimiter.events;

import java.util.Optional;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;

public final class PlayerActionHandler {
	static final String BLOCKED_MESSAGE_KEY = "message.cropbiomelimiter.blocked";
	static final String ALLOWED_PLACEMENT_WARNING_KEY = "message.cropbiomelimiter.allowed_placement_warning";

	private static final Component BLOCKED_MESSAGE = Component.translatable(BLOCKED_MESSAGE_KEY);
	private static final Component ALLOWED_PLACEMENT_WARNING = Component.translatable(ALLOWED_PLACEMENT_WARNING_KEY);

	private PlayerActionHandler() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
				onUseOnBlock(new UseOnContext(player, hand, hitResult)));
	}

	static InteractionResult onUseOnBlock(UseOnContext context) {
		if (context == null) {
			return InteractionResult.PASS;
		}
		try {
			return tryOnUseOnBlock(context);
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing item use because Crop Biome Limiter failed inside the player action callback.", exception);
			return InteractionResult.PASS;
		}
	}

	private static InteractionResult tryOnUseOnBlock(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) {
			return InteractionResult.PASS;
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

		return InteractionResult.PASS;
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
			notifyPlayer(player, BLOCKED_MESSAGE);
		}
		return InteractionResult.FAIL;
	}

	public static void warnAllowedPlacement(Level level, Player player) {
		if (level == null || !level.isClientSide()) {
			notifyPlayer(player, ALLOWED_PLACEMENT_WARNING);
		}
	}

	private static void notifyPlayer(Player player, Component message) {
		try {
			if (player != null && CropBiomeLimiter.cropDecisionService().shouldSendChatInfo()) {
				player.sendSystemMessage(message);
			}
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.debug("Could not send Crop Biome Limiter chat feedback.", exception);
		}
	}

	private record PlacementTarget(BlockPos pos, BlockState state) {
	}
}
