package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.net.PacketTarget;
import de.take_weiland.mods.commons.net.SimplePacket;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
class CustomPacketTarget extends ASMPacketTarget {

	private final Type ptType;
	private final CodePiece ptInstance;

	CustomPacketTarget(Type ptType, CodePiece ptInstance) {
		this.ptType = ptType;
		this.ptInstance = ptInstance;
	}

	@Override
	CodePiece sendPacket(SyncType type, CodePiece packet) {
		String desc = getMethodDescriptor(getType(SimplePacket.class), getType(PacketTarget.class));
		return CodePieces.invoke(INVOKEINTERFACE, getInternalName(SimplePacket.class), "sendTo", desc,
				packet, ptInstance).append(new InsnNode(POP));
	}

	@Override
	String methodPostfix() {
		return ptType.getInternalName().replace('/', '_');
	}
}
