package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
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

	public static Collection<MethodNode> getConstructors(ClassNode clazz) {
		return Collections2.filter(clazz.methods, isConstructor);
	}

	public static List<MethodNode> findRootConstructors(ClassNode clazz) {
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

	public static MethodNode findMethod(ClassNode clazz, String name) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name)) {
				return method;
			}
		}
		return null;
	}

	public static MethodNode findMethod(ClassNode clazz, String name, String desc) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return method;
			}
		}
		return null;
	}

	public static MethodNode findMinecraftMethod(ClassNode clazz, String mcpName, String srgName) {
		return findMethod(clazz, useMcpNames() ? mcpName : srgName);
	}

	public static MethodNode findMinecraftMethod(ClassNode clazz, String mcpName, String srgName, String desc) {
		return findMethod(clazz, useMcpNames() ? mcpName : srgName, desc);
	}

	public static boolean useMcpNames() {
		return SevenCommons.MCP_ENVIRONMENT;
	}
	
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
	
	public static String makeNameInternal(String name) {
		return name.replace('.', '/');
	}
	
	public static String undoInternalName(String name) {
		return name.replace('/', '.');
	}
	
	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;
	
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			Iterable<IClassNameTransformer> nameTransformers = Iterables.filter(SevenCommons.CLASSLOADER.getTransformers(), IClassNameTransformer.class);
			nameTransformer = Iterables.getOnlyElement(nameTransformers, null);
			nameTransChecked = true;
		}
		return nameTransformer;
	}
	
	public static String deobfuscateClass(String obfName) {
		IClassNameTransformer t = getClassNameTransformer();
		return ASMUtils.makeNameInternal(t == null ? obfName : t.remapClassName(obfName));
	}
	
	public static String obfuscateClass(String deobfName) {
		IClassNameTransformer t = getClassNameTransformer();
		return ASMUtils.makeNameInternal(t == null ? deobfName : t.unmapClassName(deobfName));
	}

	public static ClassNode getClassNode(String name) {
		return getClassNode(name, 0);
	}

	private static final int THIN_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

	public static ClassNode getThinClassNode(String name) {
		return getClassNode(name, THIN_FLAGS);
	}

	public static ClassNode getThinClassNode(byte[] bytes) {
		return getClassNode(bytes, THIN_FLAGS);
	}

	public static ClassNode getClassNode(String name, int readerFlags) {
		try {
			return getClassNode(SevenCommons.CLASSLOADER.getClassBytes(obfuscateClass(name)), readerFlags);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ClassNode getClassNode(byte[] bytes) {
		return getClassNode(bytes, 0);
	}

	public static ClassNode getClassNode(byte[] bytes, int readerFlags) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, readerFlags);
		return clazz;
	}

	public static AnnotationNode getAnnotation(FieldNode field, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(field.visibleAnnotations, field.invisibleAnnotations), ann);
	}
	
	public static AnnotationNode getAnnotation(ClassNode clazz, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(clazz.visibleAnnotations, clazz.invisibleAnnotations), ann);
	}
	
	public static AnnotationNode getAnnotation(MethodNode method, Class<? extends Annotation> ann) {
		return getAnnotation(JavaUtils.concatNullable(method.visibleAnnotations, method.invisibleAnnotations), ann);
	}
	
	public static AnnotationNode getAnnotation(Iterable<AnnotationNode> annotations, Class<? extends Annotation> ann) {
		String desc = Type.getDescriptor(ann);
		for (AnnotationNode node : annotations) {
			if (node.desc.equals(desc)) {
				return node;
			}
		}
		return null;
	}
	
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return getAnnotation(field, annotation) != null;
	}
	
	public static boolean hasAnnotation(ClassNode clazz, Class<? extends Annotation> annotation) {
		return getAnnotation(clazz, annotation) != null;
	}
	
	public static boolean hasAnnotation(MethodNode method, Class<? extends Annotation> annotation) {
		return getAnnotation(method, annotation) != null;
	}

	
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
	}
	
	public static ClassInfo getClassInfo(Class<?> clazz) {
		return new ClassInfoFromClazz(clazz);
	}
	
	public static ClassInfo getClassInfo(ClassNode clazz) {
		return new ClassInfoFromNode(clazz);
	}

	public static ClassInfo getClassInfo(String className) {
		className = className.replace('/', '.');
		Class<?> clazz;
		if ((clazz = SevenCommons.REFLECTOR.findLoadedClass(CLASSLOADER, className)) != null) {
			return new ClassInfoFromClazz(clazz);
		} else {
			try {
				byte[] bytes = CLASSLOADER.getClassBytes(className);
				if (bytes == null) {
					throw new ClassNotFoundException(className);
				}
				return new ClassInfoFromNode(getThinClassNode(bytes));
			} catch (Exception e) {
				throw JavaUtils.throwUnchecked(e);
			}
		}
	}

	public static interface ClassInfo {
		
		Collection<String> interfaces();
		
		String superName();
		
		String internalName();
		
		boolean isInterface();

		boolean hasAnnotation(Class<? extends Annotation> annotation);
		
	}
	
	private static final class ClassInfoFromClazz implements ClassInfo {

		private final Class<?> clazz;
		private final Collection<String> interfaces;
		
		ClassInfoFromClazz(Class<?> clazz) {
			this.clazz = clazz;
			interfaces = Collections2.transform(Arrays.asList(clazz.getInterfaces()), ClassToNameFunc.INSTANCE);
		}

		@Override
		public Collection<String> interfaces() {
			return interfaces;
		}

		@Override
		public String superName() {
			Class<?> s = clazz.getSuperclass();
			return s == null ? null : Type.getInternalName(s);
		}

		@Override
		public String internalName() {
			return Type.getInternalName(clazz);
		}

		@Override
		public boolean isInterface() {
			return clazz.isInterface();
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return clazz.isAnnotationPresent(annotation);
		}
	}
	
	private static final class ClassInfoFromNode implements ClassInfo {

		private final ClassNode clazz;
		
		ClassInfoFromNode(ClassNode clazz) {
			this.clazz = clazz;
		}

		@Override
		public Collection<String> interfaces() {
			return clazz.interfaces;
		}

		@Override
		public String superName() {
			return clazz.superName;
		}

		@Override
		public String internalName() {
			return clazz.name;
		}

		@Override
		public boolean isInterface() {
			return (clazz.access & ACC_INTERFACE) == ACC_INTERFACE;
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotation) {
			return ASMUtils.hasAnnotation(clazz, annotation);
		}
	}
	
	private static enum ClassToNameFunc implements Function<Class<?>, String> {
		INSTANCE;

		@Override
		public String apply(Class<?> input) {
			return Type.getInternalName(input);
		}
	}
	
	public static boolean isAssignableFrom(ClassInfo parent, ClassInfo child) {
		// cheap tests first
		if (parent.internalName().equals("java/lang/Object") // everything is assignable to Object
				|| parent.internalName().equals(child.internalName()) // parent == child => works
				|| parent.internalName().equals(child.superName()) // parent == child.super => works
				|| child.interfaces().contains(parent.internalName())) { // child.interfaces contains parent => works
			return true;
		}
		// object doesn't implement anything
		if (child.internalName().equals("java/lang/Object") && parent.isInterface()) {
			return false;
		}
		// now we need to loop through every superinterface
		for (String iface : child.interfaces()) {
			if (isAssignableFrom(parent, getClassInfo(iface))) {
				return true;
			}
		}
		// interfaces don't have superclasses
		if (child.isInterface()) {
			return false;
		}
		// loop through every superclass
		ClassInfo current = child;
		// don't need to check Object, as that would have been covered by the cheap tests
		while (!current.internalName().equals("java/lang/Object") && !current.superName().equals("java/lang/Object")) {
			current = getClassInfo(current.superName());
			if (isAssignableFrom(parent, current)) {
				return true;
			}
		}
		return false;
	}
}
