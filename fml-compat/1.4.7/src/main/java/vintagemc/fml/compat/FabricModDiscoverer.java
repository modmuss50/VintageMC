package vintagemc.fml.compat;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.discovery.ContainerType;
import cpw.mods.fml.common.discovery.ModCandidate;

import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.VersionRange;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.File;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Create FMl mods for fabric mods
 */
public class FabricModDiscoverer {
	public static List<ModCandidate> findCandidates() {
		List<ModCandidate> candidates = new ArrayList<>();

		for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
			List<Path> paths = modContainer.getOrigin().getPaths();
			Path modPath = paths.get(0); // A bit of a bad assumption, but best we can do for now.

			candidates.add(new FabricModCandidate(modContainer, modPath));
		}

		return candidates;
	}

	public static class FabricModCandidate extends ModCandidate {
		private final ModContainer container;
		private final Path source;

		public FabricModCandidate(ModContainer container, Path path) {
			// May not be a jar but we hack it in later.
			super(path.toFile(), path.toFile(), ContainerType.JAR);
			this.container = container;
			this.source = path;
		}

		public List<cpw.mods.fml.common.ModContainer> createFMLContainers() {
			return Collections.singletonList(new FabricFMLModContainer(container.getMetadata(), source));
		}

		@Override
		public boolean isMinecraftJar() {
			return container.getMetadata().getId().equals("minecraft");
		}
	}

	private static class FabricFMLModContainer implements cpw.mods.fml.common.ModContainer {
		private final net.fabricmc.loader.api.metadata.ModMetadata fabricMetadata;
		private final Path source;
		private final ModMetadata modMetadata;

		private ArtifactVersion processedVersion;

		private FabricFMLModContainer(net.fabricmc.loader.api.metadata.ModMetadata fabricMetadata, Path source) {
			this.fabricMetadata = fabricMetadata;
			this.source = source;

			this.modMetadata = new ModMetadata();
			this.modMetadata.modId = getModId();
			this.modMetadata.name = getName();
			this.modMetadata.logoFile = "/" + fabricMetadata.getIconPath(64).orElse("");
			this.modMetadata.description = fabricMetadata.getDescription();
		}

		@Override
		public String getModId() {
			return fabricMetadata.getId();
		}

		@Override
		public String getName() {
			return fabricMetadata.getName();
		}

		@Override
		public String getVersion() {
			return fabricMetadata.getVersion().getFriendlyString();
		}

		@Override
		public File getSource() {
			return source.toFile();
		}

		@Override
		public ModMetadata getMetadata() {
			return modMetadata;
		}

		@Override
		public void bindMetadata(MetadataCollection metadataCollection) {

		}

		@Override
		public void setEnabledState(boolean bl) {

		}

		@Override
		public Set<ArtifactVersion> getRequirements() {
			return Collections.emptySet();
		}

		@Override
		public List<ArtifactVersion> getDependencies() {
			return Collections.emptyList();
		}

		@Override
		public List<ArtifactVersion> getDependants() {
			return Collections.emptyList();
		}

		@Override
		public String getSortingRules() {
			return "";
		}

		@Override
		public boolean registerBus(EventBus eventBus, LoadController loadController) {
			return true;
		}

		@Override
		public boolean matches(Object object) {
			return false;
		}

		@Override
		public Object getMod() {
			return new Object();
		}

		@Override
		public ArtifactVersion getProcessedVersion() {
			if (this.processedVersion == null) {
				this.processedVersion = new DefaultArtifactVersion(this.getModId(), this.getVersion());
			}
			return this.processedVersion;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}

		@Override
		public boolean isNetworkMod() {
			return false;
		}

		@Override
		public String getDisplayVersion() {
			return fabricMetadata.getVersion().getFriendlyString();
		}

		@Override
		public VersionRange acceptableMinecraftVersionRange() {
			return Loader.instance().getMinecraftModContainer().getStaticVersionRange();
		}

		@Override
		public Certificate getSigningCertificate() {
			return null;
		}
	}
}
