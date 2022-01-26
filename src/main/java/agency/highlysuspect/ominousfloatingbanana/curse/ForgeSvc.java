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

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
	
	public static List<ForgeSvcAddonMeta> bulkMetadata(OkHttpClient client, CurseManifest manifest) throws IOException {
		//Request metadata about each addon.
		JsonArray projectIdsJson = new JsonArray();
		manifest.files.forEach(f -> projectIdsJson.add(f.projectID));
		
		Request request = new Request.Builder()
			.url(API + "/addon/")
			.headers(HEADERS)
			.post(RequestBody.create(projectIdsJson.toString(), APPLICATION_JSON_UTF8))
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			return Init.GSON.fromJson(response.body().charStream(), new TypeToken<List<ForgeSvcAddonMeta>>() {}.getType());
		}
	}
	
	public static ForgeSvcAddonMeta ohMan(OkHttpClient client, int projectId) throws IOException {
		Request request = new Request.Builder()
			.url(API + "/addon/" + projectId)
			.headers(HEADERS)
			.get()
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			return Init.GSON.fromJson(response.body().charStream(), ForgeSvcAddonMeta.class);
		}
	}
	
	public static void main(String[] args) throws IOException {
		String wow = Files.readString(Paths.get("crap/crucial 2 manifest SMALL.json").toAbsolutePath());
		CurseManifest manifest = Init.GSON.fromJson(wow, CurseManifest.class);
		
		List<ForgeSvcAddonMeta> yes = bulkMetadata(Init.OKHTTP, manifest);
		
		System.out.println("hi");
	}
}
