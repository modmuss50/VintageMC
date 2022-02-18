package vintagemc.fml.compat.mixin;

import net.minecraftforge.transformers.EventTransformer;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EventTransformer.class)
public class EventTransformerMixin {
    @Redirect(method = "buildEvents", at = @At(value = "INVOKE", target = "Lorg/objectweb/asm/Type;getType(Ljava/lang/String;)Lorg/objectweb/asm/Type;", remap = false), remap = false)
    private Type getType(String typeDescriptor) {
        return Type.getObjectType(typeDescriptor);
    }
}
