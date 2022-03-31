package agency.highlysuspect.ominousfloatingbanana;

import agency.highlysuspect.ominousfloatingbanana.net.RateLimiter;
import agency.highlysuspect.ominousfloatingbanana.net.UserAgentSetter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
	name = Cli.INVOCATION,
	mixinStandardHelpOptions = true,
	version = Cli.VERSION,
	description = Cli.DESCRIPTION
)
public class Cli {
	public static final String INVOCATION = "java -jar ominous-floating-banana.jar";
	public static final String NAME = "Ominous Floating Banana";
	public static final String VERSION = "0.0.0.0.0.0.0";
	public static final String DESCRIPTION = "Curse file download utility";
	
	public static void main(String[] args) {
		System.out.println(NAME + " " + VERSION + " - " + DESCRIPTION);
		
		//noinspection InstantiationOfUtilityClass
		Cli cli = new Cli();
		
		System.exit(new CommandLine(cli)
			.addSubcommand("sync", new SyncCommand())
			.execute(args));
	}
	
	@CommandLine.Command(mixinStandardHelpOptions = true, description = "Download a manifest full of mods.")
	public static class SyncCommand implements Callable<Integer> {
		@CommandLine.Option(
			names = "--manifest-path",
			description = "Path to manifest file.",
			defaultValue = "ofb-manifest.json",
			showDefaultValue = CommandLine.Help.Visibility.ALWAYS
		)
		public Path manifestPath;
		
		@CommandLine.Option(
			names = "--destination-path",
			description = "Where mods will be written to.",
			defaultValue = "mods/",
			showDefaultValue = CommandLine.Help.Visibility.ALWAYS
		)
		public Path destination;
		
		@CommandLine.Option(
			names = "--dry-run",
			description = {
				"Don't download any of the mod files to disk.",
				"Note that many HTTP requests are still performed."
			}
		)
		public boolean dryRun;
		
		@CommandLine.Option(
			names = "--rate-limit",
			description = "Minimum time (in milliseconds) between HTTP requests.",
			defaultValue = "1000"
		)
		public long rateLimit;
		
		@CommandLine.Option(
			names = "--user-agent",
			description = "Sets the HTTP User-Agent.",
			defaultValue = "Ominous Floating Banana - https://github.com/quat1024/ominous-floating-banana/"
		)
		public String userAgent;
		
		@Override
		public Integer call() throws Exception {
			//(picocli is kind of weird...)
			manifestPath = manifestPath.toAbsolutePath();
			destination = destination.toAbsolutePath();
			
			OkHttpClient.Builder builder = new OkHttpClient.Builder()
				//Rate-limit requests and set a user-agent
				.addInterceptor(new RateLimiter(rateLimit))
				.addInterceptor(new UserAgentSetter(userAgent));
			
			OkHttpClient client = builder.build();
			
			OfbManifest manifest = OfbManifest.fromFile(createGson(), manifestPath);
			manifest.performDownload(client, destination, dryRun);
			
			return 0;
		}
	}
	
	public static void log(String bla) {
		System.out.println(bla);
	}
	
	public static void warn(String bla) {
		System.err.println(bla);
	}
	
	public static Gson createGson() {
		return new GsonBuilder().create();
	}
}
