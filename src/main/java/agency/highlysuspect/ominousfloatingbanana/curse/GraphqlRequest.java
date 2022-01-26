package agency.highlysuspect.ominousfloatingbanana.curse;

import agency.highlysuspect.ominousfloatingbanana.Init;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public interface GraphqlRequest<RESPONSE> {
	JsonObject requestToJson();
	RESPONSE parse(JsonElement value);
	
	MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
	
	default RESPONSE perform(Request.Builder builder, OkHttpClient client) throws IOException {
		Request request = builder
			.addHeader("Accept", "application/json")
			.post(RequestBody.create(requestToJson().toString(), APPLICATION_JSON_UTF8))
			.build();
		
		try(Response response = client.newCall(request).execute()) {
			ResponseBody body = response.body();
			//hope it fits in a string lol
			String content = body != null ? body.string() : "<nothing>";
			
			if(!response.isSuccessful() || body == null) {
				throw new IOException("Successful connection, but server said " + response.code() + " " + response.message() + "; " + content);
			}
			
			return parse(Init.GSON.fromJson(content, JsonElement.class));
		} catch (IOException e) {
			throw new IOException("IOException while connecting", e);
		}
	}
}
