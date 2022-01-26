package agency.highlysuspect.ominousfloatingbanana.curse;

import java.util.List;
import java.util.function.UnaryOperator;

public record CurseManifest2 (
	MinecraftVersion minecraft,
	String manifestType,
	int manifestVersion,
	String name,
	String version,
	String author,
	List<File> files,
	String overrides
) {
	public static record MinecraftVersion(String version, List<ModLoaders> modLoaders) {
		public static record ModLoaders(String id, boolean primary) {}
	}
	
	public static record File(int projectID, int fileID, boolean required) {
		public File withFileID(int newFileID) {
			return new File(
				this.projectID(),
				newFileID,
				this.required()
			);
		}
	}
	
	public CurseManifest2 applyToFiles(UnaryOperator<List<File>> fileOperator) {
		return withFiles(fileOperator.apply(files));
	}
	
	public CurseManifest2 withFiles(List<File> newFiles) {
		return new CurseManifest2(
			this.minecraft(),
			this.manifestType(),
			this.manifestVersion(),
			this.name(),
			this.version(),
			this.author(),
			newFiles,
			this.overrides()
		);
	}
}
