package de.take_weiland.mods.commons.asm;

import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>A collection of utility methods for working with the ASM library.</p>
 */
@ParametersAreNonnullByDefault
public final class ASMUtils {

	private ASMUtils() { }

	/**
	 * <p>Convert from one type to another.</p>
	 * <p>The following conversions are allowed:</p>
	 * <ul>
	 *     <p>primitive -> primitive</p>
	 *     <p>primitive -> wrapper (only direct conversion from a primitive to it's wrapper)</p>
	 *     <p>wrapper -> primitive (again only direct conversion)</p>
	 *     <p>class -> class (may be a no-op or a checkcast)</p>
	 * </ul>
	 * @param gen the generator adapter
	 * @param from the type on top of the stack
	 * @param to the type to convert to
	 */
	public static void convertTypes(GeneratorAdapter gen, Type from, Type to) {
		if (isPrimitive(from) && isPrimitive(to)) {
			gen.cast(from, to);
		} else {
			if (isPrimitive(from)) {
				Type boxed = boxedType(from);
				box(gen, boxed);
				if (!isAssignableFrom(to, boxed)) {
					gen.checkCast(to);
				}
			} else if (isPrimitive(to)) {
				Type boxed = boxedType(to);
				if (!isAssignableFrom(to, from)) {
					gen.checkCast(boxed);
				}
				unbox(gen, to);
			} else {
				if (!isAssignableFrom(to, from)) {
					gen.checkCast(to);
				}
			}
		}
	}

	private static boolean isAssignableFrom(Type a, Type b) {
		return ClassInfo.of(a).isAssignableFrom(ClassInfo.of(b));
	}

	private static void box(GeneratorAdapter gen, Type boxed) {
		Type primT = unboxedType(boxed);
		org.objectweb.asm.commons.Method boxMethod =
				new org.objectweb.asm.commons.Method("valueOf", boxed, new Type[] { primT });

		gen.invokeStatic(boxed, boxMethod);
	}

	private static void unbox(GeneratorAdapter gen, Type primitive) {
		Type boxedT = boxedType(primitive);

		org.objectweb.asm.commons.Method unboxMethod =
				new org.objectweb.asm.commons.Method(primitive.getClassName() + "Value", primitive, new Type[0]);
		gen.invokeVirtual(boxedT, unboxMethod);
	}

	// *** name utilities *** //

	/**
	 * <p>Convert the given binary name (e.g. {@code java.lang.Object$Subclass}) to an internal name (e.g. {@code java/lang/Object$Subclass}).</p>
	 *
	 * @param binaryName the binary name
	 * @return the internal name
	 */
	public static String internalName(String binaryName) {
		return binaryName.replace('.', '/');
	}

	/**
	 * <p>Convert the given internal name to a binary name (opposite of {@link #internalName(String)}).</p>
	 *
	 * @param internalName the internal name
	 * @return the binary name
	 */
	public static String binaryName(String internalName) {
		return internalName.replace('/', '.');
	}

	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;

