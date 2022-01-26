package agency.highlysuspect.ominousfloatingbanana.curse;

import agency.highlysuspect.ominousfloatingbanana.graphql.GField;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public class CurseProxy {
	//Some quick notes about nikky's server:
	// - It doesn't seem to like Content-Type: application/graphql. It requires a JSON encoded query.
	// - The spec says "operationName" is optional, but unless I'm doing something wrong you
	//   need to pass the server a named operation and declare the same name in "operationName",
	//   so, i don't think anonymous queries work.
	private static final String ENDPOINT = "https://curse.nikky.moe/graphql";
	
	private static final Headers HEADERS = new Headers.Builder()
		.add("User-Agent", "quaternary/my jank java project (now with okhttp)")
		.add("Accept", "application/json")
		.add("X-FunFact", "Dragons are pretty neat honestly")
		.build();
	
	private static final MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
	
	public static JsonValue sendGraphQlRequestToNikky(OkHttpClient client, GField query) {
		//See https://graphql.org/learn/serving-over-http/#post-request .
		
		Request request = new Request.Builder()
			.url(ENDPOINT)
			.headers(HEADERS)
			.post(
				RequestBody.create(
					new JsonObject()
						.add("query", query.stringifyToQueryNamed("a"))
						.add("operationName", "a")
						.toString(),
					APPLICATION_JSON_UTF8)
			)
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			ResponseBody body = response.body();
			
			//assume it all fits comfortably in a string
			String content = body != null ? body.string() : "";
			
			if(!response.isSuccessful()) {
				throw new ConnectionError("Successful connection, but server said " + response.code() + " " + response.message() + "; " + content);
			}
			
			//TODO: maybe unwrap the graphql data/errors fields?
			return Json.parse(content);
		} catch (IOException e) {
			throw new ConnectionError("IOException while connecting", e);
		}
	}
	
	public static class ConnectionError extends RuntimeException {
		public ConnectionError(String message) {super(message);}
		public ConnectionError(String message, Throwable cause) {super(message, cause);}
	}
	
	//this error handling is bad and I should feel bad. very stringly-typed. i'm just taping things together and will work something out later.
	public static int slugToProjectId(OkHttpClient client, String slug) {
		GField query = new GField("addons").prop("slug", slug).nest(
			new GField("id"), 
			new GField("categorySection").nest(
				new GField("id")
			)
		);
		
		JsonValue response = sendGraphQlRequestToNikky(client, query);
		
		try {
			//standard graphql protocol stuff (maybe i should handle this earlier?)
			JsonObject dataClause = response.asObject().get("data").asObject();
			
			//the addons block. if there are no returned addons, there wasn't a project with that slug
			JsonArray addons = dataClause.get("addons").asArray();
			if(addons.isEmpty()) {
				throw new ConnectionError("Did not find a project with slug " + slug + ".");
			}
			
			//according to comp500/packwiz, if it doesn't have category section 8, it's not a minecraft mod
			JsonObject addon = addons.get(0).asObject();
			int id = addon.get("id").asInt();
			int categorySection = addon.get("categorySection").asObject().get("id").asInt();
			
			if(categorySection != 8) {
				throw new ConnectionError("Slug " + slug + " has project id " + id + " but category section " + categorySection + ", and so probably isn't a mod.");
			}
			
			return id;
		} catch (UnsupportedOperationException e) {
			throw new ConnectionError("Can't make sense of this json: " + response);
		}
	}
	
	public static void main(String[] args) {
		OkHttpClient client = new OkHttpClient();
		try {
			System.out.println(slugToProjectId(client, "botania"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(slugToProjectId(client, "botania-fabric"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(slugToProjectId(client, "asjdklasjdlkasjkldjaskld"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(slugToProjectId(client, "new-super-quark-pack-deluxe-s-xl-ultra-5g-pro-max"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
