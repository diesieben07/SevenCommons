package de.take_weiland.mods.commons.crash;

import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.crash.CrashReportCategory;

import java.util.concurrent.Callable;

/**
 * <p>Helper class for working with CrashReports.</p>
 * @author diesieben07
 */
public final class Crashing {

	/**
	 * <p>Apply an {@code UncaughtExceptionHandler} to the given Thread, which will cause any uncaught Exceptions from
	 * the Thread to cause a CrashReport in the main Minecraft Thread.</p>
	 * @param thread the Thread
	 * @return the same Thread that was passed in, for convenience
	 */
	public static <T extends Thread> T applyExceptionHandler(T thread) {
		thread.setUncaughtExceptionHandler(CrashExceptionHandler.INSTANCE);
		return thread;
	}

	/**
	 * <p>Append the given Objects to the category with the given name. If the array contains a single {@code Callable}
	 * it will be called for the actual value. Any Exception thrown during that process will be appended instead.
	 * Otherwise the value will be converted to a String representation using
	 * {@link de.take_weiland.mods.commons.util.JavaUtils#toString(Object)}.</p>
	 * @param category the category
	 * @param name the name for the value
	 * @param value the value
	 */
	public static void addSection(CrashReportCategory category, String name, final Object... value) {
		if (value.length == 1) {
			Object single = value[0];
			if (single instanceof Callable) {
				try {
					addSection(category, name, ((Callable<?>) single).call());
				} catch (Throwable t) {
					category.addCrashSectionThrowable(name, t);
				}
			} else {
				category.addCrashSection(name, JavaUtils.toString(single));
			}
		} else {
			category.addCrashSection(name, JavaUtils.toString(value));
		}
	}

	private Crashing() { }

}
