package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public final class SimplePacketTypeTransformer implements ASMClassTransformer {

	public static final String FACTORY_FIELD = "_sc$packetFactory";
	private static final ClassInfo simplePacketTypeInfo = ClassInfo.of("de/take_weiland/mods/commons/net/SimplePacketType");

	@Override
	public final boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isAbstract() || classInfo.isInterface() || !simplePacketTypeInfo.isAssignableFrom(classInfo)) {
			return false;
		}

		if (!classInfo.isEnum()) {
			throw new IllegalArgumentException("SimplePacketType only allowed on Enums! Class: " + clazz.name);
		}

		FieldNode factoryField = new FieldNode(ACC_PRIVATE | ACC_STATIC, FACTORY_FIELD, getDescriptor(Object.class), null, null);
		clazz.fields.add(factoryField);

		MethodNode method = new MethodNode(ACC_PUBLIC, SimplePacketTypeProxy.GETTER, getMethodDescriptor(getType(Object.class)), null, null);
		InsnList insns = method.instructions;
		insns.add(new FieldInsnNode(GETSTATIC, clazz.name, factoryField.name, factoryField.desc));
		insns.add(new InsnNode(ARETURN));
		clazz.methods.add(method);

		method = new MethodNode(ACC_PUBLIC, SimplePacketTypeProxy.SETTER, getMethodDescriptor(VOID_TYPE, getType(Object.class)), null, null);
		insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new FieldInsnNode(PUTSTATIC, clazz.name, factoryField.name, factoryField.desc));

		insns.add(new InsnNode(RETURN));
		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/SimplePacketTypeProxy");
		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
