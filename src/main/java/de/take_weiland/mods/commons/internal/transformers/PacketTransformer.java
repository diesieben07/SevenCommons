package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ASMUtils.ClassInfo;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.internal.PacketWithFactory;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketFactory;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public final class PacketTransformer extends AbstractASMTransformer {

	private final ClassInfo modPacketCI = ASMUtils.getClassInfo(ModPacket.class);
	
	@Override
	public void transform(ClassNode clazz) {
		if (!ASMUtils.isAssignableFrom(modPacketCI, ASMUtils.getClassInfo(clazz))) {
			return;
		}
		
		if (!hasDefaultConstructor(clazz)) {
			addDefaultConstructor(clazz);
		}
		
		FieldNode factory = createFactoryField(clazz);
		FieldNode type = createTypeField(clazz);
		
		createGetter(clazz, factory, PacketWithFactory.GET_FACTORY);
		createGetter(clazz, type, PacketWithFactory.GET_TYPE);
		
		clazz.interfaces.add("de/take_weiland/mods/commons/internal/PacketWithFactory");
	}
	
	private void createGetter(ClassNode clazz, FieldNode field, String name) {
		String desc = getMethodDescriptor(getType(field.desc));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		
		method.instructions.add(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
		method.instructions.add(new InsnNode(ARETURN));
		
		clazz.methods.add(method);
	}

	public static final String FACTORY_FIELD = "_sc$packetfactory";

	private FieldNode createFactoryField(ClassNode clazz) {
		String name = FACTORY_FIELD;
		String desc = getDescriptor(PacketFactory.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	}

	public static final String TYPE_FIELD = "_sc$packettype";
	
	private FieldNode createTypeField(ClassNode clazz) {
		String name = TYPE_FIELD;
		String desc = getDescriptor(Enum.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	} 
	
	private void addDefaultConstructor(ClassNode clazz) {
		String name = "<init>";
		String desc = getMethodDescriptor(VOID_TYPE);
		MethodNode cstr = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		cstr.instructions.add(new VarInsnNode(ALOAD, 0));
		cstr.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
		cstr.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(cstr);
	}

	private boolean hasDefaultConstructor(ClassNode clazz) {
		String desc = getMethodDescriptor(VOID_TYPE);
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>") && method.desc.equals(desc)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean transforms(String className) {
		return !className.startsWith("net/minecraft/")
				&& !className.startsWith("net/minecraftforge/")
				&& !className.startsWith("cpw/mods/fml/");
	}

}
