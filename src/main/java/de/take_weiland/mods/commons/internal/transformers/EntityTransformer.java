package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.Internal;
import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

@Internal
public final class EntityTransformer extends SelectiveTransformer {

	@Override
	protected boolean transform(ClassNode clazz, String className) {
		FieldNode syncedProps = addSyncedPropsField(clazz);

		for (MethodNode method : clazz.methods) {
			if (method.name.equals("registerExtendedProperties")) {
				transformRegisterProps(clazz, method, syncedProps);
			} else if (method.name.equals(ASMConstants.M_ON_UPDATE_MCP) || ASMUtils.deobfuscate(clazz.name, method).equals(ASMConstants.M_ON_UPDATE_SRG)) {
				transformOnUpdate(clazz, method, syncedProps);
			}
		}
		addPropertyGetter(clazz, syncedProps);
		addPropertySetter(clazz, syncedProps);
		
		clazz.interfaces.add("de/take_weiland/mods/commons/sync/EntityProxy");
		
		return true;
	}

	private void addPropertySetter(ClassNode clazz, FieldNode syncedProps) {
		String name = "_sc_sync_setSyncedProperties";
		String desc = getMethodDescriptor(VOID_TYPE, getType(List.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new VarInsnNode(ALOAD, 1));
		method.instructions.add(new FieldInsnNode(PUTFIELD, clazz.name, syncedProps.name, syncedProps.desc));
		method.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(method);
	}

	private void addPropertyGetter(ClassNode clazz, FieldNode syncedProps) {
		String name = "_sc_sync_getSyncedProperties";
		String desc = getMethodDescriptor(getType(List.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new FieldInsnNode(GETFIELD, clazz.name, syncedProps.name, syncedProps.desc));
		method.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(method);
	}

	private FieldNode addSyncedPropsField(ClassNode clazz) {
		FieldNode field = new FieldNode(ACC_PUBLIC, "_sc_sync_entityprops", getDescriptor(List.class), null, null);
		clazz.fields.add(field);
		return field;
	}
	
	private void transformRegisterProps(ClassNode clazz, MethodNode method, FieldNode syncedProps) {
		InsnList insns = new InsnList();

		Type listType = getType(List.class);

		insns.add(new VarInsnNode(ALOAD, 0)); // for PUTFIELD
		insns.add(new InsnNode(DUP)); // for GETFIELD
		insns.add(new InsnNode(DUP)); // method parameter
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, syncedProps.name, syncedProps.desc));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new VarInsnNode(ALOAD, 2));

		String owner = "de/take_weiland/mods/commons/internal/SyncASMHooks";
		String name = "onNewEntityProperty";
		String desc = getMethodDescriptor(listType, getObjectType(clazz.name), listType, getType(String.class), getType(IExtendedEntityProperties.class));
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		insns.add(new FieldInsnNode(PUTFIELD, clazz.name, syncedProps.name, syncedProps.desc));

		AbstractInsnNode methodEnd = ASMUtils.findLastReturn(method).getPrevious(); // get the ALOAD
		method.instructions.insertBefore(methodEnd, insns);
	}
	
	private void transformOnUpdate(ClassNode clazz, MethodNode method, FieldNode syncedProps) {
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new InsnNode(DUP));
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, syncedProps.name, syncedProps.desc));

		String owner = "de/take_weiland/mods/commons/internal/SyncASMHooks";
		String name = "tickSyncedProperties";
		String desc = getMethodDescriptor(VOID_TYPE, getObjectType(clazz.name), getType(List.class));
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		method.instructions.insert(insns);
	}
	
	@Override
	protected boolean transforms(String className) {
		return "net.minecraft.entity.Entity".equals(className);
	}

}
