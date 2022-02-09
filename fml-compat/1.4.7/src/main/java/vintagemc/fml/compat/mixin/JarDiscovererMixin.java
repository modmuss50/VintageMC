package vintagemc.fml.compat.mixin;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.JarDiscoverer;
import cpw.mods.fml.common.discovery.ModCandidate;

import vintagemc.fml.compat.FabricModDiscoverer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(JarDiscoverer.class)
public class JarDiscovererMixin {

	@Inject(method = "discover", at = @At("HEAD"), remap = false, cancellable = true)
	public void discover(ModCandidate candidate, ASMDataTable container, CallbackInfoReturnable<List<ModContainer>> cir) {
		if (candidate instanceof FabricModDiscoverer.FabricModCandidate fabricModCandidate) {
			// Create the dummy ModContainer for fabric mods.
			cir.setReturnValue(fabricModCandidate.createFMLContainers());
		}
	}
}
