package agency.highlysuspect.ominousfloatingbanana.curse;

import agency.highlysuspect.ominousfloatingbanana.Init;
import agency.highlysuspect.ominousfloatingbanana.curse.types.CurseManifest;
import com.google.gson.JsonArray;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Services for interacting with the "real" CurseForge
 */
public class CurseForgeSvc {
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
	private static final String API = "https://addons-ecs.forgesvc.net/api/v2";
	private static final Headers HEADERS = new Headers.Builder()
		.add("User-Agent", "Super Sketchy Scraper wooOOoooOOooo")
		.add("Accept", "application/json")
		.build();
	
	public static void bigMeta(OkHttpClient client, CurseManifest manifest) throws IOException {
		List<Integer> projectIds = manifest.files.stream().map(f -> f.projectID).collect(Collectors.toList());
		
		JsonArray yes = new JsonArray();
		projectIds.forEach(yes::add);
		
		Request request = new Request.Builder()
			.url(API + "/addon/")
			.headers(HEADERS)
			.post(RequestBody.create(yes.toString(), APPLICATION_JSON_UTF8))
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			System.out.println(response.body().string());
			System.out.println("hi");
		}
	}
	
	public static void ohMan(OkHttpClient client, int projectId) throws IOException {
		Request request = new Request.Builder()
			.url(API + "/addon/" + projectId)
			.headers(HEADERS)
			.get()
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			System.out.println(response);
			System.out.println(response.body().string());
			System.out.println("hi");
		}
	}
	
	public static void main(String[] args) throws IOException {
		String wow = Files.readString(Paths.get("crap/crucial 2 manifest SMALL.json").toAbsolutePath());
		CurseManifest manifest = Init.GSON.fromJson(wow, CurseManifest.class);
		
		//ohMan(new OkHttpClient(), manifest.files.get(69).projectID);
		
		bigMeta(new OkHttpClient(), manifest);
	}
}
