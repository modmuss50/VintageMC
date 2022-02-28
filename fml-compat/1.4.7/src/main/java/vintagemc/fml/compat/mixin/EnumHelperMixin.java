package vintagemc.fml.compat.mixin;

import net.minecraftforge.common.EnumHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(EnumHelper.class)
public class EnumHelperMixin {
    @Shadow private static boolean isSetup;

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true, remap = false)
    private static void setup(CallbackInfo ci){
        isSetup = true;
        ci.cancel();
    }

    @Inject(method = "addEnum(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;[Ljava/lang/Object;)Ljava/lang/Enum;", at = @At("HEAD"), cancellable = true, remap = false)
    private static <T extends Enum<?>> void addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object[] paramValues, CallbackInfoReturnable<T> cir) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method values = enumType.getDeclaredMethod("values");
        values.setAccessible(true);
        Object[] objects = (Object[]) values.invoke(null);
        cir.setReturnValue((T) objects[0]);
    }

}
