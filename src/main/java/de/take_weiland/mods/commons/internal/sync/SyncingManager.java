package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.sync.PropertySyncer;
import de.take_weiland.mods.commons.sync.PropertyWatcher;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class SyncingManager {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncingManager";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String CREATE_SYNCER = "createSyncer";
	public static final String CREATE_WATCHER = "createWatcher";

	private static final Map<Class<?>, Class<? extends PropertyWatcher<?>>> watchers = Maps.newHashMap();
	private static final Map<Class<?>, Class<? extends PropertySyncer<?>>> syncers = Maps.newHashMap();

	public static <T> void registerSyncer(@org.jetbrains.annotations.NotNull Class<T> clazz, @org.jetbrains.annotations.NotNull Class<? extends PropertySyncer<T>> syncer) {
		checkArgument(!syncers.containsKey(clazz), "PropertySyncer for class %s already registered", clazz.getName());
		checkArgument(!watchers.containsKey(clazz), "Cannot register a PropertySyncer for class %s, a PropertyWatcher has already been registered", clazz.getName());
		syncers.put(clazz, syncer);
	}

	public static <T> void registerWatcher(@org.jetbrains.annotations.NotNull Class<T> clazz, @org.jetbrains.annotations.NotNull Class<? extends PropertyWatcher<? extends T>> watcher) {
		checkArgument(!watchers.containsKey(clazz), "PropertyWatcher for class %s already registered", clazz.getName());
		checkArgument(!syncers.containsKey(clazz), "Cannot register a PropertyWatcher for class %s, a PropertySyncer has already been registered", clazz.getName());
		watchers.put(clazz, watcher);
	}

	public static CallSite inDyBootstrap(MethodHandles.Lookup callerLookup, String name, MethodType type, Class<?> toSync) throws NoSuchMethodException, IllegalAccessException {
		if (type.parameterCount() != 0) {
			throw invalidInDyCall();
		}
		MethodHandles.Lookup myLookup = MethodHandles.lookup();

		switch (name) {
			case CREATE_SYNCER:
				if (type.returnType() != PropertySyncer.class) {
					throw invalidInDyCall();
				}

				Class<?> syncer = syncers.get(toSync);
				if (syncer == null) {
					throw new RuntimeException("No PropertySyncer found for class " + toSync.getName());
				}
				return new ConstantCallSite(myLookup.findConstructor(syncer, methodType(void.class)).asType(methodType(PropertySyncer.class)));
			case CREATE_WATCHER:
				if (type.returnType() != PropertyWatcher.class) {
					throw invalidInDyCall();
				}

				Class<?> watcher = syncers.get(toSync);
				if (watcher == null) {
					watcher = lookupSuperWatcher(toSync);
				}
				return new ConstantCallSite(myLookup.findConstructor(watcher, methodType(void.class)).asType(methodType(PropertyWatcher.class)));
			default:
				throw invalidInDyCall();
		}
	}

	private static RuntimeException invalidInDyCall() {
		return new RuntimeException("Invalid InvokeDynamic call to syncerBootstrap!");
	}

	private static Class<?> lookupSuperWatcher(Class<?> clazz) {
		Class<? extends PropertyWatcher<?>> found = null;
		for (Map.Entry<Class<?>, Class<? extends PropertyWatcher<?>>> entry : watchers.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz)) {
				if (found != null) {
					throw new IllegalStateException("Multiple PropertyWatchers for clazz " + clazz.getName());
				}
				found = entry.getValue();
			}
		}
		if (found == null) {
			throw new IllegalStateException(String.format("No PropertyWatcher found for class %s", clazz.getName()));
		}
		watchers.put(clazz, found);
		return found;
	}


}
