package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.Listenable;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.internal.ListenableInternal;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;
import static de.take_weiland.mods.commons.asm.ASMUtils.isAssignableFrom;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ListenableTransformer extends AbstractASMTransformer {

	private static final ASMUtils.ClassInfo listenableCI = getClassInfo(Listenable.class);
	private static final String listDesc = getDescriptor(List.class);
	private static final String getterDesc = getMethodDescriptor(getType(List.class));
	private static final String setterDesc = getMethodDescriptor(VOID_TYPE, getType(List.class));

	@Override
	public void transform(ClassNode clazz) {
		if (!shouldTransform(clazz)) {
			return;
		}

		FieldNode field = new FieldNode(ACC_PRIVATE, "_sc$listeners", listDesc, null, null);
		clazz.fields.add(field);

		MethodNode method = new MethodNode(ACC_PUBLIC, ListenableInternal.GETTER, getterDesc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(ARETURN));
		clazz.methods.add(method);

		method = new MethodNode(ACC_PUBLIC, ListenableInternal.SETTER, setterDesc, null, null);
		insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(RETURN));
		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/ListenableInternal");
	}

	private boolean shouldTransform(ClassNode clazz) {
		return (clazz.access & ACC_INTERFACE) != ACC_INTERFACE
				&& isAssignableFrom(listenableCI, getClassInfo(clazz))
				&& !isAssignableFrom(listenableCI, getClassInfo(clazz.superName));
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/fml/");
	}
}
