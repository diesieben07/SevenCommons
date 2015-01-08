package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import cpw.mods.fml.common.discovery.ASMDataTable;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.serialize.ByteStreamSerializer;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
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
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class SerializerRegistry {

	private static final Map<Class<?>, SerializerRegistry> registries;

	static {
		registries = ImmutableMap.<Class<?>,SerializerRegistry>of(
				ByteStreamSerializer.SPI.class, new ByteStreamSerializerRegistry(),
				NBTSerializer.SPI.class, new NBTSerializerRegistry()
		);
	}

	private final Class<?> valueSerializerIface;
	private final Class<?> contentSerializerIface;
	private final Class<? extends Annotation> annotation;

	private final ListMultimap<Class<?>, MethodHandle> registry;

	protected SerializerRegistry(Class<?> valueSerializerIface, Class<?> contentSerializerIface, Class<? extends Annotation> annotation, ASMDataTable data) throws ClassNotFoundException, NoSuchFieldException {
		this.valueSerializerIface = valueSerializerIface;
		this.contentSerializerIface = contentSerializerIface;
		this.annotation = annotation;

		ImmutableListMultimap.Builder<Class<?>, MethodHandle> builder = ImmutableListMultimap.builder();

		MethodHandles.Lookup lookup = MethodHandles.lookup();

		Set<ASMDataTable.ASMData> all = data.getAll(annotation.getName());
		for (ASMDataTable.ASMData info : all) {
			MethodHandle handle = asMethodHandle(MethodHandles.lookup(), info);
			builder.put()
		}

		registry = builder.build();
	}

	private static MethodHandle asMethodHandle(MethodHandles.Lookup lookup, ASMDataTable.ASMData annotationData) {
		MethodHandle mh;

		try {
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

                mh = lookup.unreflectGetter(field);
            }
		} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
			throw JavaUtils.throwUnchecked(e);
		}

		return mh.asType(methodType(Object.class, TypeSpecification.class));
	}

	private static void verifyMember(boolean condition, ASMDataTable.ASMData data, String fail) {
		checkState(condition, data.getObjectName() + " in " + data.getClassName() + " is invalid: " + fail);
	}

	private Object findSerializer(TypeSpecification<?> type) {
		for (Class<?> clazz : JavaUtils.hierarchy(type.getRawType(), JavaUtils.Interfaces.INCLUDE)) {
			Collection<MethodHandle> providers = registry.get(clazz);
			Object serializer = findSerializerFromProviders(type, providers);
			if (serializer != null) {
				return serializer;
			}
		}
		throw new IllegalArgumentException("No serializer found for " + type);
	}

	private Object findSerializerFromProviders(TypeSpecification<?> type, Collection<MethodHandle> providers) {
		SerializationMethod serializationMethod = type.getDesiredMethod();

		Object serializer = null;
		for (MethodHandle provider : providers) {
			Object applied;
			try {
				applied = (Object) provider.invokeExact((TypeSpecification<?>) type);
			} catch (Throwable t) {
				throw JavaUtils.throwUnchecked(t);
			}

			if (applied == null) {
				continue;
			} else {
				switch (serializationMethod) {
					case VALUE:
						checkReturn(valueSerializerIface, applied, provider);
						break;
					case CONTENTS:
						checkReturn(contentSerializerIface, applied, provider);
						break;
				}

			}

			if (serializer == null) {
				serializer = applied;
			} else {
				throw new IllegalStateException("Multiple Serializers for type " + type);
			}
		}
		return serializer;
	}

	private static void checkReturn(Class<?> iface, Object val, MethodHandle handle) {
		checkState(iface.isInstance(val), "Invalid return value from " + handle + " expected an instance of " + iface + " or null");
	}

}
