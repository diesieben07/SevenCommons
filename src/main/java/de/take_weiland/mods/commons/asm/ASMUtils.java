package de.take_weiland.mods.commons.asm;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.launchwrapper.IClassNameTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import static de.take_weiland.mods.commons.internal.SevenCommons.CLASSLOADER;
import static org.objectweb.asm.Opcodes.*;

public final class ASMUtils {

	private ASMUtils() { }

	private static final Predicate<MethodNode> isConstructor = new Predicate<MethodNode>() {
		@Override
		public boolean apply(MethodNode method) {
			return method.name.equals("<init>");
		}
	};

	// *** bytecode analyzing helpers *** //

	/**
	 * finds the last return instruction in the given method.
	 * @param method the method
	 * @return the last return instruction
	 * @throws java.lang.IllegalArgumentException if the method doesn't have valid return opcode (should never happen with any valid method)
	 */
	public static AbstractInsnNode findLastReturn(MethodNode method) {
		int searchFor = Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN);
		AbstractInsnNode node = method.instructions.getLast();
		do {
			if (node.getOpcode() == searchFor) {
				return node;
			}
			node = node.getPrevious();
		} while (node != null);
		throw new IllegalArgumentException("Illegal method: Has no or wrong return opcode!");
	}

	// *** method finding helpers *** //

	/**
	 * find the method with the given name. If multiple methods with the same parameters exist, the first one will be returned
	 * @param clazz the class
	 * @param name the method name to search for
	 * @return the first method with the given name or null if no such method is found
	 */
	public static MethodNode findMethod(ClassNode clazz, String name) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * find the method with the given name and method descriptor.
	 * @param clazz the class
	 * @param name the method name to search for
	 * @param desc the method descriptor to search for
	 * @return the method with the given name and descriptor or null if no such method is found
	 * @see org.objectweb.asm.Type#getMethodDescriptor
	 */
	public static MethodNode findMethod(ClassNode clazz, String name, String desc) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * Find the method with the given name. It is automatically chosen between MCP and SRG name, depending on if this code is running in a development environment.
	 * @param clazz the class
	 * @param mcpName the MCP name of the method (e.g. {@code updateEntity})
	 * @param srgName the SRG name of the method (e.g. {@code func_70316_g}
	 * @return the first method with the given name or null if no such method is found
	 */
	public static MethodNode findMinecraftMethod(ClassNode clazz, String mcpName, String srgName) {
		return findMethod(clazz, useMcpNames() ? mcpName : srgName);
	}

	/**
	 * find the method with the given name and descriptor. It is automatically chosen between MCP and SRG name, depending on if this code is running in a development environment.
	 * @param clazz the class
	 * @param mcpName the MCP name of the method (e.g. {@code updateEntity})
	 * @param srgName the SRG name of the method (e.g. {@code func_70316_g}
	 * @param desc the method descriptor of the method
	 * @return the method or null if no such method is found
	 * @see org.objectweb.asm.Type#getMethodDescriptor
	 */
	public static MethodNode findMinecraftMethod(ClassNode clazz, String mcpName, String srgName, String desc) {
		return findMethod(clazz, useMcpNames() ? mcpName : srgName, desc);
	}

	/**
	 * like {@link #findMethod(org.objectweb.asm.tree.ClassNode, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMethod(ClassNode clazz, String name) {
		MethodNode m = findMethod(clazz, name);
		if (m == null) {
			throw MissingMethodException.create(name, clazz.name);
		}
		return m;
	}

	/**
	 * like {@link #findMethod(org.objectweb.asm.tree.ClassNode, String, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMethod(ClassNode clazz, String name, String desc) {
		MethodNode m = findMethod(clazz, name, desc);
		if (m == null) {
			throw MissingMethodException.withDesc(name, desc, clazz.name);
		}
		return m;
	}

	/**
	 * like {@link #findMinecraftMethod(org.objectweb.asm.tree.ClassNode, String, String, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMinecraftMethod(ClassNode clazz, String mcpName, String srgName, String desc) {
		MethodNode m = findMinecraftMethod(clazz, mcpName, srgName, desc);
		if (m == null) {
			throw MissingMethodException.withDesc(mcpName, srgName, desc, clazz.name);
		}
		return m;
	}

	/**
	 * like {@link #findMinecraftMethod(org.objectweb.asm.tree.ClassNode, String, String)}, but throws if method not found
	 * @throws MissingMethodException if method doesn't exist
	 */
	public static MethodNode requireMinecraftMethod(ClassNode clazz, String mcpName, String srgName) {
		MethodNode m = findMinecraftMethod(clazz, mcpName, srgName);
		if (m == null) {
			throw MissingMethodException.create(mcpName, srgName, clazz.name);
		}
		return m;
	}

	/**
	 * <p>get all constructors of the given ClassNode</p>
	 * <p>The returned collection is a live-view, so if new constructors get added, they will be present in the returned collection immediately</p>
	 * @param clazz the class
	 * @return all constructors
	 */
	public static Collection<MethodNode> getConstructors(ClassNode clazz) {
		return Collections2.filter(clazz.methods, isConstructor);
	}

	@Deprecated
	public static List<MethodNode> findRootConstructors(ClassNode clazz) {
		return getRootConstructors(clazz);
	}

	/**
	 * <p>Get all constructors, which don't call another constructor of the same class.</p>
	 * <p>Useful if you need to add code that is called, whenever a new instance of the class is created, no matter through which constructor.</p>
	 * @param clazz the class
	 * @return all root constructors
	 */
	public static List<MethodNode> getRootConstructors(ClassNode clazz) {
		List<MethodNode> roots = Lists.newArrayList();

		cstrs:
		for (MethodNode method : getConstructors(clazz)) {
			AbstractInsnNode insn = method.instructions.getFirst();
			do {
				if (insn.getOpcode() == INVOKESPECIAL && ((MethodInsnNode) insn).owner.equals(clazz.name)) {
					continue cstrs;
				}
				insn = insn.getNext();
			} while (insn != null);
			roots.add(method);
		}
		return roots;
	}

	/**
	 * Determine whether this is an obfuscated environment or not. True if MCP names should be used (development environment)
	 * @return true if this is a development environment and MCP names should be used.
	 */
	public static boolean useMcpNames() {
		return SevenCommons.MCP_ENVIRONMENT;
	}

	// *** Class name Utilities *** //

	/**
	 * convert the given binary name (e.g. {@code java.lang.Object$Subclass}) to an internal name (e.g. {@code java/lang/Object$Subclass})
	 * @param binaryName the binary name
	 * @return the internal name
	 */
	public static String internalName(String binaryName) {
		return binaryName.replace('.', '/');
	}

	/**
	 * convert the given internal name to a binary name (opposite of {@link #internalName(String)}
	 * @param internalName the internal name
	 * @return the binary name
	 */
	public static String binaryName(String internalName) {
		return internalName.replace('.', '/');
	}

	@Deprecated
	public static String makeNameInternal(String name) {
		return internalName(name);
	}

	@Deprecated
	public static String undoInternalName(String name) {
		return binaryName(name);
	}

	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;

	/**
	 * get the active {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 * @return the active transformer, or null if none
	 */
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			nameTransformer = Iterables.getOnlyElement(Iterables.filter(CLASSLOADER.getTransformers(), IClassNameTransformer.class), null);
			nameTransChecked = true;
		}
		return nameTransformer;
	}

	/**
	 * transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 * @param untransformedName the un-transformed name of the class
	 * @return the transformed name of the class
	 */
	public static String transformName(String untransformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? untransformedName : t.remapClassName(binaryName(untransformedName)));
	}

	/**
	 * un-transform the class name with the current {@link net.minecraft.launchwrapper.IClassNameTransformer}, if any
	 * @param transformedName the transformed name of the class
	 * @return the un-transformed name of the class
	 */
	public static String untransformName(String transformedName) {
		IClassNameTransformer t = getClassNameTransformer();
		return internalName(t == null ? transformedName : t.unmapClassName(binaryName(transformedName)));
	}

	@Deprecated
	public static String obfuscateClass(String deobfName) {
		return untransformName(deobfName);
	}

	@Deprecated
	public static String deobfuscateClass(String obfName) {
		return transformName(obfName);
	}

	// *** Misc Utils *** //

	/**
	 * equivalent to {@link #getClassNode(String, int)} with no ClassReader flags
	 */
	public static ClassNode getClassNode(String name) {
		return getClassNode(name, 0);
	}

	/**
	 * gets a {@link org.objectweb.asm.tree.ClassNode} for the given class name
	 * @param name the class to load
	 * @param readerFlags the flags to pass to the {@link org.objectweb.asm.ClassReader}
	 * @return a ClassNode
	 * @throws MissingClassException if the class couldn't be found or can't be loaded as raw-bytes
	 */
	public static ClassNode getClassNode(String name, int readerFlags) {
		try {
			byte[] bytes = CLASSLOADER.getClassBytes(transformName(name));
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
	 * equivalent to {@link #getClassNode(byte[], int)} with no ClassReader flags
	 */
	public static ClassNode getClassNode(byte[] bytes) {
		return getClassNode(bytes, 0);
	}

	/**
	 * gets a {@link org.objectweb.asm.tree.ClassNode} representing the class described by the given bytes
	 * @param bytes the raw bytes describing the class
	 * @param readerFlags the the flags to pass to the {@link org.objectweb.asm.ClassReader}
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
	 * equivalent to {@link #getClassNode(String, int)} with all skip flags set ({@code ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES})
	 */
	public static ClassNode getThinClassNode(String name) {
		return getClassNode(name, THIN_FLAGS);
	}

	/**
	 * equivalent to {@link #getClassNode(byte[], int)} with all skip flags set ({@code ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES})
	 */
	public static ClassNode getThinClassNode(byte[] bytes) {
		return getClassNode(bytes, THIN_FLAGS);
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given field
	 * @param field the field
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(FieldNode field, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(field.visibleAnnotations, field.invisibleAnnotations), ann);
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given class
	 * @param clazz the class
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(ClassNode clazz, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(clazz.visibleAnnotations, clazz.invisibleAnnotations), ann);
	}

	/**
	 * gets the {@link org.objectweb.asm.tree.AnnotationNode} for the given Annotation class, if present on the given method
	 * @param method the method
	 * @param ann the annotation class to get
	 * @return the AnnotationNode or null if the annotation is not present
	 */
	public static AnnotationNode getAnnotation(MethodNode method, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(method.visibleAnnotations, method.invisibleAnnotations), ann);
	}

	private static AnnotationNode getAnnotation(Iterable<AnnotationNode> annotations, Class<? extends Annotation> ann) {
		String desc = Type.getDescriptor(ann);
		for (AnnotationNode node : annotations) {
			if (node.desc.equals(desc)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * check if the given Annotation class is present on this field
	 * @param field the field
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return getAnnotation(field, annotation) != null;
	}

	/**
	 * check if the given Annotation class is present on this class
	 * @param clazz the class
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		return getAnnotation(clazz, annotation) != null;
	}

	/**
	 * check if the given Annotation class is present on this method
	 * @param method the method
	 * @param annotation the annotation
	 * @return true if the annotation is present
	 */
	public static boolean hasAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
		return getAnnotation(method, annotation) != null;
	}

	/**
	 * Checks if the given {@link org.objectweb.asm.Type} represents a primitive or void
	 * @param type the type
	 * @return true if the {@code Type} represents a primitive type or void
	 */
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
	}

	/**
	 * create a {@link ClassInfo} representing the given Class
	 * @param clazz the Class
	 * @return a ClassInfo
	 */
	public static ClassInfo getClassInfo(Class<?> clazz) {
		return new ClassInfoFromClazz(clazz);
	}

	/**
	 * create a {@link ClassInfo} representing the given ClassNode
	 * @param clazz the ClassNode
	 * @return a ClassInfo
	 */
	public static ClassInfo getClassInfo(ClassNode clazz) {
		return new ClassInfoFromNode(clazz);
	}

	/**
	 * <p>create a {@link ClassInfo} representing the given class.</p>
	 * <p>This method will not load any classes through the ClassLoader directly, but instead use the ASM library to analyze the raw class bytes.</p>
	 * @param className the class
	 * @return a ClassInfo
	 * @throws MissingClassException if the class could not be found
	 */
	public static ClassInfo getClassInfo(String className) {
		className = binaryName(className);
		Class<?> clazz;
		if ((clazz = SevenCommons.REFLECTOR.findLoadedClass(CLASSLOADER, className)) != null) {
			return new ClassInfoFromClazz(clazz);
		} else {
			return new ClassInfoFromNode(getThinClassNode(className));
		}
	}

	/**
	 * @deprecated use {@link de.take_weiland.mods.commons.asm.ClassInfo#isAssignableFrom(ClassInfo)}
	 */
	@Deprecated
	public static boolean isAssignableFrom(ClassInfo parent, ClassInfo child) {
		return parent.isAssignableFrom(child);
	}

}
