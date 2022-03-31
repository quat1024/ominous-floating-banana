package agency.highlysuspect.ominousfloatingbanana.net;

import agency.highlysuspect.ominousfloatingbanana.Cli;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RateLimiter implements Interceptor {
	public RateLimiter(long timeout) {
		this.timeout = timeout;
		this.lastRequestTime = System.currentTimeMillis() - timeout;
	}
	
	//How many milliseconds to wait before consecutive requests
	public final long timeout;
	private long lastRequestTime;
	
	@NotNull
	@Override
	public Response intercept(@NotNull Chain chain) throws IOException {
		long now = System.currentTimeMillis();
		if(now - lastRequestTime < timeout) {
			try {
				Thread.sleep(now - lastRequestTime);
			} catch (InterruptedException e) {
				//(fart noise)
			}
		}
		
		lastRequestTime = now;
		return chain.proceed(chain.request());
	}
}
