package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.*;
import de.take_weiland.mods.commons.internal.AnnotationNull;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.sync.*;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public class WatcherRegistry {

	private static Multimap<Class<?>, WatcherSPI> providerRegistry = ArrayListMultimap.create();

	static {
		SevenCommons.registerPostInitCallback(new Runnable() {
			@Override
			public void run() {
				synchronized (WatcherRegistry.class) {
					freeze();
				}
			}
		});
	}

	static void freeze() {
		providerRegistry = ImmutableListMultimap.copyOf(providerRegistry);
	}

	public static void register(Class<?> clazz, WatcherSPI spi) {
		synchronized (WatcherRegistry.class) {
			checkNotFrozen();
			providerRegistry.put(clazz, spi);
		}
	}

	private static void checkNotFrozen() {
		checkState(!isFrozen(), "Tried to register Watcher after postInit");
	}

	private static void checkFrozen() {
		checkState(isFrozen(), "Request for Watcher before postInit");
	}

	private static boolean isFrozen() {
		return providerRegistry instanceof ImmutableListMultimap;
	}

	static Watcher<?> findWatcher(PropertyMetadata metadata) throws ReflectiveOperationException {
		checkFrozen();
		Sync annotation = metadata.getAnnotation(Sync.class);
		checkState(annotation != null, "Invalid call to SyncASMHooks!");

		SerializationMethod syncMethod = annotation.method();
		boolean hasAs = annotation.as() != AnnotationNull.class;
		boolean hasWith = annotation.with() != AnnotationNull.class;

		if (hasAs && hasWith) {
			throw new IllegalArgumentException("Cannot specify as() and with() in @Sync!");
		}

		Watcher<?> watcher;
		if (hasWith) {
			watcher = findWatcherInstance(annotation.with(), syncMethod, metadata);
		} else {
			if (hasAs) {
				checkArgument(metadata.getRawType().isAssignableFrom(annotation.as()), "@Sync.as() must be subclass of field type");
				metadata = new MetadataOverrideType(metadata, annotation.as());
		}
			watcher = findWatcherByType(metadata, syncMethod);
		}
		if (watcher == null) {
			throw new IllegalArgumentException("Could not find watcher for " + metadata);
		}
		return watcher;
	}

	private static Watcher<?> findWatcherByType(PropertyMetadata metadata, SerializationMethod syncMethod) throws ReflectiveOperationException {
		Class<?> rawType = metadata.getRawType();
		Watcher<?> watcher = findWatcherInstance(rawType, syncMethod, metadata);
		if (watcher != null) {
			return watcher;
		}
		watcher = findWatcherWithProviders(metadata, syncMethod);
		return watcher;
	}

	private static Watcher<?> findWatcherWithProviders(PropertyMetadata metadata, SerializationMethod syncMethod) {
		Watcher<?> watcher = null;
		Class<?> type = metadata.getRawType();
		do {
			Collection<WatcherSPI> providers = providerRegistry.get(type);
			for (WatcherSPI provider : providers) {
				if (watcher == null) {
					watcher = provider.provideWatcher(metadata, syncMethod);
				} else {
					if (provider.provideWatcher(metadata, syncMethod) != null) {
						throw multipleWatchers(metadata);
					}
				}
			}
			type = type.getSuperclass();
		} while (type != null);
		return watcher;
	}

	private static Watcher<?> findWatcherInstance(Class<?> clazz, SerializationMethod syncContents, PropertyMetadata metadata) throws ReflectiveOperationException {
		ListMultimap<SerializationMethod, Member> membersByType = ArrayListMultimap.create(2, 1);

		for (Field field : clazz.getFields()) {
			WatcherFactory ann = field.getAnnotation(WatcherFactory.class);
			if (ann != null) {
				checkStatic(field);
				checkIsWatcher(field.getType(), field);
				field.setAccessible(true);
				for (SerializationMethod method : ann.methods()) {
					membersByType.put(method, field);
				}
			}
		}

		for (java.lang.reflect.Method method : clazz.getMethods()) {
			WatcherFactory ann = method.getAnnotation(WatcherFactory.class);
			if (ann != null) {
				checkStatic(method);
				checkIsWatcher(method.getReturnType(), method);
				Class<?>[] paramTypes = method.getParameterTypes();
				method.setAccessible(true);
				if (paramTypes.length != 0 && (paramTypes.length != 1 || paramTypes[0] != PropertyMetadata.class)) {
					throw new RuntimeException("@WatcherFactory with invalid argument types on " + method.getDeclaringClass().getName() + "." + method.getName());
				}
				for (SerializationMethod syncMethod : ann.methods()) {
					membersByType.put(syncMethod, method);
				}
			}
		}

		List<Member> matchedMembers = membersByType.get(syncContents);
		switch (matchedMembers.size()) {
			case 0:
				return null;
			case 1:
				return getWatcherFromMember(Iterables.getOnlyElement(matchedMembers), metadata);
			default:
				throw multipleWatchers(metadata);
		}
	}

	private static Watcher<?> getWatcherFromMember(Member member, PropertyMetadata metadata) throws ReflectiveOperationException {
		if (member instanceof Field) {
			return (Watcher<?>) ((Field) member).get(null);
		} else {
			java.lang.reflect.Method method = (java.lang.reflect.Method) member;
			if (method.getParameterTypes().length == 0) {
				return (Watcher<?>) method.invoke(null, ArrayUtils.EMPTY_OBJECT_ARRAY);
			} else {
				return (Watcher<?>) method.invoke(null, metadata);
			}
		}
	}

	private static RuntimeException multipleWatchers(PropertyMetadata metadata) {
		return new IllegalStateException("Multiple possible Watchers for " + metadata);
	}

	private static void checkStatic(Member member) {
		checkState(Modifier.isStatic(member.getModifiers()), "@WatcherFactory on non-static member " + member.getName() + " in " + member.getDeclaringClass());
	}

	private static void checkIsWatcher(Class<?> type, Member member) {
		checkState(Watcher.class.isAssignableFrom(type), "@WatcherFactory on member with mismatched type (" + member.getName() + " in " + member.getDeclaringClass() + ")");
	}

}
