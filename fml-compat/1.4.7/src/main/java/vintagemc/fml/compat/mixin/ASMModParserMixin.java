package vintagemc.fml.compat.mixin;

import cpw.mods.fml.common.discovery.asm.ASMModParser;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ASMModParser.class)
public class ASMModParserMixin {

	@Redirect(method = "beginNewTypeName", at = @At(value = "INVOKE", target = "Lorg/objectweb/asm/Type;getObjectType(Ljava/lang/String;)Lorg/objectweb/asm/Type;", remap = false), remap = false)
	public Type beginNewTypeName(String internalName) {
		if (internalName == null) {
			// Dont crash on module-info.class that have no super class.
			internalName = "java/lang/Object";
		}
		return Type.getObjectType(internalName);
	}
}
