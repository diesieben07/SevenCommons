package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import org.objectweb.asm.Type;

/**
* @author diesieben07
*/
class EnumSyncer extends Syncer {

	private final Type type;

	EnumSyncer(Type type) {
		this.type = type;
	}

	@Override
	ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
		return Conditions.ifEqual(oldValue, newValue, type, false);
	}

	@Override
	CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
		return CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
				"writeEnum",
				ASMUtils.getMethodDescriptor(void.class, WritableDataBuf.class, Enum.class),
				packetBuilder, newValue);
	}

	@Override
	CodePiece read(CodePiece oldValue, CodePiece dataBuf) {
		return CodePieces.castTo(type, CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
				"readEnum",
				ASMUtils.getMethodDescriptor(Enum.class, DataBuf.class, Class.class),
				dataBuf, CodePieces.constant(type)));
	}
}
