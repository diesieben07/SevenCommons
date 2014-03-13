package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import de.take_weiland.mods.commons.internal.MetadataItemProxy;
import de.take_weiland.mods.commons.metadata.Metadata;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ItemBlockTransformer extends AbstractASMTransformer {

	@Override
	public boolean transform(ClassNode clazz) {
		final Type object = getType(Object.class);
		final Type itemStack = getObjectType("net/minecraft/item/ItemStack");
		final Type metadata = getType(Metadata.class);

		String name = MetadataItemProxy.GETTER;
		String desc = getMethodDescriptor(metadata, object, itemStack);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new VarInsnNode(ALOAD, 2));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		name = "dispatchBlockGetMetadata";
		desc = getMethodDescriptor(metadata, getObjectType(clazz.name), object, itemStack);
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		insns.add(new InsnNode(ARETURN));

		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/MetadataItemProxy");
		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return internalName.equals("net/minecraft/item/ItemBlock");
	}
}
