package vintagemc.fml.compat.mixin;

import cpw.mods.fml.common.FMLModContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FMLModContainer.class)
public class FMLModContainerMixin {
    @Redirect(method = {"constructMod", "parseSimpleFieldAnnotation"}, at = @At(value = "INVOKE", target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", remap = false), remap = false)
    private Class<?> constructMod(String name, boolean init, ClassLoader initialize) throws ClassNotFoundException {
        return Class.forName(name, init, FabricLauncherBase.getLauncher().getTargetClassLoader());
    }
}
