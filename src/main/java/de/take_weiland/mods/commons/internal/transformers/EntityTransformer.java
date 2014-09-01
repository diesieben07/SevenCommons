package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.EntityProxy;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class EntityTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		ASMVariable syncProps = createSyncedProps(clazz);

		transformRegisterProperties(clazz, ASMUtils.requireMethod(clazz, "registerExtendedProperties"), syncProps);

		Type worldType = getObjectType("net/minecraft/world/World");
		transformConstructor(clazz, ASMUtils.requireMethod(clazz, "<init>", Type.getMethodDescriptor(VOID_TYPE, worldType)));

		transformOnUpdate(clazz, ASMUtils.requireMinecraftMethod(clazz, MCPNames.M_ON_UPDATE), syncProps);

		clazz.interfaces.add(EntityProxy.CLASS_NAME);
		return true;
	}

	private static void transformOnUpdate(ClassNode clazz, MethodNode method, ASMVariable syncProps) {
		String owner = ASMHooks.CLASS_NAME;
		String name = ASMHooks.TICK_SYNC_PROPS;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(List.class));
		CodePiece code = CodePieces.invokeStatic(owner, name, desc, syncProps.get());
		code.prependTo(method.instructions);
	}

	private static void transformConstructor(ClassNode clazz, MethodNode method) {
		AbstractInsnNode insn = ASMUtils.findLastReturn(method);
		InsnList hook = new InsnList();
		hook.add(new VarInsnNode(ALOAD, 0));
		String owner = ASMHooks.CLASS_NAME;
		String name = ASMHooks.ENTITY_CONSTRUCT;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getObjectType(clazz.name));
		hook.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		method.instructions.insertBefore(insn, hook);
	}

	private static void transformRegisterProperties(ClassNode clazz, MethodNode method, ASMVariable syncProps) {
		AbstractInsnNode insn = ASMUtils.findLastReturn(method);
		do {
			insn = insn.getPrevious();
		} while (insn.getOpcode() != ALOAD);

		String owner = ASMHooks.CLASS_NAME;
		String name = ASMHooks.NEW_ENTITY_PROPS;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(String.class), getType(IExtendedEntityProperties.class), getObjectType(clazz.name));
		CodePiece hook = CodePieces.invokeStatic(owner, name, desc,
				CodePieces.of(new VarInsnNode(ALOAD, 1)), CodePieces.of(new VarInsnNode(ALOAD, 2)), CodePieces.getThis());

		hook.insertBefore(method.instructions, insn);
	}

	private static ASMVariable createSyncedProps(ClassNode clazz) {
		String name = "_sc$syncedExtProps";
		String desc = Type.getDescriptor(List.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_TRANSIENT, name, desc, null, null);
		clazz.fields.add(field);

		name = EntityProxy.GET_PROPERTIES;
		desc = Type.getMethodDescriptor(Type.getType(List.class));
		MethodNode method = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method);

		name = EntityProxy.SET_PROPERTIES;
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(List.class));
		method = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(RETURN));

		clazz.methods.add(method);
		return ASMVariables.of(clazz, field, CodePieces.getThis());
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/entity/Entity".equals(internalName);
	}
}
