package agency.highlysuspect.ominousfloatingbanana.net;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public record UserAgentSetter(String userAgent) implements Interceptor {
	@NotNull
	@Override
	public Response intercept(@NotNull Chain chain) throws IOException {
		return chain.proceed(chain.request().newBuilder()
			.header("User-Agent", userAgent)
			.build());
	}
}
