package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UpdateControllerLocal extends AbstractUpdateController {

	static final List<String> INTERNAL_MODS = Arrays.asList("mcp", "forge", "fml", "minecraft");
	
	private static final String LOG_CHANNEL = "Sevens ModUpdater";
	public static final Logger LOGGER;
	
	static {
		FMLLog.makeLog(LOG_CHANNEL);
		LOGGER = Logger.getLogger(LOG_CHANNEL);
	}
	
	private final ExecutorService executor;

	public UpdateControllerLocal() {
		List<UpdatableMod> mods = Lists.transform(Loader.instance().getActiveModList(), new Function<ModContainer, UpdatableMod>() {
			@Override
			public UpdatableMod apply(ModContainer container) {
				try {
					if (INTERNAL_MODS.contains(container.getModId().toLowerCase())) {
						return new FMLInternalMod(container, UpdateControllerLocal.this);
					} else {
						return new ModsFolderMod(container, UpdateControllerLocal.this);
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
		
		executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("Sevens ModUpdater %d").build());
	}
	
	@Override
	public void searchForUpdates(UpdatableMod mod) {
		validate(mod);
		if (mod.transition(ModUpdateState.CHECKING)) {
			executor.execute(new TaskSearchUpdates(mod));
		}
	}
	
	@Override
	public void update(UpdatableMod mod, ModVersion version) {
		validate(mod);
		if (mod.transition(ModUpdateState.DOWNLOADING)) {
			executor.execute(new TaskInstallUpdate(mod, version));
		}
	}
	
	private void validate(UpdatableMod mod) {
		if (!mods.containsKey(mod.getModId())) { // check key here, its faster
			throw new IllegalArgumentException(String.format("Mod %s not valid for this UpdateController!", mod.getModId()));
		}
	}

	@Override
	public boolean restartMinecraft() {
		// inspired from http://java.dzone.com/articles/programmatically-restart-java
		
		final List<String> command = Lists.newArrayList();

		String javaBinary = System.getProperty("java.home") + "/bin/java";

		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			javaBinary += ".exe";
		}
		
		if (!new File(javaBinary).canExecute()) {
			return false;
		}

		// java binary
		command.add("\"" + javaBinary + "\"");

		// vm arguments
		List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

		// if it's the agent argument : we ignore it otherwise the
		// address of the old application and the new one will be in conflict
		command.addAll(Collections2.filter(vmArguments, new Predicate<String>() {

			@Override
			public boolean apply(String arg) {
				return !arg.contains("-agentlib");
			}
			
		}));
		
		// program main and program arguments
		String sunJavaCommand = System.getProperty("sun.java.command");
		if (sunJavaCommand == null) {
			return false;
		}

		Iterator<String> mainCommand = Splitter.on(' ').omitEmptyStrings().trimResults().split(sunJavaCommand).iterator();

		if (!mainCommand.hasNext()) {
			return false;
		}
		
		String mainCommandFirst = mainCommand.next();

		// program main is a jar
		if (mainCommandFirst.endsWith(".jar")) {
			// if it's a jar, add -jar mainJar
			command.add("-jar");
			command.add("\"" + new File(mainCommandFirst).getPath() + "\"");
		} else {
			// else it's a .class, add the classpath and mainClass
			command.add("-cp");
			command.add("\"" + System.getProperty("java.class.path") + "\"");
			command.add(mainCommandFirst);
		}

		// finally add program arguments
		Iterators.addAll(command, mainCommand);

		System.out.println(Joiner.on(' ').join(command));
		
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.inheritIO();
			builder.start();
		} catch (IOException e) {
			return false;
		}
		
		SCModContainer.proxy.shutdownMinecraft();
		return true;
	}
	
	
	
}
