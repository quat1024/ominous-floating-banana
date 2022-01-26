package agency.highlysuspect.ominousfloatingbanana;

import agency.highlysuspect.ominousfloatingbanana.curse.ForgeSvc;
import agency.highlysuspect.ominousfloatingbanana.curse.types.CurseManifest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(mixinStandardHelpOptions = true, version = "0", subcommands = {
	Cli.DownloadManifest.class
})
public class Cli {
	@SuppressWarnings("InstantiationOfUtilityClass")
	public static void main(String[] args) {
		System.exit(new CommandLine(new Cli()).execute(args));
	}
	
	@Command(name = "download", mixinStandardHelpOptions = true)
	public static class DownloadManifest implements Runnable {
		@Option(names = "manifest", description = "Path to a Curse-format manifest.json file.")
		Path manifestPath = Paths.get(".ofb/manifest.json");
		
		@Option(names = "output", description = "mods/ folder that downloaded mods will be output into.")
		Path outputPath = Paths.get("mods/");
		
		@Override
		public void run() {
			try {
				CurseManifest manifest = CurseManifest.read(manifestPath);
				System.out.println(outputPath.toAbsolutePath());
				Files.createDirectories(outputPath);
				
				for(CurseManifest.File file : manifest.files) {
					ForgeSvc.downloadMod(Init.OKHTTP, file.projectID, file.fileID, outputPath);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
