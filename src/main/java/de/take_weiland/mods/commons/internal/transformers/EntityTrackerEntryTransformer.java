package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.MCPNames.M_SEND_PACKET_TO_PLAYER;
import static de.take_weiland.mods.commons.asm.MCPNames.M_TRY_START_WATCHING_THIS;
import static de.take_weiland.mods.commons.internal.transformers.TransformerUtil.requireNext;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public class EntityTrackerEntryTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_TRY_START_WATCHING_THIS);

		method.instructions.insertBefore(findInsertionHook(method), generateEventCall(clazz));

		return true;
	}

	private InsnList generateEventCall(ClassNode clazz) {
		Type entityPlayer = getObjectType("net/minecraft/entity/player/EntityPlayer");
		Type entity = getObjectType("net/minecraft/entity/Entity");

		String myEntity = MCPNames.field(MCPNames.F_MY_ENTITY);

		String methodDesc = getMethodDescriptor(VOID_TYPE, entityPlayer, entity);

		InsnList hook = new InsnList();
		hook.add(new VarInsnNode(ALOAD, 1));
		hook.add(new VarInsnNode(ALOAD, 0));
		hook.add(new FieldInsnNode(GETFIELD, clazz.name, myEntity, entity.getDescriptor()));
		hook.add(new MethodInsnNode(INVOKESTATIC, ASMHooks.CLASS_NAME, ASMHooks.ON_START_TRACKING, methodDesc));

		return hook;
	}

	private AbstractInsnNode findInsertionHook(MethodNode method) {
		return new CodeSearcher()
				.add(new CodeSearcher.Stage() {
					@Override
					public AbstractInsnNode findNext(AbstractInsnNode current) {
						while (true) {
							while (current.getOpcode() != INVOKESPECIAL) {
								current = TransformerUtil.requireNext(current);
							}
							MethodInsnNode min = (MethodInsnNode) current;
							if (min.owner.equals("net/minecraft/network/packet/Packet41EntityEffect") && min.name.equals("<init>")) {
								return min;
							}
							current = requireNext(current);
						}
					}
				})
				.add(new CodeSearcher.Stage() {
					@Override
					public AbstractInsnNode findNext(AbstractInsnNode current) {
						final String sendPacket = MCPNames.method(M_SEND_PACKET_TO_PLAYER);

						while (true) {
							while (current.getOpcode() != INVOKEVIRTUAL) {
								current = requireNext(current);
							}
							MethodInsnNode min = (MethodInsnNode) current;
							if (min.owner.equals("net/minecraft/network/NetServerHandler") && min.name.equals(sendPacket)) {
								return min;
							}
							current = requireNext(current);
						}
					}
				})
				.add(new CodeSearcher.Stage() {
					@Override
					public AbstractInsnNode findNext(AbstractInsnNode current) {
						for (int i = 0; i < 2; ++i) {
							do {
								current = requireNext(current);
							} while (current.getOpcode() != GOTO);
						}
						return current;
					}
				})
				.find(method.instructions);
	}

	@Override
	public boolean transforms(String className) {
		return "net/minecraft/entity/EntityTrackerEntry".equals(className);
	}

}
