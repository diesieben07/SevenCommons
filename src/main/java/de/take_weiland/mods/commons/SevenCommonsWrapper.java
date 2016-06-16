package de.take_weiland.mods.commons;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.lang.String.format;

/**
 * <p>An automatic downloader for SevenCommons for you to ship with your mod.
 * To use it, ship this file with your mod with a changed package name.
 * If your Mod is a Coremod (IFMLLoadingPlugin), call {@link #setup()} from the constructor of your IFMLLoadingPlugin.
 * If your Mod is a normal mod, add the following lines to the Jar-Manifest of your jar file:<pre>
 * FMLCorePlugin: com.example.yourmod.SevenCommonsWrapper
 * FMLCorePluginContainsFMLMod: true</pre></p>
 * <p>Additionally add the following entry to your Jar-Manifest:<pre>
 * SevenCommonsVersion: {MajorVersion}
 * </pre></p>
 *
 * @author diesieben07
 */
@IFMLLoadingPlugin.MCVersion(SevenCommonsWrapper.MC_VERSION)
public final class SevenCommonsWrapper implements IFMLLoadingPlugin {

    public static void setup() {
        if (Launch.blackboard.containsKey(MARKER)) {
            return;
        }
        Launch.blackboard.put(MARKER, Boolean.TRUE);

        Set<Integer> reqVersions = allReqVersions();
        Set<Integer> presVersions = allPresVersions();
        if (reqVersions.size() != 1) {
            System.err.println("Multiple SevenCommons versions requested: " + reqVersions);
            System.exit(1);
        } else if (presVersions.size() >= 2) {
            System.err.println("Multiple SevenCommons versions installed: " + presVersions);
            System.exit(1);
        } else {
            int reqVersion = Iterables.getOnlyElement(reqVersions);
            if (presVersions.isEmpty()) {
                doDownload(reqVersion);
            } else {
                int presVersion = Iterables.getOnlyElement(presVersions);
                if (presVersion != reqVersion) {
                    System.err.println("Requested SevenCommons version was: " + reqVersion + ", but " + presVersion + " is installed.");
                } else {
                    System.out.println("Requested SevenCommons version already installed.");
                }
            }
        }
    }

    private static final String MARKER = "de.take_weiland.sevencommons.wrapper";
    private static final String REQ_VERSION_ENTRY = "SevenCommonsVersion";
    private static final String PRESENT_VERSION_ENTRY = "SevenCommonsInstalledVersion";
    static final String MC_VERSION = "1.6.4";
    private static final String JAR_REQUEST_URL = "http://mods.take-weiland.de/sevencommons_version.php?v=%d";
    private static final String SC_MAIN_CLASS = "de.take_weiland.mods.commons.internal.SevenCommonsLoader";

    private static ImmutableSet<Integer> allReqVersions() {
        return allVersions(getVersionFunc(REQ_VERSION_ENTRY));
    }

    private static ImmutableSet<Integer> allPresVersions() {
        return allVersions(getVersionFunc(PRESENT_VERSION_ENTRY));
    }

    private static ImmutableSet<Integer> allVersions(Function<JarFile, Integer> getVersionFunc) {
        return FluentIterable.from(getAllMods())
                .transform(getVersionFunc)
                .filter(Predicates.notNull())
                .toSet();
    }

    private static Integer getVersion(JarFile mod, String versionKey) {
        Manifest mf;
        try {
            mf = mod.getManifest();
        } catch (IOException e) {
            return null;
        }
        if (mf != null) {
            String version = mf.getMainAttributes().getValue(versionKey);
            if (version == null) {
                return null;
            } else {
                Integer i = Ints.tryParse(version);
                if (i == null || i < 0) {
                    throw new RuntimeException("Invalid SevenCommons version in JarFile: " + mod.getName());
                }
                return i;
            }

        } else {
            return null;
        }
    }

    private static Function<JarFile, Integer> getVersionFunc(final String key) {
        return input -> getVersion(input, key);
    }

