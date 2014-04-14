package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.internal.PacketDownloadPercent;
import de.take_weiland.mods.commons.internal.PacketVersionSelect;
import de.take_weiland.mods.commons.internal.ServerProxy;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.mcrestarter.MinecraftRelauncher;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.MiscUtil;
import net.minecraft.util.MathHelper;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UpdateControllerLocal extends AbstractUpdateController {

	static final Set<String> INTERNAL_MODS = ImmutableSet.of("mcp", "forge", "fml", "minecraft");
	
	public static final Logger LOGGER = SevenCommons.scLogger("Updater");
	
	private final ScheduledExecutorService executor;
	private final Map<String, URL> updateUrls;
	private final  AtomicLong pendingDownloadBytes = new AtomicLong();
	private final AtomicLong downloadBytesComplete = new AtomicLong();

	private final Set<Path> cleanupPaths = Collections.newSetFromMap(new ConcurrentHashMap<Path, Boolean>());

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
					searchForUpdates();
				}
			}, periodicChecks, periodicChecks, TimeUnit.MINUTES);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				cleanupIfFailure();
			}
		}));

		searchForUpdates();
	}

	@Override
	public void performInstall() {
		if (isRefreshing()) {
			return;
		}
		for (UpdatableMod mod : getMods()) {
			ModVersion version = mod.getVersions().getSelectedVersion();
			if (!version.isInstalled() && mod.getState().canTransition(ModUpdateState.INSTALLING)) {
				ModVersionCollection info = mod.getVersions();
				if (info == null || !info.isInstallable(version)) {
					throw new IllegalArgumentException(String.format("Version %s is not available for mod %s", version.getModVersion(), mod.getModId()));
				}
				mod.transition(ModUpdateState.INSTALLING);
				executor.execute(new TaskInstallUpdate(this, mod, version));
			}
		}
	}

	@Override
	public void onStateChange(UpdatableMod mod, ModUpdateState oldState) {
		super.onStateChange(mod, oldState);
		if (modsInState(ModUpdateState.INSTALLING) == 0 && oldState == ModUpdateState.INSTALLING) {
			// cleanup if we finished installing and there was a problem
			cleanupIfFailure();
		}
		SCModContainer.proxy.refreshUpdatesGui();
	}

	void cleanupIfFailure() {
		if (!cleanupPaths.isEmpty() && hasFailed()) {
			cleanup();
		}
	}

	private void cleanup() {
		for (Iterator<Path> it = cleanupPaths.iterator(); it.hasNext();) {
			Path path = it.next();
			if (Files.isRegularFile(path)) {
				tryDelete(path);
			}
			it.remove();
		}
	}

	private static final int OPTIMIZE_RETRIES = 10;

	@Override
	public void optimizeVersionSelection() {
		boolean changed;
		int count = 0;
		do {
			changed = false;
			for (UpdatableMod mod : mods.values()) {
				changed |= mod.getVersions().selectOptimalVersion();
			}
			count++;
		} while (changed && count <= OPTIMIZE_RETRIES);
		SCModContainer.proxy.refreshUpdatesGui();
		if (changed) {
			SCModContainer.proxy.displayOptimizeFailure();
		}
	}

	@Override
	public void searchForUpdates() {
		for (UpdatableMod mod : mods.values()) {
			if (mod.getUpdateURL() != null && mod.transition(ModUpdateState.REFRESHING)) {
				executor.execute(new TaskSearchUpdates(mod));
			}
		}
	}

	@Override
	public int getDownloadPercent() {
		return MathHelper.floor_float(((float) downloadBytesComplete.get() / (float) pendingDownloadBytes.get()) * 100);
	}

	@Override
	public void resetFailure() {
		if (!isInstalling() && hasFailed()) {
			cleanup();
			for (UpdatableMod mod : getMods()) {
				if (mod.getState() == ModUpdateState.INSTALL_FAIL || mod.getState() == ModUpdateState.INSTALL_OK) {
					mod.transition(ModUpdateState.AVAILABLE);
				}
			}
			pendingDownloadBytes.set(0);
			downloadBytesComplete.set(0);
		}
	}

	public void modifyPendingBytes(long delta) {
		pendingDownloadBytes.addAndGet(delta);
		resendRemotePercent();
	}

	public void onBytesDownloaded(long bytes) {
		downloadBytesComplete.addAndGet(bytes);
		resendRemotePercent();
	}



	private void resendRemotePercent() {
		if (ServerProxy.currentUpdateViewer != null) {
			int percentNow = getDownloadPercent();
			if (ServerProxy.lastPercent != percentNow) {
				new PacketDownloadPercent(percentNow).sendTo(ServerProxy.currentUpdateViewer);
				ServerProxy.lastPercent = percentNow;
			}
		}
	}

	public void registerForFailureDeletion(Path file) {
		cleanupPaths.add(file);
	}

	public void writeMods(WritableDataBuf out) {
		out.putVarInt(getMods().size());
		for (UpdatableMod mod : getMods()) {
			out.putString(mod.getModId());
			out.putString(mod.getName());
			out.putBoolean(mod.isInternal());

			writeArtifactVersion(out, mod.getVersions().getCurrentVersion().getModVersion());
			writeVersions(out, mod.getVersions());
		}

		writeStateCounts(out);
	}

	private void writeStateCounts(WritableDataBuf out) {
		for (int count : stateCount) {
			out.putVarInt(count);
		}
	}

	private void writeVersions(WritableDataBuf out, ModVersionCollection versions) {
		List<ModVersion> allVersions = versions.getAvailableVersions();
		out.putVarInt(allVersions.size() - 1);
		for (ModVersion v : allVersions) {
			if (v == versions.getCurrentVersion()) {
				continue;
			}
			v.write(out);
		}
		out.putVarInt(versions.getSelectedVersionIndex());
	}

	private void writeDependencies(WritableDataBuf out, List<Dependency> dependencies) {
		out.putVarInt(dependencies.size());
		for (Dependency dep : dependencies) {
			dep.write(out);
		}
	}

	private void writeArtifactVersion(WritableDataBuf out, ArtifactVersion v) {
		out.putString(v.getLabel());
		out.putString(v.getVersionString());
	}

	@Override
	public void onVersionSelect(UpdatableMod mod, int index) {
		if (ServerProxy.currentUpdateViewer != null) {
			new PacketVersionSelect(mod.getModId(), index).sendTo(ServerProxy.currentUpdateViewer);
		}
	}

	private static final String RELAUNCHER_MANIFEST = "Main-Class: de.take_weiland.mods.commons.internal.mcrestarter.MinecraftRelauncher\n";

	@Override
	public void restartMinecraft() {
		if (MiscUtil.isDevelopmentEnv()) {
			LOGGER.warning("Can't restart in development environment!");
			SCModContainer.proxy.displayRestartFailure();
			return;
		}
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
			SCModContainer.proxy.displayRestartFailure();
			return;
		}

		SCModContainer.proxy.shutdownMinecraft();
	}
	
	@SuppressWarnings("EmptyCatchBlock")
	private static void tryDelete(Path path) {
		try {
			if (path != null) {
				Files.delete(path);
				System.out.println("Deleted " + path);
			}
		} catch (IOException e) { }
	}

}
