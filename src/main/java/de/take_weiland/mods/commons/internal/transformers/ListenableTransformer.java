package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.util.Listenable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author diesieben07
 */
public class ListenableTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		if ((clazz.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE || !hasListenable(clazz.interfaces)) {
			return;
		}

		String name = "_sc_listeners";
		String desc = Type.getDescriptor(List.class);
		FieldNode listeners = new FieldNode(Opcodes.ACC_PRIVATE, name, desc, null, null);
		clazz.fields.add(listeners);

		addRegisterMethod(clazz, listeners);
		addUnregisterMethod(clazz, listeners);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/ListenableInternal");
	}

	private boolean hasListenable(Collection<String> ifaces) {
		for (String iface : ifaces) {
			if (iface.equals("de/take_weiland/mods/commons/util/Listenable") || hasListenable(ASMUtils.getClassInfo(iface).interfaces())) {
				return true;
			}
		}
		return false;
	}

	private void addRegisterMethod(ClassNode clazz, FieldNode listeners) {
		String name = "_sc_registerListener";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Listenable.Listener.class));
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;

		LabelNode notNull = new LabelNode();

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, listeners.name, listeners.desc));
		insns.add(new InsnNode(Opcodes.DUP));
		insns.add(new JumpInsnNode(Opcodes.IFNONNULL, notNull));

		String arrayList = Type.getInternalName(ArrayList.class);
		insns.add(new InsnNode(Opcodes.POP));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new TypeInsnNode(Opcodes.NEW, arrayList));
		insns.add(new InsnNode(Opcodes.DUP));
		insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, arrayList, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE)));
		insns.add(new InsnNode(Opcodes.DUP_X1));
		insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, listeners.name, listeners.desc));

		insns.add(notNull);
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class));
		insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(List.class), "add", desc));
		insns.add(new InsnNode(Opcodes.POP));

		insns.add(new InsnNode(Opcodes.RETURN));

		clazz.methods.add(method);
	}

	private void addUnregisterMethod(ClassNode clazz, FieldNode listeners) {
		String name = "_sc_removeListener";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Listenable.Listener.class));
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;

		LabelNode notNull = new LabelNode();

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, listeners.name, listeners.desc));
		insns.add(new InsnNode(Opcodes.DUP));
		insns.add(new JumpInsnNode(Opcodes.IFNONNULL, notNull));

		insns.add(new InsnNode(Opcodes.POP));
		insns.add(new InsnNode(Opcodes.RETURN));

		insns.add(notNull);
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class));
		insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(List.class), "remove", desc));
		insns.add(new InsnNode(Opcodes.POP));

		insns.add(new InsnNode(Opcodes.RETURN));

		clazz.methods.add(method);
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/")
				&& !internalName.startsWith("de/take_weiland/mods/commons/internal/");
	}
}
