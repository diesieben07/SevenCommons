package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import de.take_weiland.mods.commons.net.SimplePacketType;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public final class SimplePacketTypeTransformer extends AbstractASMTransformer {

	public static final String FACTORY_FIELD = "_sc$packetFactory";
	private static final ASMUtils.ClassInfo simplePacketTypeInfo = getClassInfo(SimplePacketType.class);

	@Override
	public final void transform(ClassNode clazz) {
		if (!simplePacketTypeInfo.isAssignableFrom(getClassInfo(clazz))) {
			return;
		}

		if ((clazz.access & ACC_ENUM) != ACC_ENUM) {
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
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/");
	}
}
