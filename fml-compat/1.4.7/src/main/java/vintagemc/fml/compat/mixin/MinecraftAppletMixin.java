package vintagemc.fml.compat.mixin;

import net.minecraft.client.MinecraftApplet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftApplet.class)
public abstract class MinecraftAppletMixin {
	@Shadow
	public abstract void fmlInitReentry();

	/**
	 * @author modmuss50.
	 */
	@Overwrite
	public void init() {
		// Don't relaunch
		fmlInitReentry();
	}
}
