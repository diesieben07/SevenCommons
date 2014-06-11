package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.EntityProxy;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public final class EntityTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		FieldNode syncedProps = addSyncedPropsField(clazz);

		String onUpdate = MCPNames.use() ? M_ON_UPDATE_MCP : M_ON_UPDATE_SRG;

		for (MethodNode method : clazz.methods) {
			if (method.name.equals(M_REGISTER_EXT_PROPS)) {
				transformRegisterProps(clazz, method, syncedProps);
			} else if (method.name.equals(onUpdate)) {
				transformOnUpdate(clazz, method, syncedProps);
			}
		}
		addPropertyGetter(clazz, syncedProps);
		addPropertySetter(clazz, syncedProps);
		
		clazz.interfaces.add("de/take_weiland/mods/commons/internal/EntityProxy");
		return true;
	}

	private void addPropertySetter(ClassNode clazz, FieldNode syncedProps) {
		String name = EntityProxy.SETTER;
		String desc = getMethodDescriptor(VOID_TYPE, getType(List.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new VarInsnNode(ALOAD, 1));
		method.instructions.add(new FieldInsnNode(PUTFIELD, clazz.name, syncedProps.name, syncedProps.desc));
		method.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(method);
	}

	private void addPropertyGetter(ClassNode clazz, FieldNode syncedProps) {
		String name = EntityProxy.GETTER;
		String desc = getMethodDescriptor(getType(List.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new FieldInsnNode(GETFIELD, clazz.name, syncedProps.name, syncedProps.desc));
		method.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(method);
	}

	private FieldNode addSyncedPropsField(ClassNode clazz) {
		FieldNode field = new FieldNode(ACC_PUBLIC, "_sc$syncedEntityprops", getDescriptor(List.class), null, null);
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
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, syncedProps.name, syncedProps.desc));

		String owner = "de/take_weiland/mods/commons/internal/SyncASMHooks";
		String name = "tickSyncedProperties";
		String desc = getMethodDescriptor(VOID_TYPE, getObjectType(clazz.name), getType(List.class));
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		SyncingTransformer.addBaseSyncMethodCall(clazz.name, CodePieces.getThis()).appendTo(insns);

		method.instructions.insert(insns);
	}
	
	@Override
	public boolean transforms(String className) {
		return "net/minecraft/entity/Entity".equals(className);
	}

}
