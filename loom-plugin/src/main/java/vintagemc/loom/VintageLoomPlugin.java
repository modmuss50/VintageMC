package vintagemc.loom;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
public class VintageLoomPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getLogger().lifecycle("Vintage loom");

        VintageLoomExtension vintageLoom = project.getExtensions().create("vintageLoom", VintageLoomExtension.class);
    }
}
