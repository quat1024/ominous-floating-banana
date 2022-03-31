Ominous Floating Banana
=======================

Tool that manages a folder full of mods downloaded from CurseForge and *nothing else*. By "minimalist", I mean "it is missing a lot of features and is sort of a pain to use".

![high constrast banana (scary)](./crap/banana.jpg)

It is a "modpack development tool" as far as "it helps you check a modpack into Git without checking in a zillion `.jar` files then gets out of your way to allow you to work.".

It:

* reads a manifest file from `./ofb-manifest.json` that contains a list of Curse project/file IDs,
* downloads the files into `./mods/`.
* That's it. It doesn't do anything else.

It is *not*:

* a "Curse modpack downloader";
* a tool to download and configure a mod *loader*;
* a tool to download a mod given only its name or slug;
* a tool to automatically check for updates or update mods to their latest version;
* a tool to download mods from services other than Curse;
* a tool to distribute a self-hosted modpack to players.

It also does not have an exporter. That... would probably be a good idea to add eventually. (This was always meant to be something you run directly from a launcher instance's directory, so you can use your launcher's instance-exporting commands to get things done for now.)

If you are looking for a tool that does some (or all) of those things, I recommend you check comp's [Big List of Modpack Things](https://gist.github.com/comp500/13ae6f058221196077fb19953ac608c7) and find a more suitable tool.

# Usage

(Well first I have to figure out how to compile it; you'd probably have an `ominous-floating-banana.jar`, right?)

0. Gitignore your `mods/` folder.
1. Check that `java --version` refers to Java 16 or newer on your computer.
2. Place `ominous-floating-banana.jar` *inside* the Minecraft instance directory.
   * It should go next to `options.txt` and be *adjacent* to all the folders like `run`, `mods` etc.
   * You don't have to gitignore it (think of it like a Gradle wrapper)
3. Next to it, place a manifest file named `ofb-manifest.json`, see below for what these are.
4. Running `java -jar ominous-floating-banana.jar sync` will proceed to synchronize the contents of the `mods/` directory with what is specified in the manifest.

It is very noisy and logs a whole bunch of stuff.

Main knobs to turn:

* `--manifest-path ./some/file.json` changes the manifest file (defaults to `./ofb-manifest.json`)
* `--destination-path ./some/other/path` changes the mods directory (it will be created if it does not exist, defaults to `./mods`)
* `--dry-run` will have OFB retrieve and log download URLs for all the mods, but won't actually sync them

It is a [picocli CLI](https://picocli.info/), so picocli features are available too, like `--help`.

# Notes on syncing

OFB really, *really* tries to avoid accessing the Curse API if it doesn't have to. You may have to babysit it a little.

* The biggest "gotcha" is that OFB does not check anything relating to file hashes at all.
  * If the manifest states that a given projectID/fileID pair downloads a file named `coolmod-1.18.1.jar`, and a file named `coolmod-1.18.1.jar` exists in the `./mods` folder, the Banana will **never** attempt to redownload it, *even if the file is modified on-disk*. The only way to redownload it is to manually delete it.
* `--rate-limit 500` sets the minimum time between starting new requests to 500 milliseconds.
  * The default rate limit is **one second** between requests. I am aware this is a fairly long time, but I do not want to get you IP banned.
* `--user-agent "Totally Not A Curse Scraper"` sets the HTTP user-agent. Curse will never know what's up. Absolutely foolproof!
* It knows about the ".jar.disabled" convention for manually disabling mods and will not redownload any files disabled in that way.

# Manifest Format

`ofb-manifest.json` looks like a very small subset of the Curse manifest format, with everything except the mod files stripped out, and `"required": true` replaced by two additional parameters per mod.

```json
{
	"files": [
		{
			"name": "Botania",
			"filename": "Botania-1.18.1-429.jar",
			"projectID": 225643,
			"fileID": 3664223
		},
		{
			"name": "Patchouli",
			"filename": "Patchouli-1.18.2-66.jar",
			"projectID": 306770,
			"fileID": 3680301
		},
		{
			"name": "Curios",
			"filename": "curios-forge-1.18.2-5.0.6.3.jar",
			"projectID": 309927,
			"fileID": 3670447
		}
	]
}
```

* `name` is literally meaningless. It will be used in log messages and such, but it can be anything you want. Its main purpose is to make navigating the manifest file easier.
* As described above, if a file with the name specified in `filename` exists in the destination directory, OFB will make no attempt to redownload it. (This is the only caching mechanism.)
* `projectID` and `fileID` are interpreted as they are in a Curse manifest.

Even though the formats are "compatible", at the moment you shouldn't *actually* use a Curse manifest as input, b/c it doesn't have the `filename` information, which is critical for the way caching works (so OFB will just redownload the entire modpack every time, unless you manually populate the filenames). This may change in the future (an "import" command would be nice, I guess)

## Next steps

* Code cleanup.
* Prompt to delete files in `./mods` that aren't referenced in the manifest (bringing it closer to a full "sync", instead of just a "download", i guess)
* Cache the ((projectID, fileID) -> Curse download URL) lookups indefinitely. This would mean, after the first download, I don't need to store `filename` intrusively if I want a working don't-need-to-redownload-that-file feature. Which would make this a whole lot less brittle.

# Why is it called that?

I don't know.

## Building

I think you need to use `shade` or something, to get a runnable standaone jar without having to mess with java -cp? Not sure.