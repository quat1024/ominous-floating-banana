package agency.highlysuspect.ominousfloatingbanana.curse;

import agency.highlysuspect.ominousfloatingbanana.Init;
import agency.highlysuspect.ominousfloatingbanana.curse.types.CurseManifest;
import agency.highlysuspect.ominousfloatingbanana.curse.types.ForgeSvcAddonMeta;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Services for interacting with the "real" CurseForge API
 */
public class ForgeSvc {
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
	private static final String API = "https://addons-ecs.forgesvc.net/api/v2";
	//todo move this somewhere else
	private static final Headers HEADERS = new Headers.Builder()
		.add("User-Agent", "Super Sketchy Scraper wooOOoooOOooo")
		.add("Accept", "application/json")
		.build();
	
	public static List<ForgeSvcAddonMeta> bulkMetadata(OkHttpClient client, List<Integer> projectIds) throws IOException {
		JsonArray projectIdsJson = new JsonArray();
		projectIds.forEach(projectIdsJson::add);
		
		Request request = new Request.Builder()
			.url(API + "/addon/")
			.headers(HEADERS)
			.post(RequestBody.create(projectIdsJson.toString(), APPLICATION_JSON_UTF8))
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			return Init.GSON.fromJson(response.body().charStream(), new TypeToken<List<ForgeSvcAddonMeta>>() {}.getType());
		}
	}
	
	public static ForgeSvcAddonMeta metadata(OkHttpClient client, int projectId) throws IOException {
		Request request = new Request.Builder()
			.url(API + "/addon/" + projectId)
			.headers(HEADERS)
			.get()
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			return Init.GSON.fromJson(response.body().charStream(), ForgeSvcAddonMeta.class);
		}
	}
	
	public static String downloadUrl(OkHttpClient client, int projectId, int fileId) throws IOException {
		Request request = new Request.Builder()
			.url(API + "/addon/" + projectId + "/file/" + fileId + "/download-url")
			.headers(HEADERS).get().build();
		
		try(Response response = client.newCall(request).execute()) {
			//It has double quotes around it lol
			String lmao = response.body().string();
			return lmao.substring(1, lmao.length() - 1);
		}
	}
	
	public static void download(OkHttpClient client, int projectId, int fileId, String filename, Path modsFolder) throws IOException {
		Path targetPath = modsFolder.resolve(filename);
		
		String downloadUrl = downloadUrl(client, projectId, fileId);
		Request request = new Request.Builder().url(downloadUrl)
			.headers(HEADERS).get().build();
		
		try(Response response = client.newCall(request).execute();
		    BufferedSink sink = Okio.buffer(Okio.sink(targetPath))) {
			sink.writeAll(response.body().source());
		}
	}
	
	public static void main(String[] args) throws IOException {
		String manifestJson = Files.readString(Paths.get("crap/crucial 2 manifest SMALL.json").toAbsolutePath());
		CurseManifest manifest = Init.GSON.fromJson(manifestJson, CurseManifest.class);
		
		Path modsFolder = Paths.get("crap/run/mods/");
		Files.createDirectories(modsFolder);
		
		//List<ForgeSvcAddonMeta> yes = bulkMetadata(Init.OKHTTP, manifest.files.stream().map(file -> file.projectID).collect(Collectors.toList()));
		for(CurseManifest.File file : manifest.files) {
			download(Init.OKHTTP, file.projectID, file.fileID, /* todo */ file.projectID + ".jar", modsFolder);
		}
		
		System.out.println("hi");
	}
}
