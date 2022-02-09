package vintagemc.fml.compat.mixin;

import java.util.List;

import cpw.mods.fml.relauncher.RelaunchLibraryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(RelaunchLibraryManager.class)
public class RelaunchLibraryManagerMixin {

	/**
	 * @author modmuss50
	 */
	@Overwrite(remap = false)
	public static List<String> getLibraries() {
		// Not perfect but filters out the main libs and the game.
		return FabricLoader.getInstance().getAllMods().stream()
				.filter(modContainer -> modContainer.getMetadata().getType().equals("builtin"))
				.map(modContainer -> modContainer.getOrigin().getPaths())
				.flatMap(List::stream)
				.map(path -> path.toFile().getName())
				.toList();
	}
}
