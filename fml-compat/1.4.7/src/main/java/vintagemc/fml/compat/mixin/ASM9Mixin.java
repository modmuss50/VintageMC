package vintagemc.fml.compat.mixin;

import cpw.mods.fml.common.discovery.asm.ModAnnotationVisitor;
import cpw.mods.fml.common.discovery.asm.ModClassVisitor;
import cpw.mods.fml.common.discovery.asm.ModFieldVisitor;
import cpw.mods.fml.common.discovery.asm.ModMethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Bump the asm version used for these visitors so they can read Java 17 classes ;>)
 */
@Mixin({ModClassVisitor.class, ModFieldVisitor.class, ModMethodVisitor.class, ModAnnotationVisitor.class})
public class ASM9Mixin {
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = Opcodes.ASM4))
	private static int asmVersion(int constant) {
		return Opcodes.ASM9;
	}
}
