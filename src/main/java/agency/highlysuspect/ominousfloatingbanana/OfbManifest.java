package agency.highlysuspect.ominousfloatingbanana;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record OfbManifest(List<Entry> entries) {
	public static record Entry(
		//Curse project id. Authoritative.
		int projectId,
		//Curse file id. Authoritative.
		int fileId,
		
		//A meaningless string. Makes manually browsing the manifest file a little more invigorating.
		String name,
		//The filename that that Curse project+fileid resolves to.
		//Used in lieu of a hash. If a file with this name exists, it will not be redownloaded.
		String filename
	) {
		public JsonObject toJson() {
			JsonObject json = new JsonObject();
			json.addProperty("projectID", projectId);
			json.addProperty("fileID", fileId);
			json.addProperty("name", name);
			json.addProperty("filename", filename);
			return json;
		}
		
		public static Entry fromJson(JsonElement jsonElem) {
			if(!(jsonElem instanceof JsonObject json)) throw new JsonSyntaxException("Not JsonObject");
			
			return new Entry(
				json.get("projectID").getAsInt(),
				json.get("fileID").getAsInt(),
				json.get("name").getAsString(),
				json.get("filename").getAsString()
			);
		}
	}
	
	public JsonObject toJson() {
		JsonArray filesArray = new JsonArray();
		for(Entry entry : entries) {
			filesArray.add(entry.toJson());
		}
		
		//wrap it in a json object (like curse manifest)
		JsonObject json = new JsonObject();
		json.add("files", filesArray);
		return json;
	}
	
	public static OfbManifest fromJson(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject json)) throw new JsonSyntaxException("Not JsonObject");
		
		List<Entry> entries = new ArrayList<>();
		json.get("files").getAsJsonArray().forEach(elem -> entries.add(Entry.fromJson(elem)));
		return new OfbManifest(entries);
	}
	
	///
	
	public static OfbManifest fromFile(Gson gson, Path manifestPath) throws IOException {
		return fromJson(gson.fromJson(Files.newBufferedReader(manifestPath), JsonElement.class));
	}
	
	public void performDownload(OkHttpClient client, Path destination, boolean dryRun) throws IOException {
		//1. Remove entries that already exist on the filesystem.
		ArrayList<Entry> toDownload = new ArrayList<>(entries);
		toDownload.removeIf(entry -> {
			Path modPathA = destination.resolve(entry.filename);
			if(Files.exists(modPathA)) {
				Cli.log("Skipping " + entry.name + " because " + modPathA + " already exists.");
				return true;
			}
			
			//respect files renamed to ".jar.disabled"
			Path modPathB = destination.resolve(entry.filename + ".disabled");
			if(Files.exists(modPathB)) {
				Cli.log("Skipping " + entry.name + " because " + modPathB + " was manually disabled.");
				return true;
			}
			
			return false;
		});
		
		//2. Actually download the files!
		ForgeSvcDownloader downloader = new ForgeSvcDownloader(client, dryRun);
		
		System.out.println("Downloading " + toDownload.size() + " mods (out of " + entries.size() + ")");
		for(Entry entry : toDownload) {
			downloader.downloadModInto(entry, destination);
		}
		
		System.out.println("All done.");
	}
}
