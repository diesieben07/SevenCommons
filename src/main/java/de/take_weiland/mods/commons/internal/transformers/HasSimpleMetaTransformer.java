package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.MetadataBlockProxy;
import de.take_weiland.mods.commons.internal.MetadataItemProxy;
import de.take_weiland.mods.commons.metadata.HasMetadata;
import de.take_weiland.mods.commons.metadata.Metadata;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class HasSimpleMetaTransformer extends AbstractAnalyzingTransformer {

	private static final ClassInfo hasSimpleMetadataCI = getClassInfo(HasMetadata.Simple.class);
	private static final ClassInfo blockCI = getClassInfo("net/minecraft/block/Block");
	private static final ClassInfo itemCI = getClassInfo("net/minecraft/item/Item");
	private static final Type worldType = getObjectType("net/minecraft/world/World");
	private static final Type itemStackType = getObjectType("net/minecraft/item/ItemStack");
	private static final Type hasSimpleMetadataType = getType(HasMetadata.Simple.class);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (!hasSimpleMetadataCI.isAssignableFrom(classInfo)) {
			return false;
		}

		boolean isBlock = blockCI.isAssignableFrom(classInfo);
		boolean isItem = !isBlock && itemCI.isAssignableFrom(classInfo);

		if (!isBlock && !isItem) {
			throw new IllegalArgumentException(String.format("Can only implement HasMetadata on Block or Item! Class: %s", clazz.name));
		}

		if (blockCI.isAssignableFrom(classInfo)) {
			transformBlock(clazz);
		} else if (itemCI.isAssignableFrom(classInfo)) {
			transformItem(clazz);
		}

		addItemStackMethod(clazz);

		for (MethodNode method : ASMUtils.getRootConstructors(clazz)) {
			method.instructions.insert(ASMUtils.findFirst(method, INVOKESPECIAL), buildTypeInjector(clazz));
		}

		return true;
	}

	private static void transformBlock(ClassNode clazz) {
		String name = MetadataBlockProxy.GETTER;
		String desc = getMethodDescriptor(getType(Metadata.class), worldType, INT_TYPE, INT_TYPE, INT_TYPE);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new VarInsnNode(ILOAD, 2));
		insns.add(new VarInsnNode(ILOAD, 3));
		insns.add(new VarInsnNode(ILOAD, 4));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		name = "getSimpleMetadata";
		desc = getMethodDescriptor(getType(Metadata.class), hasSimpleMetadataType, worldType, INT_TYPE, INT_TYPE, INT_TYPE);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/MetadataBlockProxy");
	}

	private static void addItemStackMethod(ClassNode clazz) {
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

	private static void transformItem(ClassNode clazz) {
		clazz.interfaces.add("de/take_weiland/mods/commons/internal/MetadataItemProxy");
	}

	private static InsnList buildTypeInjector(ClassNode clazz) {
		InsnList insns = new InsnList();
		insns.add(new VarInsnNode(ALOAD, 0));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		String name = "injectMetadataHolder";
		String desc = getMethodDescriptor(VOID_TYPE, hasSimpleMetadataType);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		return insns;
	}

}
