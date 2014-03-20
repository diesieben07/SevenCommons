package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.mcrestarter.MinecraftRelauncher;
import de.take_weiland.mods.commons.util.MiscUtil;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UpdateControllerLocal extends AbstractUpdateController {

	static final List<String> INTERNAL_MODS = Arrays.asList("mcp", "forge", "fml", "minecraft");
	
	private static final String LOG_CHANNEL = "Sevens ModUpdater";
	public static final Logger LOGGER = MiscUtil.getLogger(LOG_CHANNEL);
	
	private final ScheduledExecutorService executor;
	private final Map<String, URL> updateUrls;
	private int refreshCount = 0;

	public UpdateControllerLocal(Map<String, URL> updateUrls, int periodicChecks) {
		this.updateUrls = updateUrls;
		Iterable<? extends ModContainer> mc = Arrays.asList(Loader.instance().getMinecraftModContainer());
		Iterable<ModContainer> fmlMods = Iterables.concat(Loader.instance().getActiveModList(), mc);
		Iterable<UpdatableMod> mods = Iterables.transform(fmlMods, new Function<ModContainer, UpdatableMod>() {
			@Override
			public UpdatableMod apply(ModContainer container) {
				try {
					if (INTERNAL_MODS.contains(container.getModId().toLowerCase())) {
						return new FMLInternalMod(container, UpdateControllerLocal.this);
					} else {
						return new ModsFolderMod(container, UpdateControllerLocal.this.updateUrls.get(container.getModId()), UpdateControllerLocal.this);
					}
				} catch (Throwable t) {
					LOGGER.severe("Unexpected exception during UpdateableMod parsing!");
					LOGGER.severe("ModID: " + container.getModId());
					t.printStackTrace();
					return null;
				}
			}
		});

		this.mods = Maps.uniqueIndex(Iterables.filter(mods, Predicates.notNull()), ID_RETRIEVER);
		executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Sevens ModUpdater %d").build());

		if (periodicChecks > 0) {
			executor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					UpdateControllerLocal.this.searchForUpdates();
				}
			}, periodicChecks, periodicChecks, TimeUnit.MINUTES);
		}

		searchForUpdates();
	}

	private static final int OPTIMIZE_RETRIES = 10;

	@Override
	public void performInstall() {

	}

	@Override
	public boolean isRefreshing() {
		return refreshCount > 0;
	}

	@Override
	public void onStateChange(UpdatableMod mod, ModUpdateState oldState) {
		if (oldState != ModUpdateState.REFRESHING && mod.getState() == ModUpdateState.REFRESHING) {
			if (refreshCount++ == 0) {
				SCModContainer.proxy.refreshUpdatesGui();
			}
		} else if (oldState == ModUpdateState.REFRESHING && mod.getState() != ModUpdateState.REFRESHING) {
			if (--refreshCount == 0) {
				SCModContainer.proxy.refreshUpdatesGui();
			}
		}
	}

	@Override
	public boolean optimizeVersionSelection() {
		boolean changed;
		int count = 0;
		do {
			changed = false;
			for (UpdatableMod mod : mods.values()) {
				changed |= mod.getVersions().selectOptimalVersion();
			}
			count++;
		} while (changed || count >= OPTIMIZE_RETRIES);
		SCModContainer.proxy.refreshUpdatesGui();
		return !changed; // if nothing changed on the last run, we are successful
	}



	@Override
	public void searchForUpdates(UpdatableMod mod) {
		validate(mod);
		if (mod.getUpdateURL() != null && mod.transition(ModUpdateState.REFRESHING)) {
			executor.execute(new TaskSearchUpdates(mod));
		}
	}
	
	@Override
	public void update(UpdatableMod mod) {
		validate(mod);
		// TODO
//		executor.execute(new TaskInstallUpdate(mod, version));
	}
	
	private void validate(UpdatableMod mod) {
		if (!mods.containsKey(mod.getModId())) { // check key here, its faster
			throw new IllegalArgumentException(String.format("Mod %s not valid for this UpdateController!", mod.getModId()));
		}
	}

	private static final String RELAUNCHER_MANIFEST = "Main-Class: de.take_weiland.mods.commons.internal.mcrestarter.MinecraftRelauncher\n";

	@Override
	public boolean restartMinecraft() {
//		if (MiscUtil.isDevelopmentEnv()) {
//			LOGGER.warning("Can't restart in development environment!");
//			return false;
//		}
		File modFolder = new File(SevenCommons.MINECRAFT_DIR, "mods");
		File modFolder2 = new File(modFolder, SevenCommons.MINECRAFT_VERSION);
		File tempFile = new File(modFolder, MinecraftRelauncher.UPDATE_INFO_FILE);
		Path tempJar = null;
		Path tempWatcherFile = null;
		try {
			List<String> command = MinecraftRelauncher.findRelaunchCommand();
			tempWatcherFile = Files.createTempFile("SevenCommons_MinecraftRunMarker", ".tmp");
			tempJar = Files.createTempFile("SevenCommons_MinecraftRelauncher", ".jar");
			try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)))) {
				out.writeUTF(tempJar.toAbsolutePath().toString());
				out.writeUTF(tempWatcherFile.toAbsolutePath().toString());
				out.writeInt(command.size());
				for (String c : command) {
					out.writeUTF(c);
				}

				out.writeInt(2);
			}
			String relauncherClassSource = MinecraftRelauncher.class.getName().replace('.', '/') + ".class";
			String innerClassSource = MinecraftRelauncher.UpdatedModsFilter.class.getName().replace('.', '/') + ".class";
			try (InputStream in = MinecraftRelauncher.class.getClassLoader().getResourceAsStream(relauncherClassSource);
			     InputStream in2 = MinecraftRelauncher.class.getClassLoader().getResourceAsStream(innerClassSource);
			     ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempJar.toFile()))) {

				out.putNextEntry(new ZipEntry(relauncherClassSource));
				ByteStreams.copy(in, out);
				out.closeEntry();

				out.putNextEntry(new ZipEntry(innerClassSource));
				ByteStreams.copy(in2, out);
				out.closeEntry();

				out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
				out.write(RELAUNCHER_MANIFEST.getBytes(Charsets.UTF_8));
				out.closeEntry();
			}
			String[] args = new String[2];
			args[0] = "\"" + modFolder.getAbsolutePath() + "\"";
			args[1] = "\"" + modFolder2.getAbsolutePath() + "\"";

			System.out.println("Temp file: " + tempFile);
			System.out.println("Temp jar file:" + tempJar);
			System.out.println("Temp watcher file: " + tempWatcherFile);
			System.out.println("args: " + Joiner.on(' ').join(args));

			tempWatcherFile.toFile().deleteOnExit();

			MinecraftRelauncher.launchJarFile(tempJar.toFile(), Arrays.asList(args));
		} catch (IOException e) {
			tryDelete(tempFile.toPath());
			tryDelete(tempJar);
			tryDelete(tempWatcherFile);
			LOGGER.warning("Failed to restart Minecraft automatically.");
			e.printStackTrace();
			return false;
		}

		SCModContainer.proxy.shutdownMinecraft();
		return true;
	}
	
	@SuppressWarnings("EmptyCatchBlock")
	private static void tryDelete(Path path) {
		try {
			if (path != null) {
				Files.delete(path);
			}
		} catch (IOException e) { }
	}
	
}
