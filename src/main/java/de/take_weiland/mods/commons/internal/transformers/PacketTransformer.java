package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public final class PacketTransformer extends AbstractAnalyzingTransformer {

	private static final ClassInfo modPacketCI = ASMUtils.getClassInfo("de/take_weiland/mods/commons/net/ModPacket");

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isInterface() || classInfo.isAbstract() || !modPacketCI.isAssignableFrom(classInfo)) {
			return false;
		}
		if (!hasDefaultConstructor(clazz)) {
			addDefaultConstructor(clazz);
		}
		
		FieldNode type = createTypeField(clazz);
		
		createGetter(clazz, type, ModPacketProxy.GET_TYPE);
		
		clazz.interfaces.add("de/take_weiland/mods/commons/internal/ModPacketProxy");
		return true;
	}
	
	private static void createGetter(ClassNode clazz, FieldNode field, String name) {
		String desc = getMethodDescriptor(getType(field.desc));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		
		method.instructions.add(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
		method.instructions.add(new InsnNode(ARETURN));
		
		clazz.methods.add(method);
	}

	public static final String TYPE_FIELD = "_sc$packettype";
	
	private static FieldNode createTypeField(ClassNode clazz) {
		String name = TYPE_FIELD;
		String desc = getDescriptor(Enum.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	} 
	
	private static void addDefaultConstructor(ClassNode clazz) {
		String name = "<init>";
		String desc = getMethodDescriptor(VOID_TYPE);
		MethodNode cstr = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		cstr.instructions.add(new VarInsnNode(ALOAD, 0));
		cstr.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
		cstr.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(cstr);
	}

	private static boolean hasDefaultConstructor(ClassNode clazz) {
		String desc = getMethodDescriptor(VOID_TYPE);
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>") && method.desc.equals(desc)) {
				return true;
			}
		}
		return false;
	}
}
