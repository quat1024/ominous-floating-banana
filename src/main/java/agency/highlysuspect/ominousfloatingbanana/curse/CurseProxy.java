package agency.highlysuspect.ominousfloatingbanana.curse;

import agency.highlysuspect.ominousfloatingbanana.Init;
import agency.highlysuspect.ominousfloatingbanana.curse.types.Addon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Services for interacting with CurseProxy.
 */
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
	
	public static void main(String[] args) {
		try {
			System.out.println(new FetchAddonBySlug("botania").perform(Init.OKHTTP));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(new FetchAddonBySlug("botania-fabric").perform(Init.OKHTTP));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(new FetchAddonBySlug("qwertyuiopsdfghjklasdasdasd").perform(Init.OKHTTP));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(new FetchAddonBySlug("new-super-quark-pack-deluxe-s-xl-ultra-5g-pro-max").perform(Init.OKHTTP));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static record FetchAddonBySlug(String slug) implements GraphqlRequest<Addon> {
		@Override
		public JsonObject requestToJson() {
			JsonObject j = new JsonObject();
			j.addProperty("query", """
				query banana($slug: String) {
					addons(slug: $slug) {
						id
						categorySection {
							id
						}
					}
				}""");
			j.addProperty("operationName", "banana");
			
			JsonObject variables = new JsonObject();
			variables.addProperty("slug", slug);
			j.add("variables", variables);
			
			return j;
		}
		
		@Override
		public Addon parse(JsonElement value) {
			JsonArray addons = value.getAsJsonObject().get("data").getAsJsonObject().get("addons").getAsJsonArray();
			
			if(addons.isEmpty()) {
				throw new RuntimeException("Zero addons returned for slug " + slug);
			}
			
			Addon addon = Init.GSON.fromJson(addons.get(0), Addon.class);
			
			if(addon.categorySection.id != 8) {
				throw new RuntimeException("Slug " + slug + " is not a Minecraft mod");
			}
			
			return addon;
		}
	}
	
	public interface GraphqlRequest<RESPONSE> {
		JsonObject requestToJson();
		RESPONSE parse(JsonElement value);
		
		MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
		
		default RESPONSE perform(OkHttpClient client) throws IOException {
			Request request = new Request.Builder()
				.url(ENDPOINT)
				.headers(HEADERS)
				.post(RequestBody.create(requestToJson().toString(), APPLICATION_JSON_UTF8))
				.build();
			
			try(Response response = client.newCall(request).execute()) {
				ResponseBody body = response.body();
				
				if(!response.isSuccessful() || body == null) {
					//hope it fits in a string lol
					String content = body != null ? body.string() : "<nothing>";
					throw new IOException("Successful connection, but server said " + response.code() + " " + response.message() + "; " + content);
				}
				
				return parse(Init.GSON.fromJson(body.charStream(), JsonElement.class));
			} catch (IOException e) {
				throw new IOException("IOException while connecting", e);
			}
		}
	}
}
