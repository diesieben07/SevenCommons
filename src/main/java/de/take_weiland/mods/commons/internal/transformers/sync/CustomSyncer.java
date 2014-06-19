package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;

/**
* @author diesieben07
*/
class CustomSyncer extends Syncer {

	private final CodePiece syncer;
	private final Type actualType;

	CustomSyncer(CodePiece syncer, Type actualType) {
		this.syncer = syncer;
		this.actualType = actualType;
	}

	@Override
	ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
		String owner = TypeSyncer.CLASS_NAME;
		String name = TypeSyncer.METHOD_EQUAL;
		Type objectType = Type.getType(Object.class);
		String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, objectType, objectType);

		CodePiece invoke = CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, syncer, newValue, oldValue);

		return Conditions.of(invoke, IFNE, IFEQ);
	}

	@Override
	CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
		String owner = TypeSyncer.CLASS_NAME;
		String name = TypeSyncer.METHOD_WRITE;
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(WritableDataBuf.class));

		return CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, syncer, newValue, packetBuilder);
	}

	@Override
	CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
		String owner = TypeSyncer.CLASS_NAME;
		String name = TypeSyncer.METHOD_READ;
		Type objectType = Type.getType(Object.class);
		String desc = Type.getMethodDescriptor(objectType, objectType, Type.getType(DataBuf.class));

		CodePiece invoke = CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, syncer, oldValue, packetBuilder);
		if (!ASMUtils.isPrimitive(actualType) || !actualType.equals(objectType)) {
			return CodePieces.castTo(actualType, invoke);
		} else {
			return invoke;
		}
	}
}
