package de.take_weiland.mods.commons.net;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ASMUtils.ClassInfo;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;

public final class PacketTransformer extends SelectiveTransformer {

	private final ClassInfo modPacketCI = ASMUtils.getClassInfo(ModPacket.class);
	
	@Override
	protected boolean transform(ClassNode clazz, String className) {
		if (!ASMUtils.isAssignableFrom(modPacketCI, ASMUtils.getClassInfo(clazz))) {
			return false;
		}
		
		if (!hasDefaultConstructor(clazz)) {
			addDefaultConstructor(clazz);
		}
		
		FieldNode factory = createFactoryField(clazz);
		FieldNode type = createTypeField(clazz);
		
		createGetter(clazz, factory, "_sc_getFactory");
		createGetter(clazz, type, "_sc_getType");
		
		clazz.interfaces.add("de/take_weiland/mods/commons/net/PacketWithFactory");
		
		return true;
	}
	
	private void createGetter(ClassNode clazz, FieldNode field, String name) {
		String desc = getMethodDescriptor(getType(field.desc));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		
		method.instructions.add(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
		method.instructions.add(new InsnNode(ARETURN));
		
		clazz.methods.add(method);
	}
	
	private FieldNode createFactoryField(ClassNode clazz) {
		String name = "_sc_packetfactory";
		String desc = getDescriptor(PacketFactory.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	}
	
	private FieldNode createTypeField(ClassNode clazz) {
		String name = "_sc_packettype";
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
	protected boolean transforms(String className) {
		return !className.startsWith("net.minecraft.")
				&& !className.startsWith("net.minecraftforge.")
				&& !className.startsWith("cpw.mods.fml.")
				&& !className.startsWith("de.take_weiland.mods.commons.netx.");
	}

}
