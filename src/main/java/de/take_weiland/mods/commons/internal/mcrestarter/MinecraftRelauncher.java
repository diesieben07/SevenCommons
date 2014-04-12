package de.take_weiland.mods.commons.internal.mcrestarter;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author diesieben07
 */
public class MinecraftRelauncher {

	public static final String UPDATE_INFO_FILE = "_sevencommons_updaterinfo.dat";
	public static final String UPDATE_MARKER_POSTFIX = "._sc_updated";
	public static final String BACKUP_POSTFIX = ".backup";

	public static void main(String[] args) throws InterruptedException {
		if (args.length != 2) {
			throw new IllegalArgumentException("Invalid Arguments!");
		}
		File[] modFolders = new File[2];
		modFolders[0] = new File(args[0]);
		modFolders[1] = new File(args[1]);

		File tempDataFile = new File(modFolders[0], UPDATE_INFO_FILE);
		if (!tempDataFile.isFile() || !tempDataFile.canRead()) {
			throw new IllegalArgumentException("Missing Updater info file!");
		}

		List<String> mcRelaunchCommand = new ArrayList<>();
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(tempDataFile)))) {
			in.readUTF(); // get rid of the temporary jar file, only the relaunched minecraft needs it
			File watcher = new File(in.readUTF());
			if (watcher.exists()) {
				System.out.println("Watcher file exists, waiting for Minecraft to die...");
				waitForDeletion(watcher);
			}
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
			System.out.println("Minecraft dead, continueing.");
			int numArgs = in.readInt();
			for (int i = 0; i < numArgs; ++i) {
				mcRelaunchCommand.add(in.readUTF());
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to read data file!", e);
		}
		updateMods(modFolders);
		try {
			launch(mcRelaunchCommand);
		} catch (IOException e) {
			System.err.println("Failed to relaunch Minecraft.");
			e.printStackTrace();
		}
	}

	private static void waitForDeletion(File file) throws IOException, InterruptedException {
		Path path = file.toPath();
		Path directory = path.getParent();
		WatchService ws = directory.getFileSystem().newWatchService();
		directory.register(ws, StandardWatchEventKinds.ENTRY_DELETE);
		do {
			ws.take().pollEvents();
		} while (file.exists());
	}

	private static void updateMods(File[] modFolders) {
		for (File modFolder : modFolders) {
			File[] updatableMods = modFolder.listFiles(UpdatedModsFilter.INSTANCE);
			if (updatableMods == null) {
				continue;
			}
			for (File mod : updatableMods) {
				try {
					File backup = new File(mod.getPath() + BACKUP_POSTFIX);
					Files.move(mod.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);

					File marker = new File(mod.getPath() + UPDATE_MARKER_POSTFIX);
					Files.delete(marker.toPath());

				} catch (IOException e) {
					System.err.println(String.format("IOException during final update for mod %s", mod.getName()));
					e.printStackTrace();
				}
			}
		}
	}

	// inspired from http://java.dzone.com/articles/programmatically-restart-java

	public static void launch(List<String> command) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.inheritIO();
		builder.start();
	}

	public static void launchJarFile(File jarFile, List<String> args) throws IOException {
		List<String> command = new ArrayList<>();
		command.add(findJavaBinary());
		command.add("-jar");
		command.add("\"" + jarFile.getAbsolutePath() + "\"");
		command.addAll(args);
		launch(command);
	}

	public static String findJavaBinary() {
		String javaBinary = System.getProperty("java.home") + "/bin/javaw";

		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			javaBinary += ".exe";
		}

		if (!new File(javaBinary).canExecute()) {
			throw new IllegalStateException("Can't determine java binary!");
		}
		return javaBinary;
	}

	public static List<String> findRelaunchCommand() {


		final List<String> command = new ArrayList<>();

		String javaBinary = findJavaBinary();

		// java binary
		command.add("\"" + javaBinary + "\"");

		// vm arguments
		List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

		// if it's the agent argument : we ignore it otherwise the
		// address of the old application and the new one will be in conflict
		for (String vmArg : vmArguments) {
			if (!vmArg.contains("-agentlib")) {
				command.add(vmArg);
			}
		}

		// program main and program arguments
		String sunJavaCommand = System.getProperty("sun.java.command");
		if (sunJavaCommand == null) {
			throw new IllegalStateException("sun.java.command not defined!");
		}

		String[] mainCommand = sunJavaCommand.split(" ");

		String mainCommandFirst = mainCommand[0];

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
		command.addAll(Arrays.asList(mainCommand).subList(1, mainCommand.length));
		return command;
	}

	public static enum UpdatedModsFilter implements FileFilter {

		INSTANCE;

		@Override
		public boolean accept(File pathname) {
			if (!pathname.getPath().endsWith(".jar") && !pathname.getPath().endsWith(".zip")) {
				return false;
			}
			File marker = new File(pathname.getPath() + UPDATE_MARKER_POSTFIX);
			return marker.exists() && marker.isFile() && marker.canRead();

		}
	}
}
