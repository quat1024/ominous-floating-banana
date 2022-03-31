package agency.highlysuspect.ominousfloatingbanana;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Services for interacting with addons-ecs.forgesvc.net
 */
public class ForgeSvcDownloader {
	public ForgeSvcDownloader(OkHttpClient client, boolean dryRun) {
		this.client = client;
		this.dryRun = dryRun;
	}
	
	private static final String API = "https://addons-ecs.forgesvc.net/api/v2";
	private static final Headers headers = new Headers.Builder().add("Accept", "application/json").build();
	
	private final OkHttpClient client;
	private final boolean dryRun;
	
	public HttpUrl getDownloadUrl(OfbManifest.Entry mod) throws IOException {
		Cli.log("Requesting download URL for " + mod.name());
		
		Request request = new Request.Builder()
			.url(API + "/addon/" + mod.projectId() + "/file/" + mod.fileId() + "/download-url")
			.headers(headers).get().build();
		
		try(Response response = client.newCall(request).execute()) {
			ResponseBody body = response.body();
			if(body == null) throw new IOException("Got response with empty body!");
			
			//The result is returned within double quotes.. Lol. TODO maybe it's actually a json string or something
			String bodyString = body.string();
			HttpUrl downloadUrl = HttpUrl.parse(bodyString.substring(1, bodyString.length() - 1));
			Cli.log("Got URL: " + downloadUrl);
			return downloadUrl;
		}
	}
	
	public void downloadModInto(OfbManifest.Entry mod, Path destination) throws IOException {
		HttpUrl downloadUrl = getDownloadUrl(mod);
		
		//todo: sanitize filename maybe? (path traversal, weird characters)
		String fileName = downloadUrl.pathSegments().get(downloadUrl.pathSize() - 1);
		Path targetPath = destination.resolve(fileName);
		
		Cli.log("Saving to " + targetPath);
		
		if(dryRun) {
			Cli.log("Dry run - not performing download.");
			return;
		}
		
		Files.createDirectories(targetPath.getParent());
		
		Request request = new Request.Builder().url(downloadUrl)
			.headers(headers).get().build();
		
		try(Response response = client.newCall(request).execute(); //http response
		    BufferedSink sink = Okio.buffer(Okio.sink(targetPath))) {
			ResponseBody body = response.body();
			if(body == null) throw new IOException("Got response with empty body!");
			
			sink.writeAll(body.source());
		}
		
		Cli.log("Downloaded " + mod.name() + ".");
	}
}
