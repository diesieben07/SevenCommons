package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.ResponseHandlerProxy;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class PacketResponseHandlerTransformer extends AbstractAnalyzingTransformer {

	static final String responseHandler = "de/take_weiland/mods/commons/net/PacketResponseHandler";
	static final ClassInfo responseHandlerCI = ASMUtils.getClassInfo(responseHandler);
	static final Type entityPlayerType = getObjectType("net/minecraft/entity/player/EntityPlayer");
	static final Type objectType = getType(Object.class);
	static final String clientHandler = "de/take_weiland/mods/commons/net/ClientResponseHandler";
	static final ClassInfo clientHandlerCI = ASMUtils.getClassInfo(clientHandler);

	private static final Type atomicInteger = getType(AtomicInteger.class);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isAbstract() || classInfo.isInterface()) {
			return false;
		}
		boolean isResponseHandler = responseHandlerCI.isAssignableFrom(classInfo);
		boolean isClientResponseHandler = !isResponseHandler && clientHandlerCI.isAssignableFrom(classInfo);
		if (!isResponseHandler && !isClientResponseHandler) {
			return false;
		}

		FieldNode field = new FieldNode(ACC_PRIVATE, "_sc$responseCounter", atomicInteger.getDescriptor(), null, null);
		clazz.fields.add(field);

		String desc = getMethodDescriptor(BOOLEAN_TYPE, objectType, entityPlayerType);
		MethodNode method = new MethodNode(ACC_PUBLIC, ResponseHandlerProxy.HANDLE, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		if (!isClientResponseHandler) {
			insns.add(new VarInsnNode(ALOAD, 2));
		}

		desc = isClientResponseHandler ? getMethodDescriptor(VOID_TYPE, objectType) : getMethodDescriptor(VOID_TYPE, objectType, entityPlayerType);
		insns.add(new MethodInsnNode(INVOKEVIRTUAL, clazz.name, "response", desc));

		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
		String name = "decrementAndGet";
		desc = getMethodDescriptor(INT_TYPE);
		insns.add(new MethodInsnNode(INVOKEVIRTUAL, atomicInteger.getInternalName(), name, desc));
		LabelNode isNull = new LabelNode();
		insns.add(new JumpInsnNode(IFEQ, isNull));
		insns.add(new InsnNode(ICONST_0));
		insns.add(new InsnNode(IRETURN));
		insns.add(isNull);
		insns.add(new InsnNode(ICONST_1));
		insns.add(new InsnNode(IRETURN));

		clazz.methods.add(method);

		for (MethodNode constructor : ASMUtils.getRootConstructors(clazz)) {
			insns = new InsnList();
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new TypeInsnNode(NEW, atomicInteger.getInternalName()));
			insns.add(new InsnNode(DUP));
			insns.add(new MethodInsnNode(INVOKESPECIAL, atomicInteger.getInternalName(), "<init>", getMethodDescriptor(VOID_TYPE)));
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
			constructor.instructions.insert(ASMUtils.findFirst(constructor, INVOKESPECIAL));
		}

		method = new MethodNode(ACC_PUBLIC, ResponseHandlerProxy.ADD_COUNT, getMethodDescriptor(VOID_TYPE, INT_TYPE), null, null);
		insns = method.instructions;
		insns.add(new VarInsnNode(ILOAD, 1));
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
		insns.add(new MethodInsnNode(INVOKEVIRTUAL, atomicInteger.getInternalName(), "getAndAdd", getMethodDescriptor(INT_TYPE, INT_TYPE)));
		insns.add(new InsnNode(POP));
		insns.add(new InsnNode(RETURN));

		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/ResponseHandlerProxy");
		return true;
	}

}
