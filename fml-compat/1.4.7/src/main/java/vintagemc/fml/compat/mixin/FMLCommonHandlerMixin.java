package vintagemc.fml.compat.mixin;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(FMLCommonHandler.class)
public class FMLCommonHandlerMixin {

	@Inject(method = "computeBranding", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", remap = false, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
	public void computeBranding(CallbackInfo ci, ImmutableList.Builder<String> brd) {
		// Needed to make a good screenshot :D
		brd.add("Fabric Loader " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString());
	}
}
