package vintagemc.fml.compat;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.FMLRelauncher;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import vintagemc.fml.compat.coremod.CoreModLocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * A rough replacement for tasks carried out in FMLRelauncher
 */
public class FMLPreLaunch implements PreLaunchEntrypoint {
	public static Object[] injectionData;

	@Override
	public void onPreLaunch() {
		Properties properties = new Properties();

		try (InputStream is = getClass().getClassLoader().getResourceAsStream("fmlversion.properties")) {
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String major = properties.getProperty("fmlbuild.major.number", "missing");
		String minor = properties.getProperty("fmlbuild.minor.number", "missing");
		String rev = properties.getProperty("fmlbuild.revision.number", "missing");
		String build = properties.getProperty("fmlbuild.build.number", "missing");
		String mccversion = properties.getProperty("fmlbuild.mcversion", "missing");
		String mcpversion = properties.getProperty("fmlbuild.mcpversion", "missing");

		// See: FMLRelauncher.handleClientRelaunch
		FMLRelauncher.logFileNamePattern = "ForgeModLoader-client-%g.log";
		FMLRelauncher.side = "CLIENT";

		CoreModLocator.setup();
		List<String> containers = CoreModLocator.LOADING_PLUGINS.stream().map(IFMLLoadingPlugin::getModContainerClass).toList();

		injectionData = new Object[]{
				major,
				minor,
				rev,
				build,
				mccversion,
				mcpversion,
				FabricLoader.getInstance().getGameDir().toFile(),
				containers
		};

		Loader.injectData(injectionData);

		FMLRelaunchLog.minecraftHome = FabricLoader.getInstance().getGameDir().toFile();

		// This log line is critical to setup the logging correctly.
		FMLRelaunchLog.info("Forge Mod Loader version %s.%s.%s.%s for Minecraft %s loading", major, minor, rev, build, mccversion, mcpversion);
	}
}
