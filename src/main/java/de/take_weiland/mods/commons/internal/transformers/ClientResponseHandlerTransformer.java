package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ClientResponseHandlerTransformer extends AbstractAnalyzingTransformer {

	private static final String clientHandler = "de/take_weiland/mods/commons/net/ClientResponseHandler";
	private static final String responseHandler = "de/take_weiland/mods/commons/net/PacketResponseHandler";
	private static final ClassInfo clientHandlerCI = ASMUtils.getClassInfo(clientHandler);
	private static final ClassInfo responseHandlerCI = ASMUtils.getClassInfo(responseHandler);
	private static final Type objectType = getType(Object.class);
	private static final Type entityPlayerType = getObjectType("net/minecraft/entity/player/EntityPlayer");

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isAbstract()
				|| classInfo.isInterface()
				|| responseHandlerCI.isAssignableFrom(classInfo)
				|| !clientHandlerCI.isAssignableFrom(classInfo)) {
			return false;
		}

		MethodNode method = new MethodNode(ACC_PUBLIC, "onResponse", getMethodDescriptor(VOID_TYPE, objectType, entityPlayerType), null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new VarInsnNode(ALOAD, 1));
		method.instructions.add(new MethodInsnNode(INVOKEINTERFACE, clientHandler, "onResponse", getMethodDescriptor(VOID_TYPE, objectType)));
		method.instructions.add(new InsnNode(RETURN));

		clazz.methods.add(method);

		clazz.interfaces.add(responseHandler);
		return true;
	}
}
