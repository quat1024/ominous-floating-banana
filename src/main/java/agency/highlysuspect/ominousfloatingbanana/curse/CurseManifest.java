package agency.highlysuspect.ominousfloatingbanana.curse;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class CurseManifest {
	public CurseManifest(List<CurseFile> curseFiles, JsonObject originalJson) {
		this.curseFiles = curseFiles;
		this.originalJson = originalJson;
	}
	
	public final List<CurseFile> curseFiles;
	public final JsonObject originalJson;
	
	public static CurseManifest parse(JsonValue value) {
		JsonObject object = value.asObject();
		
		//Parse the "files" block out of the json object
		List<CurseFile> files = new ArrayList<>();
		object.get("files").asArray().forEach(fileValue -> files.add(CurseFile.fromJson(fileValue)));
		
		return new CurseManifest(files, object);
	}
	
	public JsonObject toJson() {
		//Copy it (this json toolkit doesn't have great utilities for copying, hope this works)
		JsonObject object = new JsonObject().merge(originalJson);
		
		//Update the "files" array
		JsonArray filesArray = new JsonArray();
		curseFiles.forEach(file -> filesArray.add(file.toJson()));
		object.set("files", filesArray);
		
		return object;
	}
	
	public static record CurseFile(int projectId, int fileId) {
		public static CurseFile fromJson(JsonValue value) {
			JsonObject object = value.asObject();
			int projectId = object.get("projectID").asInt();
			int fileId = object.get("fileID").asInt();
			return new CurseFile(projectId, fileId);
		}
		
		public JsonObject toJson() {
			JsonObject a = new JsonObject();
			a.add("projectID", projectId);
			a.add("fileID", fileId);
			a.add("required", true);
			return a;
		}
	}
}
