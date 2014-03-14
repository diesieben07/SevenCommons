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
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public abstract class AbstractMetadataTransformer extends AbstractAnalyzingTransformer {

	static final ClassInfo hasExtendedMetaCI = getClassInfo(HasMetadata.Extended.class);
	static final Type worldType = getObjectType("net/minecraft/world/World");
	static final ClassInfo blockCI = getClassInfo("net/minecraft/block/Block");
	static final ClassInfo itemCI = getClassInfo("net/minecraft/item/Item");
	static final ClassInfo hasSimpleMetadataCI = getClassInfo(HasMetadata.Simple.class);
	static final Type itemStackType = getObjectType("net/minecraft/item/ItemStack");
	static final Type hasExtendedMetaType = getType(HasMetadata.Extended.class);
	static final Type hasSimpleMetadataType = getType(HasMetadata.Simple.class);

	@Override
	public final boolean transform(ClassNode clazz, ClassInfo classInfo) {
		boolean extended = hasExtendedMetaCI.isAssignableFrom(classInfo);
		boolean simple = !extended && hasSimpleMetadataCI.isAssignableFrom(classInfo);
		if (!extended && !simple) {
			return false;
		}

		boolean isBlock = blockCI.isAssignableFrom(classInfo);
		boolean isItem = !isBlock && itemCI.isAssignableFrom(classInfo);

		if (!isBlock && !isItem) {
			throw new IllegalArgumentException(String.format("Can only implement HasMetadata on Block or Item! Class: %s", clazz.name));
		}

		if (blockCI.isAssignableFrom(classInfo)) {
			transformBlock(clazz, extended);
			clazz.interfaces.add("de/take_weiland/mods/commons/internal/MetadataBlockProxy");
		} else if (itemCI.isAssignableFrom(classInfo)) {
			clazz.interfaces.add("de/take_weiland/mods/commons/internal/MetadataItemProxy");
		}

		transformCommon(clazz, extended);
		return true;
	}

	final MethodNode getBlockGetter() {
		String name = MetadataBlockProxy.GETTER;
		String desc = getMethodDescriptor(getType(Metadata.class), worldType, INT_TYPE, INT_TYPE, INT_TYPE);
		return new MethodNode(ACC_PUBLIC, name, desc, null, null);
	}

	abstract boolean matches(ClassInfo classInfo);

	void transformBlock(ClassNode clazz, boolean extended) {
		MethodNode method = getBlockGetter();
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new VarInsnNode(ILOAD, 2));
		insns.add(new VarInsnNode(ILOAD, 3));
		insns.add(new VarInsnNode(ILOAD, 4));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		String name = extended ? "getExtendedMetadata" : "getSimpleMetadata";
		String desc = getMethodDescriptor(getType(Metadata.class), extended ? hasExtendedMetaType : hasSimpleMetadataType, worldType, INT_TYPE, INT_TYPE, INT_TYPE);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method);
	}

	void transformCommon(ClassNode clazz, boolean extended) {
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
