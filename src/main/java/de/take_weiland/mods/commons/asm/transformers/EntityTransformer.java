package de.take_weiland.mods.commons.asm.transformers;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

import java.util.Map;

import net.minecraftforge.common.IExtendedEntityProperties;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;

public class EntityTransformer extends SelectiveTransformer {

	public static String SYNCED_PROPS_NAME = "_SC_syncedProps";
	private static String SYNCER_NEW_PROP = "onNewEntityProperty";
	private static String SYNCER_NEW_PROP_DESC = getMethodDescriptor(getType(Map.class), getType(Map.class), getType(String.class), getType(IExtendedEntityProperties.class));
	
	@Override
	protected boolean transform(ClassNode clazz, String className) {
		addSyncedPropsField(clazz);
		transformRegisterProps(clazz);
		transformOnUpdate(clazz);
		return true;
	}

	private void addSyncedPropsField(ClassNode clazz) {
		FieldNode field = new FieldNode(ACC_PUBLIC, SYNCED_PROPS_NAME, getDescriptor(Map.class), null, null);
		clazz.fields.add(field);
	}
	
	private void transformRegisterProps(ClassNode clazz) {
		MethodNode method = null;
		for (MethodNode m : clazz.methods) {
			if (m.name.equals("registerExtendedProperties")) {
				method = m;
			}
		}
		
		if (method != null) {
			InsnList insns = new InsnList();
			
			insns.add(new VarInsnNode(ALOAD, 0)); // for PUTFIELD
			insns.add(new InsnNode(DUP)); // for GETFIELD
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, SYNCED_PROPS_NAME, getDescriptor(Map.class)));
			insns.add(new VarInsnNode(ALOAD, 1));
			insns.add(new VarInsnNode(ALOAD, 2));
			insns.add(new MethodInsnNode(INVOKESTATIC, SyncingTransformer.SYNCER_CLASS, SYNCER_NEW_PROP, SYNCER_NEW_PROP_DESC));
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, SYNCED_PROPS_NAME, getDescriptor(Map.class)));
			
			AbstractInsnNode lastReturn = ASMUtils.findLastReturn(method);
			method.instructions.insertBefore(lastReturn, insns);
		}
	}
	
	private void transformOnUpdate(ClassNode clazz) {
		MethodNode method = null;
		for (MethodNode m : clazz.methods) {
			if (m.name.equals("onUpdate") || ASMUtils.deobfuscate(clazz.name, m).equals("func_70071_h_")) {
				method = m;
			}
		}
		
		if (method != null) {
			InsnList insns = new InsnList();
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new InsnNode(DUP));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, SYNCED_PROPS_NAME, getDescriptor(Map.class)));
			insns.add(new MethodInsnNode(INVOKESTATIC, SyncingTransformer.SYNCER_CLASS, "syncProperties", getMethodDescriptor(VOID_TYPE, getObjectType(clazz.name), getType(Map.class))));
			
			method.instructions.insert(insns);
		}
	}
	
	@Override
	protected boolean transforms(String className) {
		return "net.minecraft.entity.Entity".equals(className);
	}

}
