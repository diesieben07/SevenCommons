package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static com.google.common.base.Preconditions.checkState;
import static de.take_weiland.mods.commons.asm.MCPNames.M_SEND_PACKET_TO_PLAYER;
import static de.take_weiland.mods.commons.asm.MCPNames.M_TRY_START_WATCHING_THIS;
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
		AbstractInsnNode currentLocation = method.instructions.getFirst();
		State state = State.START;

		while (state.hasNext()) {
			state = state.next();
			currentLocation = state.findNext(currentLocation);
		}

		return currentLocation;
	}

	enum State {

		START,

		FIND_CSTR {
			@Override
			AbstractInsnNode findNext(AbstractInsnNode location) {
				while (true) {
					while (location.getOpcode() != INVOKESPECIAL) {
						location = requireNext(location);
					}
					MethodInsnNode min = (MethodInsnNode) location;
					if (min.owner.equals("net/minecraft/network/packet/Packet41EntityEffect") && min.name.equals("<init>")) {
						return min;
					}
					location = requireNext(location);
				}

			}
		},
		FIND_SEND_PACKET {
			@Override
			AbstractInsnNode findNext(AbstractInsnNode location) {
				final String sendPacket = MCPNames.method(M_SEND_PACKET_TO_PLAYER);

				while (true) {
					while (location.getOpcode() != INVOKEVIRTUAL) {
						location = requireNext(location);
					}
					MethodInsnNode min = (MethodInsnNode) location;
					if (min.owner.equals("net/minecraft/network/NetServerHandler") && min.name.equals(sendPacket)) {
						return min;
					}
					location = requireNext(location);
				}
			}
		},
		FIND_GOTO {
			@Override
			AbstractInsnNode findNext(AbstractInsnNode location) {
				for (int i = 0; i < 2; ++i) {
					do {
						location = requireNext(location);
					} while (location.getOpcode() != GOTO);
				}
				return location;
			}
		};

		AbstractInsnNode findNext(AbstractInsnNode location) {
			throw new AssertionError();
		}

		boolean hasNext() {
			return this != FIND_GOTO;
		}

		State next() {
			return JavaUtils.byOrdinal(State.class, ordinal() + 1);
		}

	}

	static AbstractInsnNode requireNext(AbstractInsnNode node) {
		node = node.getNext();
		checkState(node != null, "Missing next node in EntityTrackerEntry bytecode");
		return node;
	}

	@Override
	public boolean transforms(String className) {
		return "net/minecraft/entity/EntityTrackerEntry".equals(className);
	}

}
