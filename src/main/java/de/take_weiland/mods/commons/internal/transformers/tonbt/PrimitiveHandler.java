package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagShort;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

/**
 * @author diesieben07
 */
final class PrimitiveHandler extends ToNBTHandler {

	private final Type primitive;

	PrimitiveHandler(ASMVariable var, Type primitive) {
		super(var);
		this.primitive = primitive;
	}

	@Override
	CodePiece makeNBT() {
		switch (primitive.getSort()) {
			case Type.BOOLEAN:
				return CodePieces.instantiate(NBTTagByte.class,
						String.class, CodePieces.constant(""),
						byte.class, ASMCondition.isTrue(var.get()).doIfElse(CodePieces.constant(1), CodePieces.constant(0)));

			case Type.CHAR:
				return CodePieces.instantiate(NBTTagShort.class,
						String.class, CodePieces.constant(""),
						short.class, CodePieces.castPrimitive(var.get(), Type.CHAR_TYPE, Type.SHORT_TYPE));

			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.LONG:
			case Type.FLOAT:
			case Type.DOUBLE:
				String nbtClass = "net/minecraft/nbt/NBTTag" + StringUtils.capitalize(primitive.getClassName());
				return CodePieces.instantiate(nbtClass,
						String.class, CodePieces.constant(""),
						primitive, var.get());

			default:
				throw new AssertionError();
		}
	}
}
