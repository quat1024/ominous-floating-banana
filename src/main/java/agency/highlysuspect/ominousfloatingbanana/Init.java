package agency.highlysuspect.ominousfloatingbanana;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Init {
	public static final Gson GSON = new GsonBuilder().create();
	
	public static final OkHttpClient OKHTTP = new OkHttpClient.Builder()
		.addInterceptor(new RateLimiter())
		.build();
	
	private static class RateLimiter implements Interceptor {
		private long lastRequestTime = 0;
		
		@NotNull
		@Override
		public Response intercept(@NotNull Chain chain) throws IOException {
			long now = System.currentTimeMillis();
			if(now - lastRequestTime < 1000L) {
				try {
					System.out.println("Sleeping for " + (now - lastRequestTime) + "ms");
					Thread.sleep(now - lastRequestTime);
				} catch (InterruptedException e) {
					//fart noise
				}
			}
			
			lastRequestTime = now;
			return chain.proceed(chain.request());
		}
	}
}
