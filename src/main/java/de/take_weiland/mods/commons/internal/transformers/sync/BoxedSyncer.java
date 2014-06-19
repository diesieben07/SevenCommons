package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.Conditions;
import de.take_weiland.mods.commons.internal.SyncASMHooks;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.getMethodDescriptor;

/**
* @author diesieben07
*/
class BoxedSyncer extends Syncer {

	private final Type type;

	BoxedSyncer(Type type) {
		this.type = type;
	}

	@Override
	ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
		return Conditions.ifEqual(oldValue, newValue, type, true);
	}

	@Override
	CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
		Type unboxed = boxedTypes.get(type);
		String owner = SyncASMHooks.CLASS_NAME;
		String name = SyncASMHooks.WRITE_INTEGRATED;
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, unboxed, Type.getType(WritableDataBuf.class));

		String unboxName = unboxed.getClassName() + "Value";
		String unboxDesc = getMethodDescriptor(unboxed);

		return CodePieces.invokeStatic(owner, name, desc,
				CodePieces.invoke(INVOKEVIRTUAL, type.getInternalName(), unboxName, unboxDesc, newValue),
				packetBuilder);
	}

	@Override
	CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
		Type unboxed = boxedTypes.get(type);

		String owner = SyncASMHooks.CLASS_NAME;
		String name = String.format(SyncASMHooks.READ_INTEGRATED, unboxed.getClassName().replace('.', '_'));
		String desc = Type.getMethodDescriptor(unboxed, Type.getType(DataBuf.class));

		String boxDesc = getMethodDescriptor(type, unboxed);

		return CodePieces.invokeStatic(type.getInternalName(), "valueOf", boxDesc,
					CodePieces.invokeStatic(owner, name, desc, packetBuilder));
	}
}
