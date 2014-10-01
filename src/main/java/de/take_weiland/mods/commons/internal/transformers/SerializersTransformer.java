package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.SerializerUtil;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.Serializers;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
public class SerializersTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMethod(clazz, Serializers.DESERIALIZE);
		InsnList insns = method.instructions;
		insns.clear();

		String name = SerializerUtil.BYTESTREAM;
		String desc = Type.getMethodDescriptor(getType(ByteStreamSerializable.class), getType(Class.class), getType(MCDataInputStream.class));
		CodePieces.invokeDynamic(name, desc, CodePieces.of(new VarInsnNode(ALOAD, 0)), CodePieces.of(new VarInsnNode(ALOAD, 1)))
				.withBootstrap(SerializerUtil.CLASS_NAME, SerializerUtil.BOOTSTRAP)
				.appendTo(insns);
		insns.add(new InsnNode(ARETURN));

		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return Serializers.CLASS_NAME.equals(internalName);
	}
}
