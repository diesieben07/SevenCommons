package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.launchwrapper.IClassNameTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.take_weiland.mods.commons.internal.SevenCommons.CLASSLOADER;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

public final class ASMUtils {

	private ASMUtils() { }
	
	public static final String deobfuscate(String className, FieldNode field) {
		return deobfuscateField(className, field.name, field.desc);
	}
	
	public static final String deobfuscateField(String className, String fieldName, String desc) {
		return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(className, fieldName, desc);
	}
	
	public static final String deobfuscate(String className, MethodNode method) {
		return deobfuscateMethod(className, method.name, method.desc);
	}
	
	public static final String deobfuscateMethod(String className, String methodName, String desc) {
		return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(className, methodName, desc);
	}
	
	public static final String getFieldDescriptor(ClassNode clazz, String fieldName) {
		for (FieldNode field : clazz.fields) {
			if (field.name.equals(fieldName)) {
				return field.desc;
			}
		}
		return null;
	}
	
	public static final boolean useMcpNames() {
		return SevenCommons.MCP_ENVIRONMENT;
	}
	
	public static final AbstractInsnNode findLastReturn(MethodNode method) {
		int searchFor = Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN);
		AbstractInsnNode found = null;
		for (int i = 0; i < method.instructions.size(); i++) {
			AbstractInsnNode insn = method.instructions.get(i);
			if (insn.getOpcode() == searchFor) {
				found = insn;
			}
		}
		return found;
	}
	
	public static final String makeNameInternal(String name) {
		return name.replace('.', '/');
	}
	
	public static final String undoInternalName(String name) {
		return name.replace('/', '.');
	}
	
	private static IClassNameTransformer nameTransformer;
	private static boolean nameTransChecked = false;
	
	public static IClassNameTransformer getClassNameTransformer() {
		if (!nameTransChecked) {
			Iterable<IClassNameTransformer> nameTransformers = Iterables.filter(SevenCommons.CLASSLOADER.getTransformers(), IClassNameTransformer.class);
			nameTransformer = Iterables.getOnlyElement(nameTransformers, null);
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

	public static ClassNode getClassNode(String name, int flags) {
		try {
			return getClassNode(SevenCommons.CLASSLOADER.getClassBytes(obfuscateClass(name)));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ClassNode getClassNode(byte[] bytes) {
		return getClassNode(bytes, 0);
	}

	public static ClassNode getClassNode(byte[] bytes, int flags) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, flags);
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
		return getClassInfo(className, 0);
	}
	
	public static ClassInfo getClassInfo(String className, int flags) {
		try {
			byte[] bytes = CLASSLOADER.getClassBytes(className);
			if (bytes != null) {
				return new ClassInfoFromNode(ASMUtils.getClassNode(bytes, flags));
			} else {
				return new ClassInfoFromClazz(Class.forName(ASMUtils.undoInternalName(className)));
			}
		} catch (Exception e) {
			throw JavaUtils.throwUnchecked(e);
		}
	}

	public static List<MethodNode> findRootConstructors(ClassNode clazz) {
		List<MethodNode> cnstrs = Lists.newArrayList();
		methods:
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>")) {
				int len = method.instructions.size();
				for (int i = 0; i < len; ++i) {
					AbstractInsnNode insn = method.instructions.get(i);
					if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
						MethodInsnNode min = (MethodInsnNode) insn;
						if (min.owner.equals(clazz.name) && min.name.equals("<init>")) {
							continue methods;
						}
					}
				}
				cnstrs.add(method);
			}
		}
		return cnstrs;
	}

	public static interface ClassInfo {
		
		List<String> interfaces();
		
		String superName();

		ClassInfo getSuperclass();

		MethodInfo[] getMethods();
		
		String internalName();
		
		boolean isInterface();
		
	}

	public static interface MethodInfo {

		ClassInfo getContainingClass();

		Type[] getArguments();

		String getName();

		Type getReturnType();

	}

	private static abstract class AbstractMethodInfo implements MethodInfo {

		private final ClassInfo declarer;

		AbstractMethodInfo(ClassInfo declarer) {
			this.declarer = declarer;
		}

		@Override
		public ClassInfo getContainingClass() {
			return declarer;
		}
	}

	private static final class MethodInfoReflective extends AbstractMethodInfo {

		private Method method;

		private MethodInfoReflective(ClassInfo declarer, Method method) {
			super(declarer);
			this.method = method;
		}

		@Override
		public Type[] getArguments() {
			return Type.getArgumentTypes(method);
		}

		@Override
		public String getName() {
			return method.getName();
		}

		@Override
		public Type getReturnType() {
			return Type.getReturnType(method);
		}
	}

	private static final class MethodInfoASM extends AbstractMethodInfo {

		private MethodNode method;

		private MethodInfoASM(ClassInfo declarer, MethodNode method) {
			super(declarer);
			this.method = method;
		}

		@Override
		public Type getReturnType() {
			return Type.getReturnType(method.desc);
		}

		@Override
		public String getName() {
			return method.name;
		}

		@Override
		public Type[] getArguments() {
			return Type.getArgumentTypes(method.desc);
		}
	}
	
	private static final class ClassInfoFromClazz implements ClassInfo {

		private final Class<?> clazz;
		private List<String> interfaces;
		
		ClassInfoFromClazz(Class<?> clazz) {
			this.clazz = clazz;
		}

		@Override
		public List<String> interfaces() {
			return interfaces == null ? (interfaces = interfaces0()) : interfaces;
		}

		private List<String> interfaces0() {
			Class<?>[] ifaces = clazz.getInterfaces();
			int len = ifaces.length;
			String[] names = new String[len];
			for (int i = 0; i < len; ++i) {
				names[i] = ASMUtils.makeNameInternal(ifaces[i].getName());
			}
			return Arrays.asList(names);
		}

		@Override
		public String superName() {
			Class<?> s = clazz.getSuperclass();
			return s == null ? null : ASMUtils.makeNameInternal(s.getCanonicalName());
		}

		@Override
		public ClassInfo getSuperclass() {
			Class<?> s = clazz.getSuperclass();
			return s == null ? null : ASMUtils.getClassInfo(s);
		}

		@Override
		public String internalName() {
			return ASMUtils.makeNameInternal(clazz.getCanonicalName());
		}

		@Override
		public boolean isInterface() {
			return clazz.isInterface();
		}

		private MethodInfo[] methods;

		@Override
		public MethodInfo[] getMethods() {
			if (methods == null) {
				Method[] reflMethods = clazz.getDeclaredMethods();
				int len = reflMethods.length;
				methods = new MethodInfo[len];
				for (int i = 0; i < len; ++i) {
					methods[i] = new MethodInfoReflective(this, reflMethods[i]);
				}
			}
			return methods;
		}
	}
	
	private static final class ClassInfoFromNode implements ClassInfo {

		private final ClassNode clazz;
		
		ClassInfoFromNode(ClassNode clazz) {
			this.clazz = clazz;
		}

		@Override
		public List<String> interfaces() {
			return clazz.interfaces;
		}

		@Override
		public String superName() {
			return clazz.superName;
		}

		@Override
		public ClassInfo getSuperclass() {
			return ASMUtils.getClassInfo(clazz.superName);
		}

		@Override
		public String internalName() {
			return clazz.name;
		}

		@Override
		public boolean isInterface() {
			return (clazz.access & ACC_INTERFACE) == ACC_INTERFACE;
		}

		private MethodInfo[] methods;

		@Override
		public MethodInfo[] getMethods() {
			if (methods == null) {
				List<MethodNode> nodeMethods = clazz.methods;
				int len = nodeMethods.size();
				methods = new MethodInfo[len];
				for (int i = 0; i < len; ++i) {
					methods[i] = new MethodInfoASM(this, nodeMethods.get(i));
				}
			}
			return methods;
		}
		
	}

	private static enum ClassToNameFunc implements Function<Class<?>, String> {
		INSTANCE;

		@Override
		public String apply(Class<?> input) {
			return ASMUtils.makeNameInternal(input.getCanonicalName());
		}
	}
	
	public static boolean isAssignableFrom(ClassInfo parent, ClassInfo child) {
		if (parent.internalName().equals(child.internalName()) || parent.internalName().equals(child.superName()) || child.interfaces().contains(parent.internalName())) {
			return true;
		} else if (child.superName() != null && !child.superName().equals("java/lang/Object")) {
			return isAssignableFrom(parent, getClassInfo(child.superName()));
		} else {
			return false;
		}
	}
}
