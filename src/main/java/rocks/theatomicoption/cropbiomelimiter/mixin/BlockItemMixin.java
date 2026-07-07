package rocks.theatomicoption.cropbiomelimiter.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rocks.theatomicoption.cropbiomelimiter.CropBiomeLimiter;
import rocks.theatomicoption.cropbiomelimiter.events.PlayerActionHandler;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Shadow
	public abstract Block getBlock();

	@Shadow
	public abstract BlockPlaceContext updatePlacementContext(BlockPlaceContext context);

	@Shadow
	protected abstract BlockState getPlacementState(BlockPlaceContext context);

	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void cropbiomelimiter$denyBlockedPlantPlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		try {
			if (context == null) {
				return;
			}

			Level level = context.getLevel();
			if (!getBlock().isEnabled(level.enabledFeatures()) || !context.canPlace()) {
				return;
			}

			BlockPlaceContext placementContext = updatePlacementContext(context);
			if (placementContext == null) {
				return;
			}

			BlockState placementState = getPlacementState(placementContext);
			if (placementState == null) {
				return;
			}

			if (!CropBiomeLimiter.cropDecisionService().canPlace(level, placementContext.getClickedPos(), placementState, placementContext.getPlayer())) {
				cir.setReturnValue(PlayerActionHandler.blocked(level, placementContext.getPlayer()));
			} else if (CropBiomeLimiter.cropDecisionService().shouldWarnOnAllowedPlacement(level, placementContext.getClickedPos(), placementState, placementContext.getPlayer())) {
				PlayerActionHandler.warnAllowedPlacement(level, placementContext.getPlayer());
			}
		} catch (RuntimeException exception) {
			CropBiomeLimiter.LOGGER.warn("Allowing block placement because Crop Biome Limiter failed inside the block placement mixin.", exception);
		}
	}
}
