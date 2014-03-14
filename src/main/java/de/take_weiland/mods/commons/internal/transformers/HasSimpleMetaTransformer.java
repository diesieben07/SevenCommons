package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.MetadataItemProxy;
import de.take_weiland.mods.commons.metadata.Metadata;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class HasSimpleMetaTransformer extends AbstractMetadataTransformer {

	@Override
	boolean matches(ClassInfo classInfo) {
		return hasSimpleMetadataCI.isAssignableFrom(classInfo);
	}

	@Override
	void transformCommon(ClassNode clazz) {
		String name = MetadataItemProxy.GETTER;
		String desc = getMethodDescriptor(getType(Metadata.class), itemStackType);
		MethodNode method1 = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method1.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		name = "getSimpleMetadata";
		desc = getMethodDescriptor(getType(Metadata.class), hasSimpleMetadataType, itemStackType);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method1);

		for (MethodNode method : ASMUtils.getRootConstructors(clazz)) {
			method.instructions.insert(ASMUtils.findFirst(method, INVOKESPECIAL), buildTypeInjector());
		}
	}

	@Override
	void transformBlock(ClassNode clazz) {
		MethodNode method = getBlockGetter();
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new VarInsnNode(ILOAD, 2));
		insns.add(new VarInsnNode(ILOAD, 3));
		insns.add(new VarInsnNode(ILOAD, 4));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		String name = "getSimpleMetadata";
		String desc = getMethodDescriptor(getType(Metadata.class), hasSimpleMetadataType, worldType, INT_TYPE, INT_TYPE, INT_TYPE);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method);
	}

	private void addItemStackMethod(ClassNode clazz) {
		String name = MetadataItemProxy.GETTER;
		String desc = getMethodDescriptor(getType(Metadata.class), itemStackType);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		name = "getSimpleMetadata";
		desc = getMethodDescriptor(getType(Metadata.class), hasSimpleMetadataType, itemStackType);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method);
	}

	private InsnList buildTypeInjector() {
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(ALOAD, 0));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		String name = "injectMetadataHolder";
		String desc = getMethodDescriptor(VOID_TYPE, hasSimpleMetadataType);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		return insns;
	}

}
