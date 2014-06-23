package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.net.SimplePacket;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class DefaultPacketTarget extends ASMPacketTarget {

	private static DefaultPacketTarget instance;

	static ASMPacketTarget instance() {
		return instance == null ? (instance = new DefaultPacketTarget()) : instance;
	}

	@Override
	CodePiece sendPacket(SyncType type, CodePiece packet) {
		String desc = getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(SimplePacket.class));
		return CodePieces.invoke(INVOKEVIRTUAL, getInternalName(SyncType.class), "sendPacket", desc,
				CodePieces.constant(type), CodePieces.getThis(), packet);
	}

	@Override
	String methodPostfix() {
		return "_sc$default";
	}
}
