package de.take_weiland.mods.commons;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import cpw.mods.fml.common.launcher.FMLTweaker;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.MathHelper;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>An automatic downloader for SevenCommons for you to ship with your mod.</p>
 * <p>You should copy this entire File and change the package name to your package.
 * The only thing to be changed is the {@link #DESIRED_VERSION} constant.</p>
 * <p>If your Mod is a Coremod (IFMLLoadingPlugin), call {@link #setup()} from the constructor of your IFMLLoadingPlugin.</p>
 * <p>If your Mod is a normal mod, add the following lines to the Jar-Manifest of your jar file:
 * <pre>FMLCorePlugin: com.example.yourmod.SevenCommonsWrapper<br />FMLCorePluginContainsFMLMod: true</pre></p>
 *
 * @author diesieben07
 */
public class SevenCommonsWrapper implements IFMLLoadingPlugin {

	private static final String DESIRED_VERSION = "0.1";

	public static void setup() {
		if ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
			throw new RuntimeException("Development environment detected! You should not use the SevenCommonsWrapper there!");
		}
		Properties props = System.getProperties();
		boolean loaded = props.get(SYS_PROP_INSTANCE) != null;
		if (loaded) {
			System.out.println("SevenCommons already loaded!");
			String version = (String) props.get(SYS_PROP_VERSION);
			if (version == null || !DESIRED_VERSION.equals(version)) {
				throw new IllegalStateException("Conflicting SevenCommons Versions: " + version + " and " + DESIRED_VERSION);
			}
		} else {
			// find the FML tweaker and get the MC dir from it
			File mcDir = null;
			@SuppressWarnings("unchecked")
			List<ITweaker> allTweaks = (List<ITweaker>) Launch.blackboard.get("Tweaks");
			for (ITweaker tweaker : allTweaks) {
				if (tweaker instanceof FMLTweaker) {
					mcDir = ((FMLTweaker) tweaker).getGameDir();
					break;
				}
			}
			if (mcDir == null) {
				throw new IllegalStateException("Couldn't find FMLTweaker, GameDir is not available!");
			}
			tryDownload(mcDir);
		}
	}

	// coremod implementation

	public SevenCommonsWrapper() {
		setup();
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) { }

	// Private implementation

	public static final String SYS_PROP_VERSION = "de.take_weiland.sevencommons.version";
	public static final String SYS_PROP_INSTANCE = "de.take_weiland.sevencommons.instance";
	private static final String SC_MAIN_CLASS = "de.take_weiland.mods.commons.internal.SevenCommons";

	private static void tryDownload(File mcDir) {
		String version = "0.1";
		try {
			File downloaded = download(mcDir, version);
			LaunchClassLoader cl = Launch.classLoader;
			cl.addURL(downloaded.toURI().toURL());
			CoreModManager.getLoadedCoremods().add(downloaded.getName());

			Method loadCoremod = CoreModManager.class.getDeclaredMethod("loadCoreMod", LaunchClassLoader.class, String.class, File.class);
			loadCoremod.setAccessible(true);
			loadCoremod.invoke(null, Launch.classLoader, SC_MAIN_CLASS, downloaded);
		} catch (Exception e) {
			System.out.println("Failed to download SevenCommons Version " + version + ".");

			System.out.println();
			System.out.println("Halting game...");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static File download(File mcDir, String version) throws Exception {
		File modFolder = new File(mcDir + "/mods");
		if (!modFolder.mkdirs() && !modFolder.isDirectory()) {
			throw new IOException("Couldn't create mods folder " + modFolder.getAbsolutePath());
		}

		System.out.println("Finding Download URL for SevenCommons Version " + version + "...");

		String encVersion = URLEncoder.encode(version, "UTF-8");
		String repo = URLEncoder.encode("http://maven.take-weiland.de", "UTF-8");
		String group = URLEncoder.encode("de.take_weiland.mods.commons", "UTF-8");
		String artifact = URLEncoder.encode("SevenCommons", "UTF-8");
		String addInfoURL = URLEncoder.encode("http://mods.take-weiland.de/info.json", "UTF-8");
		final URL infoUrl =	new URL("http://sc-versions.take-weiland.de/?action=maven"
						+ "&repo=" + repo
						+ "&group=" + group
						+ "&artifact=" + artifact
						+ "&version=" + encVersion
						+ "&additionalInfoURL=" + addInfoURL
						+ "&param=url");


		String targetURL = CharStreams.toString(new InputSupplier<Reader>() {
			                                        @Override
			                                        public Reader getInput() throws IOException {
				                                        return new InputStreamReader(infoUrl.openStream());
			                                        }
		                                        });

		if (targetURL.toLowerCase().startsWith("error: ")) {
			throw new IOException("Failed to get Download URL (" + targetURL + ") !");
		}

		URL parsedTarget = new URL(targetURL.trim());
		System.out.println("Found URL: " + parsedTarget);

		System.out.println("Downloading SevenCommons Version " + version + "...");
		System.out.println();
		System.out.print("[");
		for (int i = 0; i < BARS; ++i) {
			System.out.print(" ");
		}
		System.out.print("]");

		File target = new File(modFolder, "SevenCommons-" + version + ".jar");
		WritableByteChannel out = new FileOutputStream(target).getChannel();
		URLConnection conn = parsedTarget.openConnection();
		long total = conn.getContentLengthLong();

		ReadableByteChannel in = Channels.newChannel(conn.getInputStream());
		in = new MonitorChannel(in, total);
		ByteStreams.copy(in, out);
		in.close();
		out.close();
		return target;
	}

	private static final int BARS = 30;

	private static class MonitorChannel implements ReadableByteChannel {

		private final ReadableByteChannel delegate;
		private final long total;
		private long alreadyRead = 0;
		private int lastDisplay = -1;

		MonitorChannel(ReadableByteChannel delegate, long total) {
			this.delegate = delegate;
			this.total = total;
		}

		@Override
		public int read(ByteBuffer dst) throws IOException {
			int res = delegate.read(dst);
			if (total > 0) {
				alreadyRead += res;
				int percent = MathHelper.floor_float(((float) alreadyRead / (float) total) * (float) BARS);
				if (lastDisplay != percent) {
					lastDisplay = percent;
					System.out.print("\r[");
					for (int i = 0; i < percent; ++i) {
						System.out.print("=");
					}
					for (int i = 1; i < BARS - percent; ++i) {
						System.out.print(" ");
					}
					System.out.print("]");
				}
			}
			return res;
		}

		@Override
		public boolean isOpen() {
			return delegate.isOpen();
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}
}
