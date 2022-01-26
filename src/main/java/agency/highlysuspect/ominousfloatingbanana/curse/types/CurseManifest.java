package agency.highlysuspect.ominousfloatingbanana.curse.types;

import agency.highlysuspect.ominousfloatingbanana.Init;
import okio.BufferedSource;
import okio.Okio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord") //GSON doesn't know how to handle records yet.
public final class CurseManifest {
	public MinecraftVersion minecraft;
	public String manifestType;
	public int manifestVersion;
	public String name;
	public String version;
	public String author;
	public List<File> files;
	public String overrides;
	
	public CurseManifest(
		MinecraftVersion minecraft,
		String manifestType,
		int manifestVersion,
		String name,
		String version,
		String author,
		List<File> files,
		String overrides
	) {
		this.minecraft = minecraft;
		this.manifestType = manifestType;
		this.manifestVersion = manifestVersion;
		this.name = name;
		this.version = version;
		this.author = author;
		this.files = files;
		this.overrides = overrides;
	}
	
	public static final class MinecraftVersion {
		public final String version;
		public final List<ModLoaders> modLoaders;
		
		public MinecraftVersion(String version, List<ModLoaders> modLoaders) {
			this.version = version;
			this.modLoaders = modLoaders;
		}
		
		public static final class ModLoaders {
			public final String id;
			public final boolean primary;
			
			public ModLoaders(String id, boolean primary) {
				this.id = id;
				this.primary = primary;
			}
		}
	}
	
	public static final class File {
		public int projectID;
		public int fileID;
		public boolean required;
		
		public File(int projectID, int fileID, boolean required) {
			this.projectID = projectID;
			this.fileID = fileID;
			this.required = required;
		}
	}
	
	public static CurseManifest read(Path path) throws IOException {
		return Init.GSON.fromJson(Files.newBufferedReader(path), CurseManifest.class);
	}
}
