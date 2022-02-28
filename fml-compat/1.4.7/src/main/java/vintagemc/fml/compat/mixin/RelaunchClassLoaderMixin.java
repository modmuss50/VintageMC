package vintagemc.fml.compat.mixin;

import cpw.mods.fml.relauncher.RelaunchClassLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RelaunchClassLoader.class)
public class RelaunchClassLoaderMixin {

    @Inject(method = "findClass", at = @At("HEAD"), remap = false)
    private void findClass(String i$, CallbackInfoReturnable<Class<?>> cir) {
        // Nope, do not use this class loader! TODO: redirect to the real classloader?
        throw new UnsupportedOperationException("Wrong classloader!");
    }
}
