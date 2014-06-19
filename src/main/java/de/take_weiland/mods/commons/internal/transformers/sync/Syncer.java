package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.asm.*;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.objectweb.asm.Type.getType;

/**
* @author diesieben07
*/
abstract class Syncer {

	static final Map<Type, Type> boxedTypes = ImmutableMap.<Type, Type>builder()
		.put(getType(Boolean.class), Type.BOOLEAN_TYPE)
		.put(getType(Byte.class), Type.BYTE_TYPE)
		.put(getType(Short.class), Type.SHORT_TYPE)
		.put(getType(Integer.class), Type.INT_TYPE)
		.put(getType(Long.class), Type.LONG_TYPE)
		.put(getType(Character.class), Type.CHAR_TYPE)
		.put(getType(Float.class), Type.FLOAT_TYPE)
		.put(getType(Double.class), Type.DOUBLE_TYPE)
		.build();

	private static final Set<Type> integratedTypes = ImmutableSet.of(
			Type.getType(String.class), Type.getType(UUID.class)
	);

	static Syncer forType(Type t) {
		if (ASMUtils.isPrimitive(t) || integratedTypes.contains(t)) {
			return new IntegratedSyncer(t);
		} else if (boxedTypes.containsKey(t)) {
			return new BoxedSyncer(t);
		} else if (t.getInternalName().equals("net/minecraft/item/ItemStack")) {
			return ItemStackSyncer.instance();
		} else if (ClassInfo.of(t).isEnum()) {
			return new EnumSyncer(t);
		} else {
			throw new UnsupportedOperationException("NYI");
		}
	}

	abstract ASMCondition equals(CodePiece oldValue, CodePiece newValue);

	/**
	 * writes the value to the PacketBuilder
	 */
	abstract CodePiece write(CodePiece newValue, CodePiece packetBuilder);

	ASMCondition isNull(CodePiece value) {
		return Conditions.ifNull(value);
	}

	CodePiece wrapIndex(CodePiece value, int index) {
		return Conditions.ifNull(value)
				.then(CodePieces.constant(-index))
				.otherwise(CodePieces.constant(index))
				.build();
	}

	abstract CodePiece read(CodePiece oldValue, CodePiece packetBuilder);

}
