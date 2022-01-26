package agency.highlysuspect.ominousfloatingbanana.curse.types;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Select fields from https://addons-ecs.forgesvc.net/api/v2/addon/(project id).
 */
public class ForgeSvcAddonMeta {
	@SerializedName("id") int projectId;
	String name;
	String slug;
	List<File> latestFiles;
	List<GameVersionLatestFile> gameVersionLatestFiles;
	
	public static class File {
		@SerializedName("id") int fileId;
		String fileName;
		String downloadUrl;
	}
	
	public static class GameVersionLatestFile {
		String gameVersion;
		@SerializedName("projectFileId") int fileId;
		@SerializedName("projectFileName") String fileName;
	}
}
