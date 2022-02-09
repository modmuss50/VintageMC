package vintagemc.fml.compat.mixin;

import java.util.List;

import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.ModDiscoverer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vintagemc.fml.compat.FabricModDiscoverer;

@Mixin(ModDiscoverer.class)
public class ModDiscovererMixin {
	@Shadow
	private List<ModCandidate> candidates;

	@Inject(method = "findClasspathMods", at = @At("RETURN"), remap = false)
	public void findClasspathMods(ModClassLoader modClassLoader, CallbackInfo info) {
		// Look for fabric mods.
		this.candidates.addAll(FabricModDiscoverer.findCandidates());
	}
}
