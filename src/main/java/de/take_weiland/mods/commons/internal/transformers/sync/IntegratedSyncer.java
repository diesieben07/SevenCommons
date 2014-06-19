package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.SyncASMHooks;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import org.objectweb.asm.Type;

/**
* @author diesieben07
*/
class IntegratedSyncer extends Syncer {

	final Type typeToSync;

	IntegratedSyncer(Type typeToSync) {
		this.typeToSync = typeToSync;
	}

	@Override
	ASMCondition isNull(CodePiece value) {
		return ASMUtils.isPrimitive(typeToSync) ? Conditions.alwaysFalse() : super.isNull(value);
	}

	@Override
	CodePiece wrapIndex(CodePiece value, int index) {
		return CodePieces.constant(index);
	}

	@Override
	CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
		String owner = SyncASMHooks.CLASS_NAME;
		String name = SyncASMHooks.WRITE_INTEGRATED;
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, typeToSync, Type.getType(WritableDataBuf.class));

		return CodePieces.invokeStatic(owner, name, desc, newValue, packetBuilder);
	}

	@Override
	ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
		return Conditions.ifEqual(oldValue, newValue, typeToSync, true);
	}

	@Override
	CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
		String owner = SyncASMHooks.CLASS_NAME;
		String name = String.format(SyncASMHooks.READ_INTEGRATED, typeToSync.getClassName().replace('.', '_'));
		String desc = Type.getMethodDescriptor(typeToSync, Type.getType(DataBuf.class));

		return CodePieces.invokeStatic(owner, name, desc, packetBuilder);
	}
}