    private static Iterable<JarFile> getAllMods() {
        File mcDir = getMCDir();
        File mods1 = new File(mcDir, "/mods/");
        File mods2 = new File(mods1, "/" + MC_VERSION + "/");
        Iterable<File> files = Iterables.concat(getMods(mods1), getMods(mods2));
        return Iterables.transform(files, input -> {
            try {
                return new JarFile(input);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private static Iterable<File> getMods(File modDir) {
        if (!modDir.isDirectory()) {
            return ImmutableList.of();
        } else {
            return Arrays.asList(modDir.listFiles((dir, name) -> {
                return name.toLowerCase().endsWith(".jar");
            }));
        }
    }

    private static File getMCDir() {
        return Launch.minecraftHome == null ? new File(".") : Launch.minecraftHome;
    }

    private static void doDownload(int majorVersion) {
        System.out.println("Downloading SevenCommons. Major Version: " + majorVersion);
        Downloader loader = new Downloader(majorVersion);
        loader.start();
        try {
            loader.join();
        } catch (InterruptedException e) {
            loader.interrupt();
            return;
        }
        if (loader.errored) {
            System.err.println("Download failed. Exiting.");
            System.exit(1);
        } else {
            loadSevenCommons(loader.target);
        }
    }

    private static void loadSevenCommons(File file) {
        try {
            Launch.classLoader.addURL(file.toURI().toURL());
            CoreModManager.getLoadedCoremods().add(file.getName());
            Method loadCoremod = CoreModManager.class.getDeclaredMethod("loadCoreMod", LaunchClassLoader.class, String.class, File.class);
            loadCoremod.setAccessible(true);
            loadCoremod.invoke(null, Launch.classLoader, SC_MAIN_CLASS, file);
        } catch (Exception e) {
            System.err.println("Failed to load SevenCommons as a Coremod!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static DownloadDisplay newDisplay(Thread downloadThread) {
        return new ConsoleDisplay(downloadThread);
    }

    private static class Downloader extends Thread {

        private final DownloadDisplay display;
        private final int majorVersion;
        boolean errored;
        File target;

        Downloader(int majorVersion) {
            this.majorVersion = majorVersion;
            display = newDisplay(this);
        }

        @Override
        public void run() {
            URL source = getDownloadURL(majorVersion);
            if (source == null) {
                return;
            }
            target = getTarget(source);
            if (!target.getParentFile().mkdirs()) {
                error("Failed to create mods folder: " + target.getParent());
                return;
            }
            ReadableByteChannel in = null;
            WritableByteChannel out = null;
            try {
                display.setup();
                URLConnection conn = source.openConnection();
                in = monitor(Channels.newChannel(conn.getInputStream()), conn.getContentLength());
                out = Channels.newChannel(new FileOutputStream(target));
                ByteStreams.copy(in, out);
            } catch (IOException e) {
                e.printStackTrace();
                error("Failed to download file: " + e.getMessage());
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }

        private ReadableByteChannel monitor(ReadableByteChannel channel, int size) {
            return new MonitorChannel(channel, display, size < 0 ? Integer.MAX_VALUE : size);
        }

        private URL getDownloadURL(int majorVersion) {
            String response = getContents(getRequestURLFor(majorVersion));
            if (response.startsWith("err:")) {
                error("Failed to get Download URL for major version " + majorVersion + ". " + response);
                return null;
            }
            try {
                return new URL(response);
            } catch (MalformedURLException e) {
                error("Got invalid download URL: " + response);
                return null;
            }
        }

        private static File getTarget(URL downloadURL) {
            String filename = Files.getNameWithoutExtension(downloadURL.getFile()) + ".jar";
            return new File(getMCDir(), "/mods/" + MC_VERSION + "/" + filename);
        }

        private static URL getRequestURLFor(int majorVersion) {
            try {
                return new URL(format(JAR_REQUEST_URL, majorVersion));
            } catch (MalformedURLException e) {
                throw Throwables.propagate(e);
            }
        }

        private static String getContents(final URL url) {
            try {
                return Resources.toString(url, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void error(String err) {
            display.displayError(err);
            errored = true;
        }
    }

    private static final class MonitorChannel implements ReadableByteChannel {

        private final ReadableByteChannel delegate;
        private final DownloadDisplay display;
        private final int size;
        private int read;

        MonitorChannel(ReadableByteChannel delegate, DownloadDisplay display, int size) {
            this.delegate = delegate;
            this.display = display;
            this.size = size;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int n = delegate.read(dst);
            if (n > 0) {
                read += n;
                display.displayProgress((double) read / (double) size);
            }
            return n;
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

    private static abstract class DownloadDisplay {

        final Thread downloadThread;

        DownloadDisplay(Thread downloadThread) {
            this.downloadThread = downloadThread;
        }

        final void stop() {
            downloadThread.interrupt();
        }

        abstract void setup();

        abstract void displayProgress(double progress);

        abstract void displayError(String err);

    }

    private static final class ConsoleDisplay extends DownloadDisplay {

        private int lastPercent = -1;
        private long lastTime;

        ConsoleDisplay(Thread downloadThread) {
            super(downloadThread);
        }

        @Override
        void setup() {
            displayProgress(0);
        }

        @Override
        void displayProgress(double progress) {
            int p = (int) Math.floor(progress * 100);
            if (p == lastPercent) {
                return;
            }
            long now = System.currentTimeMillis();
            if (p != 0 && p != 100 && Math.abs(lastTime - now) <= 500) {
                return;
            }
            lastTime = now;

            lastPercent = p;
            System.out.println(p + "% complete...");
        }

        @Override
        void displayError(String err) {
            System.out.println("Download failed with: " + err);
        }
    }

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
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }
}
