package agency.highlysuspect.ominousfloatingbanana.curse;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import java.util.function.Supplier;

public class CurseProxy {
	//Some quick notes about nikky's server:
	// - It doesn't seem to like Content-Type: application/graphql. It requires a JSON encoded query.
	// - The spec says "operationName" is optional, but unless I'm doing something wrong you
	//   need to pass the server a named operation and declare the same name in "operationName",
	//   so, i don't think anonymous queries work.
	private static final String ENDPOINT = "https://curse.nikky.moe/graphql";
	private static final Headers HEADERS = new Headers.Builder()
		.add("User-Agent", "quaternary/my jank java project (now with okhttp)")
		.add("X-FunFact", "Dragons are pretty neat honestly")
		.build();
	
	public static void main(String[] args) {
		OkHttpClient client = new OkHttpClient();
		Supplier<Request.Builder> requestBuilder = () -> new Request.Builder().url(ENDPOINT).headers(HEADERS);
		
		try {
			System.out.println(new AddonBySlug("botania").perform(requestBuilder.get(), client));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(new AddonBySlug("botania-fabric").perform(requestBuilder.get(), client));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(new AddonBySlug("qwertyuiopsdfghjklasdasdasd").perform(requestBuilder.get(), client));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(new AddonBySlug("new-super-quark-pack-deluxe-s-xl-ultra-5g-pro-max").perform(requestBuilder.get(), client));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
