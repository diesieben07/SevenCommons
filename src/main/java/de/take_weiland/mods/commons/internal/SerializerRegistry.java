package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.reflect.TypeToken;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.asm.ModAnnotation;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.serialize.*;
import de.take_weiland.mods.commons.sync.Watcher;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class SerializerRegistry {

	private static SerializerRegistry nbtRegistry;
	private static SerializerRegistry streamRegistry;
	private static SerializerRegistry watcherRegistry;

	@SuppressWarnings("unchecked")
	public static <T> NBTSerializer<T> getNBTSerializer(TypeSpecification<T> type) {
		checkMethod(type, SerializationMethod.VALUE);
		// we know this cast must succeed
		return (NBTSerializer<T>) nbtRegistry.findSerializer(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> NBTSerializer.Contents<T> getNBTSerializerContent(TypeSpecification<T> type) {
		checkMethod(type, SerializationMethod.CONTENTS);
		return (NBTSerializer.Contents<T>) nbtRegistry.findSerializer(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> ByteStreamSerializer<T> getStreamSerializer(TypeSpecification<T> type) {
		checkMethod(type, SerializationMethod.VALUE);
		return (ByteStreamSerializer<T>) streamRegistry.findSerializer(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> ByteStreamSerializer.Contents<T> getStreamSerializerContents(TypeSpecification<T> type) {
		checkMethod(type, SerializationMethod.CONTENTS);
		return (ByteStreamSerializer.Contents<T>) streamRegistry.findSerializer(type);
	}

	@SuppressWarnings("unchecked")
	public static <T> Watcher<T> getWatcher(TypeSpecification<T> type) {
		return (Watcher<T>) watcherRegistry.findSerializer(type);
	}

	private static void checkMethod(TypeSpecification<?> type, SerializationMethod expected) {
		checkArgument(type.getDesiredMethod() == expected, "Invalid type for SerializationMethod." + expected.name());
	}

	public static void init(ASMDataTable data) {
		try {
			nbtRegistry = new SerializerRegistry(
					NBTSerializer.class,
					NBTSerializer.Contents.class,
					NBTSerializer.Provider.class,
					data
			);

			streamRegistry = new SerializerRegistry(
					ByteStreamSerializer.class,
					ByteStreamSerializer.Contents.class,
					ByteStreamSerializer.Provider.class,
					data
			);

			watcherRegistry = new SerializerRegistry(
					Watcher.class,
					Watcher.class,
					Watcher.Provider.class,
					data
			);
		} catch (ReflectiveOperationException e) {
			// this should never happen, if it does, just propagate
			throw Throwables.propagate(e);
		}
	}

	private static final String ANNOTATION_TYPE_KEY = "forType";
	private static final String ANNOTATION_METHOD_KEY = "method";

	private final Class<?> valueSerializerIface;
	private final Class<?> contentSerializerIface;

	private final ListMultimap<Class<?>, MethodHandle> registry;

	private SerializerRegistry(Class<?> valueSerializerIface, Class<?> contentSerializerIface, Class<? extends Annotation> annotation, ASMDataTable data) throws ReflectiveOperationException {
		this.valueSerializerIface = valueSerializerIface;
		this.contentSerializerIface = contentSerializerIface;

		ImmutableListMultimap.Builder<Class<?>, MethodHandle> builder = ImmutableListMultimap.builder();

		MethodHandles.Lookup lookup = MethodHandles.lookup();

		Set<ASMDataTable.ASMData> all = data.getAll(annotation.getName());

		Set<String> visited = new HashSet<>();
		for (ASMDataTable.ASMData info : all) {
			String uniqueID = info.getClassName() + "." + info.getObjectName();
			if (!visited.add(uniqueID)) {
				// skip already visited, because due to two mods in the env
				// we get everything twice
				continue;
			}
			MethodHandle handle = asMethodHandle(lookup, info);
			Class<?> targetClass = findTargetType(info);
			builder.put(targetClass, handle);
		}

		registry = builder.build();
	}

	private Object findSerializer(TypeSpecification<?> type) {
		SerializationMethod method = type.getDesiredMethod();
		ChangeableMethodSpec<?> overriddenSpec = null;

		for (Class<?> clazz : JavaUtils.hierarchy(type.getRawType(), JavaUtils.Interfaces.INCLUDE)) {
			Collection<MethodHandle> providers = registry.get(clazz);
			Object serializer;

			if (method != SerializationMethod.DEFAULT) {
				serializer = findSerializerFromProviders(type, providers);
			} else {
				if (overriddenSpec == null) {
					overriddenSpec = new ChangeableMethodSpec<>(type);
				}
				overriddenSpec.overrideMethod = SerializationMethod.VALUE;
				serializer = findSerializerFromProviders(overriddenSpec, providers);
				if (serializer != null) {
					overriddenSpec.overrideMethod = SerializationMethod.CONTENTS;
					serializer = findSerializerFromProviders(overriddenSpec, providers);
				}
			}

			if (serializer != null) {
				return serializer;
			}
		}
		throw new IllegalArgumentException("No serializer found for " + type);
	}

	private Object findSerializerFromProviders(TypeSpecification<?> type, Collection<MethodHandle> providers) {
		Object serializer = null;
		for (MethodHandle provider : providers) {
			Object applied = apply(provider, type);

			if (applied == null) {
				continue;
			}

			if (serializer == null) {
				serializer = applied;
			} else {
				throw new IllegalStateException("Multiple Serializers for " + type +
						": " + JavaUtils.defaultToString(serializer) + " and " + JavaUtils.defaultToString(applied));
			}
		}
		return serializer;
	}

	private Object apply(MethodHandle mh, TypeSpecification<?> spec) {
		Object retVal;
		try {
			retVal = (Object) mh.invokeExact((TypeSpecification<?>) spec);
		} catch (Throwable t) {
			throw JavaUtils.throwUnchecked(t);
		}
		if (retVal != null) {
			switch (spec.getDesiredMethod()) {
				case CONTENTS:
					checkReturn(contentSerializerIface, retVal, mh);
					break;
				case VALUE:
					checkReturn(valueSerializerIface, retVal, mh);
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		return retVal;
	}

	private static Class<?> findTargetType(ASMDataTable.ASMData annotationData) throws ClassNotFoundException {
		String className;
		Type type = (Type) annotationData.getAnnotationInfo().get(ANNOTATION_TYPE_KEY);
		if (type == null) {
			className = annotationData.getClassName();
		} else {
			checkState(!ASMUtils.isPrimitive(type), "Cannot make serializer for primitive type");
			className = type.getClassName();
		}
		return Class.forName(className, true, Launch.classLoader);
	}

	private MethodHandle asMethodHandle(MethodHandles.Lookup lookup, ASMDataTable.ASMData annotationData) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
		MethodHandle mh;

		SerializationMethod filter = SerializationMethod.DEFAULT;
		ModAnnotation.EnumHolder methodVal = (ModAnnotation.EnumHolder) annotationData.getAnnotationInfo().get(ANNOTATION_METHOD_KEY);
		if (methodVal != null) {
			filter = Enum.valueOf(SerializationMethod.class, getEnumValue(methodVal));
		}

		Class<?> containingClass = Class.forName(annotationData.getClassName(), true, Launch.classLoader);
		String objectName = annotationData.getObjectName();
		int parenIdx = objectName.indexOf('(');
		if (parenIdx >= 0) {
			// method
			String methodName = objectName.substring(0, parenIdx);
			String methodSig = objectName.substring(parenIdx);
			Type ret = Type.getReturnType(methodSig);
			Type[] args = Type.getArgumentTypes(methodSig);

			verifyMember(!ASMUtils.isPrimitive(ret), annotationData, "Primitive return type");
			verifyMember(ret.getSort() != Type.ARRAY, annotationData, "Array return type");
			verifyMember(args.length == 1, annotationData, "More than one argument");
			verifyMember(args[0].getInternalName().equals(Type.getInternalName(TypeSpecification.class)), annotationData, "Parameter must be TypeSpecification");

			Method method = containingClass.getDeclaredMethod(methodName, TypeSpecification.class);
			method.setAccessible(true);
			verifyMember(Modifier.isStatic(method.getModifiers()), annotationData, "Must be static");
			mh = lookup.unreflect(method);
		} else {
			Field field = containingClass.getDeclaredField(objectName);
			field.setAccessible(true);
			verifyMember(!field.getType().isPrimitive(), annotationData, "Primitive type");
			verifyMember(!field.getType().isArray(), annotationData, "Array type");
			verifyMember(Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()), annotationData, "Field must be static final");
			verifyMember(filter != SerializationMethod.DEFAULT, annotationData, "Field needs method attribute");

			MethodHandle getter = lookup.unreflectGetter(field);
			mh = MethodHandles.dropArguments(getter, 0, TypeSpecification.class);
		}

		if (filter != SerializationMethod.DEFAULT) {
			mh = mh.asType(methodType(Object.class, TypeSpecification.class));
			mh = MethodHandles.insertArguments(mhFilterSerializationMethod, 0, filter, mh);
		}

		return mh.asType(methodType(Object.class, TypeSpecification.class));
	}

	private static final Field enumHolderVal;

	static {
		try {
			enumHolderVal = ModAnnotation.EnumHolder.class.getDeclaredField("value");
			enumHolderVal.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw Throwables.propagate(e);
		}
	}

	private static String getEnumValue(ModAnnotation.EnumHolder holder) {
		try {
			return (String) enumHolderVal.get(holder);
		} catch (IllegalAccessException e) {
			throw new AssertionError();
		}
	}

	private static final MethodHandle mhFilterSerializationMethod;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			mhFilterSerializationMethod = lookup.findStatic(SerializerRegistry.class,
					"filterSerializationMethod",
					methodType(Object.class, SerializationMethod.class, MethodHandle.class, TypeSpecification.class));

		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	private static Object filterSerializationMethod(SerializationMethod filter, MethodHandle target, TypeSpecification<?> type) throws Throwable {
		if (type.getDesiredMethod() == filter) {
			return (Object) target.invokeExact((TypeSpecification<?>) type);
		} else {
			return null;
		}
	}

	private static void verifyMember(boolean condition, ASMDataTable.ASMData data, String fail) {
		checkState(condition, data.getObjectName() + " in " + data.getClassName() + " is invalid: " + fail);
	}

	private static void checkReturn(Class<?> iface, Object val, MethodHandle handle) {
		checkState(iface.isInstance(val), "Invalid return value from " + handle + " expected an instance of " + iface + " or null");
	}

	private static final class ChangeableMethodSpec<T> implements TypeSpecification<T> {

		private final TypeSpecification<T> wrapped;

		SerializationMethod overrideMethod;

		ChangeableMethodSpec(TypeSpecification<T> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public SerializationMethod getDesiredMethod() {
			return overrideMethod;
		}

		@Override
		public TypeToken<T> getType() {
			return wrapped.getType();
		}

		@Override
		public Class<? super T> getRawType() {
			return wrapped.getRawType();
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return wrapped.hasAnnotation(annotation);
		}

		@Override
		public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
			return wrapped.getAnnotation(annotationClass);
		}
	}

}
