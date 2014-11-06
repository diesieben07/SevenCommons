package de.take_weiland.mods.commons.internal.transformers;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.internal.PacketHandlerProxy;
import de.take_weiland.mods.commons.net.PacketDirection;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public final class ModPacketTransformer extends AbstractAnalyzingTransformer {

	private static final int DEFAULT_EXPECTED_SIZE = 32;
	public static final String MOD_PACKET_CLASS = "de/take_weiland/mods/commons/net/ModPacket";
	private static final ClassInfo modPacketCI = ClassInfo.of(MOD_PACKET_CLASS);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (clazz.name.equals(MOD_PACKET_CLASS) || classInfo.isInterface() || !modPacketCI.isAssignableFrom(classInfo)) {
			return false;
		}
		if (!hasDefaultConstructor(clazz)) {
			addDefaultConstructor(clazz);
		}

		// make the class publicly accessible
		clazz.access = (clazz.access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;

		if (!classInfo.isAbstract()) {
			String name = "_sc$packetHandler";
			String desc = Type.getDescriptor(PacketHandlerProxy.class);
			FieldNode handlerField = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_TRANSIENT, name, desc, null, null);
			clazz.fields.add(handlerField);

			createSetHandler(clazz, handlerField);
			createGetHandler(clazz, handlerField);
		}

		AnnotationNode validReceiver = ASMUtils.getAnnotation(clazz, PacketDirection.class);
		if (validReceiver != null || !classInfo.isAbstract()) {
			PacketDirection.Dir receiveSide;
			if (validReceiver == null) {
				receiveSide = PacketDirection.Dir.BOTH_WAYS;
			} else {
				receiveSide = ASMUtils.getAnnotationProperty(validReceiver, "value", PacketDirection.class);
			}

			createSideValidator(clazz, receiveSide);
		}

		clazz.interfaces.add(ModPacketProxy.CLASS_NAME);
		return true;
	}

	private static void createSideValidator(ClassNode clazz, PacketDirection.Dir dir) {
		String name = ModPacketProxy.CAN_SIDE_RECEIVE;
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Side.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		if (dir == PacketDirection.Dir.BOTH_WAYS) {
			insns.add(new InsnNode(ICONST_1));
			insns.add(new InsnNode(IRETURN));
		} else {
			Side valid = dir == PacketDirection.Dir.TO_CLIENT ? Side.CLIENT : Side.SERVER;
			insns.add(new VarInsnNode(ALOAD, 1));
			CodePieces.constant(valid).appendTo(insns);

			LabelNode eq = new LabelNode();
			insns.add(new JumpInsnNode(IF_ACMPEQ, eq));
			insns.add(new InsnNode(ICONST_0));
			insns.add(new InsnNode(IRETURN));
			insns.add(eq);
			insns.add(new InsnNode(ICONST_1));
			insns.add(new InsnNode(IRETURN));
		}
		clazz.methods.add(method);
	}

	private static void createSetHandler(ClassNode clazz, FieldNode field) {
		String name = ModPacketProxy.SET_HANDLER;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(PacketHandlerProxy.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new FieldInsnNode(PUTSTATIC, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(RETURN));
		clazz.methods.add(method);
	}

	private static void createGetHandler(ClassNode clazz, FieldNode field) {
		String name = ModPacketProxy.GET_HANDLER;
		String desc = Type.getMethodDescriptor(getType(PacketHandlerProxy.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(ARETURN));
		clazz.methods.add(method);
	}

	private static void addDefaultConstructor(ClassNode clazz) {
		String name = "<init>";
		String desc = getMethodDescriptor(VOID_TYPE);
		MethodNode cstr = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		cstr.instructions.add(new VarInsnNode(ALOAD, 0));
		cstr.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
		cstr.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(cstr);
	}

	private static boolean hasDefaultConstructor(ClassNode clazz) {
		return ASMUtils.findMethod(clazz, "<init>", getMethodDescriptor(VOID_TYPE)) != null;
	}
}
