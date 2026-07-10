package rocks.theatomicoption.cropbiomelimiter.mixin;

import java.util.List;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rocks.theatomicoption.cropbiomelimiter.worldgen.VillageFarmProcessorLists;

@Mixin(ProcessorLists.class)
public abstract class ProcessorListsMixin {
	@Redirect(
			method = "bootstrap",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/data/worldgen/ProcessorLists;register(Lnet/minecraft/data/worldgen/BootstapContext;Lnet/minecraft/resources/ResourceKey;Ljava/util/List;)V"
			)
	)
	private static void cropbiomelimiter$registerProcessorList(
			BootstapContext<StructureProcessorList> context,
			ResourceKey<StructureProcessorList> key,
			List<StructureProcessor> processors
	) {
		VillageFarmProcessorLists.register(context, key, processors);
	}
}
