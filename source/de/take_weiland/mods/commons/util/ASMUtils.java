package de.take_weiland.mods.commons.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import de.take_weiland.mods.commons.internal.SevenCommons;

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
	
	public static final String obfuscateSrg(String srgName) {
		int lastUnderscore = srgName.lastIndexOf('_');
		return lastUnderscore > 0 ? srgName.substring(lastUnderscore + 1) : srgName;
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
	
	public static final MethodInsnNode generateStaticMethodCall(String targetClass, String methodName, Type returnType, Type... params) {
		return new MethodInsnNode(Opcodes.INVOKESTATIC, makeNameInternal(targetClass), methodName, Type.getMethodDescriptor(returnType, params));
	}
	
	public static final MethodInsnNode generateMethodCall(Method method) {
		int opcode = Modifier.isStatic(method.getModifiers()) ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL;
		return new MethodInsnNode(opcode, Type.getInternalName(method.getDeclaringClass()), method.getName(), Type.getMethodDescriptor(method));
	}
	
}