	/**
	 * <p>Get the active {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any.</p>
	 *
	 * @return the active transformer, or null if none
	 */
	@Nullable
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			nameTransformer = FluentIterable.from(Launch.classLoader.getTransformers())
					.filter(IClassNameTransformer.class)
					.first().orNull();
			nameTransChecked = true;
		}
		return nameTransformer;
	}

	/**
	 * <p>Transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any. Returns the untransformed
	 * name if no transformer is present.</p>
	 *
	 * @param untransformedName the un-transformed internal name of the class
	 * @return the transformed internal name of the class
	 */
	public static String transformName(String untransformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? untransformedName : t.remapClassName(binaryName(untransformedName)));
	}

	/**
	 * <p>Un-transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any. Returns
	 * the transformed name if no transformer is present.</p>
	 *
	 * @param transformedName the transformed internal name of the class
	 * @return the un-transformed internal name of the class
	 */
	public static String untransformName(String transformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? transformedName : t.unmapClassName(binaryName(transformedName)));
	}

	// *** Misc Utils *** //

	/**
	 * <p>Create a ClassNode for the class specified by the given internal name. The class must be accessible
	 * by the {@link net.minecraft.launchwrapper.Launch#classLoader LaunchClassLoader}.</p>
	 * @param name the internal name
	 * @return a ClassNode
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassNode getClassNode(String name) {
		return getClassNode(name, 0);
	}

	/**
	 * <p>Create a ClassNode for the class specified by the given internal name. The class must be accessible
	 * by the {@link net.minecraft.launchwrapper.Launch#classLoader LaunchClassLoader}.</p>
	 *
	 * @param name the internal name
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassNode getClassNode(String name, int readerFlags) {
		try {
			byte[] bytes = Launch.classLoader.getClassBytes(untransformName(name));
			if (bytes == null) {
				throw new MissingClassException(name);
			}
			return getClassNode(bytes, readerFlags);
		} catch (Exception e) {
			Throwables.propagateIfInstanceOf(e, MissingClassException.class);
			throw new MissingClassException(name, e);
		}
	}

	/**
	 * <p>Create a ClassNode for the class represented by the given bytes.</p>
	 * @param bytes the class bytes
	 * @return a ClassNode
	 */
	public static ClassNode getClassNode(byte[] bytes) {
		return getClassNode(bytes, 0);
	}

	/**
	 * <p>Create a ClassNode for the class represented by the given bytes.</p>
	 *
	 * @param bytes the class bytes
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 */
	public static ClassNode getClassNode(byte[] bytes, int readerFlags) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, readerFlags);
		return clazz;
	}

	private static final int THIN_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

	/**
	 * <p>Create a ClassNode for the class specified by the given internal name. The class must be accessible
	 * by the {@link net.minecraft.launchwrapper.Launch#classLoader LaunchClassLoader}.</p>
	 * <p>The ClassNode will only contain the rough outline of the class. It is read with {@code ClassReader.SKIP_CODE}, {@code ClassReader.SKIP_DEBUG}
	 * {@code ClassReader.SKIP_FRAMES} set.</p>
	 * @param name the internal name
	 * @return a ClassNode
	 *
	 */
	public static ClassNode getThinClassNode(String name) {
		return getClassNode(name, THIN_FLAGS);
	}

	/**
	 * <p>Create a ClassNode for the class represented by the given bytes.</p>
	 * <p>The ClassNode will only contain the rough outline of the class. It is read with {@code ClassReader.SKIP_CODE}, {@code ClassReader.SKIP_DEBUG}
	 * {@code ClassReader.SKIP_FRAMES} set.</p>
	 * @param bytes the class bytes
	 * @return a ClassNode
	 *
	 */
	public static ClassNode getThinClassNode(byte[] bytes) {
		return getClassNode(bytes, THIN_FLAGS);
	}

	// *** annotation utilities *** //

	/**
	 * <p>Checks if the given {@link org.objectweb.asm.Type} represents a primitive type or the void type.</p>
	 *
	 * @param type the type
	 * @return true if the {@code Type} represents a primitive type or void
	 */
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
	}

	/**
	 * <p>Checks if the given {@code Type} represents a primitive wrapper such as {@code Integer} or the {@code Void} type.</p>
	 * @param type
	 * @return
	 */
	public static boolean isPrimitiveWrapper(Type type) {
		return unboxedType(type) != type;
	}

	public static Type unboxedType(Type wrapper) {
		switch (wrapper.getInternalName()) {
			case "java/lang/Void":
				return Type.VOID_TYPE;
			case "java/lang/Boolean":
				return Type.BOOLEAN_TYPE;
			case "java/lang/Byte":
				return Type.BYTE_TYPE;
			case "java/lang/Short":
				return Type.SHORT_TYPE;
			case "java/lang/Character":
				return Type.CHAR_TYPE;
			case "java/lang/Integer":
				return Type.INT_TYPE;
			case "java/lang/Long":
				return Type.LONG_TYPE;
			case "java/lang/Float":
				return Type.FLOAT_TYPE;
			case "java/lang/Double":
				return Type.DOUBLE_TYPE;
			default:
				return wrapper;
		}
	}

	public static Type boxedType(Type primitive) {
		switch (primitive.getSort()) {
			case Type.VOID:
				return Type.getType(Void.class);
			case Type.BOOLEAN:
				return Type.getType(Boolean.class);
			case Type.BYTE:
				return Type.getType(Byte.class);
			case Type.SHORT:
				return Type.getType(Short.class);
			case Type.CHAR:
				return Type.getType(Character.class);
			case Type.INT:
				return Type.getType(Integer.class);
			case Type.LONG:
				return Type.getType(Long.class);
			case Type.FLOAT:
				return Type.getType(Float.class);
			case Type.DOUBLE:
				return Type.getType(Double.class);
			default:
				return primitive;
		}
	}

}
