package de.take_weiland.mods.commons.asm;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.JavaUtils;

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
	
	public static final MethodInsnNode generateMethodCall(String targetClass, String methodName, Type returnType, Type... params) {
		return new MethodInsnNode(Opcodes.INVOKEVIRTUAL, makeNameInternal(targetClass), methodName, Type.getMethodDescriptor(returnType, params));
	}
	
	public static final MethodInsnNode generateStaticMethodCall(String targetClass, String methodName, Type returnType, Type... params) {
		return new MethodInsnNode(Opcodes.INVOKESTATIC, makeNameInternal(targetClass), methodName, Type.getMethodDescriptor(returnType, params));
	}
	
	public static final MethodInsnNode generateMethodCall(Method method) {
		int opcode = Modifier.isStatic(method.getModifiers()) ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
		return new MethodInsnNode(opcode, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
	}

	public static ClassNode getClassNode(byte[] bytes) {
		ClassReader reader = new ClassReader(bytes);
		ClassNode clazz = new ClassNode();
		reader.accept(clazz, 0);
		return clazz;
	}

	public static ClassNode getClassNode(String name) {
		try {
			return getClassNode(SevenCommons.CLASSLOADER.getClassBytes(name));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean hasAnnotation(FieldNode field, Class<? extends Annotation> annotation) {
		return hasAnnotation(field, Type.getType(annotation));
	}
	
	public static boolean hasAnnotation(FieldNode field, Type annotation) {
		return containsAnnotation(Iterators.concat(JavaUtils.nullToEmpty(field.visibleAnnotations).iterator(), JavaUtils.nullToEmpty(field.invisibleAnnotations).iterator()), annotation.getDescriptor());
	}
	
	private static boolean containsAnnotation(Iterator<AnnotationNode> annotations, final String annotationDesc) {
		return Iterators.any(annotations, new Predicate<AnnotationNode>() {

			@Override
			public boolean apply(AnnotationNode node) {
				return node.desc.equals(annotationDesc);
			}
		});
	}
	
	public static boolean isPrimitive(Type type) {
		return type.getSort() != Type.ARRAY && type.getSort() != Type.OBJECT && type.getSort() != Type.METHOD;
	}
}