package de.take_weiland.mods.commons.internal.transformers.nbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.NBTSerialization;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.io.IOException;
import java.util.UUID;

import static de.take_weiland.mods.commons.asm.ASMCondition.ifNotNull;
import static de.take_weiland.mods.commons.asm.ASMCondition.ifTrue;
import static de.take_weiland.mods.commons.asm.CodePieces.*;
import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
abstract class NBTPropHandler {

	static NBTPropHandler create(ASMVariable var) {
		Type type = var.getType();
		if (ASMUtils.isPrimitive(type)) {
			return new ForPrimitive(var);
		} else if (type.getInternalName().equals("java/lang/String")) {
			return new ForString(var);
		} else if (type.getInternalName().equals("java/util/UUID")) {
			return new ForUUID(var);
		}

		ClassInfo ci = ClassInfo.of(type);
		if (ci.isEnum()) {
			return new ForEnum(var);
		} else if (ClassInfo.of(NBTBase.class).isAssignableFrom(ci) && !ClassInfo.of(NBTTagEnd.class).isAssignableFrom(ci)) {
			return new ForNBT(var);
		} else {
			throw new UnsupportedOperationException("Don't know how to save clazz " + type.getInternalName() + " to NBT");
		}
	}

	final ASMVariable var;

	NBTPropHandler(ASMVariable var) {
		this.var = var;
	}

	final CodePiece toNbt() {
		if (canBeNull()) {
			CodePiece nonNullSer = toNbt0(var.get());

			String owner = getInternalName(NBTSerialization.class);
			String name = NBTSerialization.SERIALIZED_NULL;
			String desc = Type.getMethodDescriptor(getType(NBTBase.class));
			CodePiece nullSer = CodePieces.invokeStatic(owner, name, desc);

			return CodePieces.doIfElse(IFNULL, var.get(), nullSer, nonNullSer);
		} else {
			return toNbt0(var.get());
		}
	}

	final CodePiece readFromNbt(CodePiece nbt) {
		CodePiece readValue;
		if (canBeNull()) {
			String owner = getInternalName(NBTSerialization.class);
			String name = NBTSerialization.IS_SERIALIZED_NULL;
			String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(NBTBase.class));

			CodePiece isNull = CodePieces.invokeStatic(owner, name, desc, nbt);
			readValue = ifTrue(isNull).doIfElse(constantNull(), fromNbt(nbt));
		} else {
			readValue = fromNbt(nbt);
		}

		AnnotationNode ann = var.getterAnnotation(ToNbt.class);
		ToNbt.ValueMissingAction missAction = ASMUtils.getAnnotationProperty(ann, "onMissing", ToNbt.ValueMissingAction.USE_DEFAULT);
		CodePiece onMissing;
		switch (missAction) {
			case USE_DEFAULT:
				onMissing = var.set(defaultValueForType());
				break;
			case THROW:
				onMissing = CodePieces.doThrow(IOException.class, "Missing NBT value " + getKey());
				break;
			default:
				throw new AssertionError();
		}

		ASMCondition validTag;

		if (knowsTagType()) {
			validTag = ifNotNull(nbt).and(nbtIdMatches(nbt, requiredTagType()));
		} else {
			validTag = ifNotNull(nbt);
		}

		return validTag.doIfElse(var.set(readValue), onMissing);
	}

	private String nbtKey;
	final String getKey() {
		if (nbtKey == null) {
			AnnotationNode ann = var.getterAnnotation(ToNbt.class);
			nbtKey = ASMUtils.getAnnotationProperty(ann, "key", var.name());
		}
		return nbtKey;
	}

	final boolean knowsTagType() {
		return requiredTagType() != -1;
	}

	int requiredTagType() {
		return -1;
	}

	abstract CodePiece defaultValueForType();
	abstract boolean canBeNull();
	abstract CodePiece toNbt0(CodePiece value);
	abstract CodePiece fromNbt(CodePiece nbt);

	private static abstract class ForReference extends NBTPropHandler {

		ForReference(ASMVariable var) {
			super(var);
		}

		private Boolean canBeNull;

		@Override
		boolean canBeNull() {
			if (canBeNull == null) {
				if (var.getterAnnotation("org/jetbrains/annotations/NotNull") != null) {
					canBeNull = false;
				} else {
					AnnotationNode ann = var.getterAnnotation(ToNbt.class);
					canBeNull = ASMUtils.getAnnotationProperty(ann, "nullable", true);
				}
			}
			return canBeNull;
		}

		@Override
		CodePiece defaultValueForType() {
			return CodePieces.constantNull();
		}
	}

	private static final class ForString extends ForReference {

		ForString(ASMVariable var) {
			super(var);
		}

		@Override
		CodePiece toNbt0(CodePiece value) {
			return CodePieces.instantiate(NBTTagString.class,
					new Type[] { getType(String.class), getType(String.class) },
					CodePieces.constant(""), var.get());
		}

		@Override
		CodePiece fromNbt(CodePiece nbt) {
			String owner = getInternalName(NBTTagString.class);
			String name = MCPNames.field(F_NBT_STRING_DATA);
			String desc = Type.getDescriptor(String.class);

			CodePiece stringTag = CodePieces.castTo(NBTTagString.class, nbt);
			CodePiece stringVal = CodePieces.getField(owner, name, desc, stringTag);

			return nbtIdMatches(nbt, 8).doIfElse(stringVal, constantNull());
		}
	}

	private static final class ForPrimitive extends NBTPropHandler {

		ForPrimitive(ASMVariable var) {
			super(var);
		}

		@Override
		boolean canBeNull() {
			return false;
		}

		@Override
		CodePiece defaultValueForType() {
			switch (var.getType().getSort()) {
				case Type.BOOLEAN:
					return CodePieces.constant(false);
				case Type.BYTE:
				case Type.SHORT:
				case Type.INT:
				case Type.CHAR:
					return CodePieces.constant(0);
				case Type.LONG:
					return CodePieces.constant(0L);
				case Type.FLOAT:
					return CodePieces.constant(0F);
				case Type.DOUBLE:
					return CodePieces.constant(0D);
				default:
					throw new AssertionError();
			}
		}

		@Override
		int requiredTagType() {
			switch (var.getType().getSort()) {
				case Type.BOOLEAN:
				case Type.BYTE:
					return 1;
				case Type.SHORT:
				case Type.CHAR:
					return 2;
				case Type.INT:
					return 3;
				case Type.LONG:
					return 4;
				case Type.FLOAT:
					return 5;
				case Type.DOUBLE:
					return 6;
				default:
					throw new AssertionError();
			}
		}

		@Override
		CodePiece toNbt0(CodePiece value) {
			String owner;
			CodePiece transValue = value;
			Type targetType = var.getType();

			switch (var.getType().getSort()) {
				case Type.BOOLEAN:
					owner = getInternalName(NBTTagByte.class);
					transValue = CodePieces.doIfElse(value, constant(1), constant(0));
					targetType = Type.BYTE_TYPE;
					break;
				case Type.BYTE:
					owner = getInternalName(NBTTagByte.class);
					break;
				case Type.SHORT:
					owner = getInternalName(NBTTagShort.class);
					break;
				case Type.CHAR:
					owner = getInternalName(NBTTagShort.class);
					transValue = CodePieces.castPrimitive(value, Type.CHAR_TYPE, Type.SHORT_TYPE);
					targetType = Type.SHORT_TYPE;
					break;
				case Type.INT:
					owner = getInternalName(NBTTagInt.class);
					break;
				case Type.LONG:
					owner = getInternalName(NBTTagLong.class);
					break;
				case Type.FLOAT:
					owner = getInternalName(NBTTagFloat.class);
					break;
				case Type.DOUBLE:
					owner = getInternalName(NBTTagDouble.class);
					break;
				default:
					throw new AssertionError();
			}

			Type[] args = { getType(String.class), targetType };
			return CodePieces.instantiate(owner, args, constant(""), transValue);
		}

		@Override
		CodePiece fromNbt(CodePiece nbt) {
			switch (var.getType().getSort()) {
				case Type.BOOLEAN:
					return doIfElse(IFEQ, getByte(nbt), constant(false), constant(true));
				case Type.BYTE:
					return getByte(nbt);
				case Type.SHORT:
					return getShort(nbt);
				case Type.CHAR:
					return CodePieces.castPrimitive(getShort(nbt), Type.SHORT_TYPE, Type.CHAR_TYPE);
				case Type.INT:
					String owner = getInternalName(NBTTagInt.class);
					String name = MCPNames.field(F_NBT_INT_DATA);
					String desc = Type.INT_TYPE.getDescriptor();
					return CodePieces.getField(owner, name, desc, castTo(NBTTagInt.class, nbt));
				case Type.LONG:
					owner = getInternalName(NBTTagLong.class);
					name = MCPNames.field(F_NBT_LONG_DATA);
					desc = Type.LONG_TYPE.getDescriptor();
					return CodePieces.getField(owner, name, desc, castTo(NBTTagLong.class, nbt));
				case Type.FLOAT:
					owner = getInternalName(NBTTagFloat.class);
					name = MCPNames.field(F_NBT_FLOAT_DATA);
					desc = Type.FLOAT_TYPE.getDescriptor();
					return CodePieces.getField(owner, name, desc, castTo(NBTTagFloat.class, nbt));
				case Type.DOUBLE:
					owner = getInternalName(NBTTagDouble.class);
					name = MCPNames.field(F_NBT_DOUBLE_DATA);
					desc = Type.DOUBLE_TYPE.getDescriptor();
					return CodePieces.getField(owner, name, desc, castTo(NBTTagDouble.class, nbt));
				default:
					throw new AssertionError();
			}
		}

		private static CodePiece getByte(CodePiece nbt) {
			String owner = getInternalName(NBTTagByte.class);
			String name = MCPNames.field(F_NBT_BYTE_DATA);
			String desc = Type.BYTE_TYPE.getDescriptor();
			return CodePieces.getField(owner, name, desc, CodePieces.castTo(NBTTagByte.class, nbt));
		}

		private static CodePiece getShort(CodePiece nbt) {
			String owner = getInternalName(NBTTagShort.class);
			String name = MCPNames.field(F_NBT_SHORT_DATA);
			String desc = Type.SHORT_TYPE.getDescriptor();
			return CodePieces.getField(owner, name, desc, CodePieces.castTo(NBTTagShort.class, nbt));
		}
	}

	private static class ForUUID extends ForReference {

		ForUUID(ASMVariable var) {
			super(var);
		}

		@Override
		CodePiece toNbt0(CodePiece value) {
			String owner = getInternalName(NBTSerialization.class);
			String name = "writeUUID";
			String desc = getMethodDescriptor(getType(NBTBase.class), getType(UUID.class));

			return CodePieces.invokeStatic(owner, name, desc, value);
		}

		@Override
		CodePiece fromNbt(CodePiece nbt) {
			String owner = getInternalName(NBTSerialization.class);
			String name = "readUUID";
			String desc = getMethodDescriptor(getType(UUID.class), getType(NBTBase.class));

			return CodePieces.invokeStatic(owner, name, desc, nbt);
		}
	}

	private static class ForEnum extends ForReference {

		ForEnum(ASMVariable var) {
			super(var);
		}

		@Override
		int requiredTagType() {
			return NBT.TAG_STRING;
		}

		@Override
		CodePiece toNbt0(CodePiece value) {
			String owner = getInternalName(Enum.class);
			String name = "name";
			String desc = getMethodDescriptor(getType(String.class));
			CodePiece enumName = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, value);

			Type[] argTypes = {getType(String.class), getType(String.class)};
			return CodePieces.instantiate(NBTTagString.class, argTypes, constant(""), enumName);
		}

		@Override
		CodePiece fromNbt(CodePiece nbt) {
			String owner = getInternalName(NBTTagString.class);
			String name = MCPNames.field(F_NBT_STRING_DATA);
			String desc = getDescriptor(String.class);

			CodePiece enumName = CodePieces.getField(owner, name, desc, castTo(NBTTagString.class, nbt));

			owner = getInternalName(Enum.class);
			name = "valueOf";
			desc = getMethodDescriptor(getType(Enum.class), getType(Class.class), getType(String.class));

			CodePiece value = CodePieces.invokeStatic(owner, name, desc, constant(var.getType()), enumName);
			return castTo(var.getType(), value);
		}
	}

	private static class ForNBT extends ForReference {

		ForNBT(ASMVariable var) {
			super(var);
		}

		@Override
		int requiredTagType() {
			return tagTypeFromClass(var.getType().getInternalName());
		}

		@Override
		CodePiece toNbt0(CodePiece value) {
			return value;
		}

		@Override
		CodePiece fromNbt(CodePiece nbt) {
			if (var.getType().getInternalName().equals("net/minecraft/nbt/NBTBase")) {
				return nbt;
			} else {
				return castTo(var.getType(), nbt);
			}
		}
	}

	private static ASMCondition nbtIdMatches(CodePiece nbt, int id) {
		String owner = getInternalName(NBTBase.class);
		String name = MCPNames.method(M_NBT_GET_ID);
		String desc = Type.getMethodDescriptor(BYTE_TYPE);
		CodePiece nbtId = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, nbt);
		return ASMCondition.ifSame(nbtId, constant(id), Type.INT_TYPE);
	}

	private static int tagTypeFromClass(String clazz) {
		switch (ASMUtils.transformName(clazz)) {
			case "net/minecraft/nbt/NBTBase":
				return -1;
			case "net/minecraft/nbt/NBTTagByte":
				return NBT.TAG_BYTE;
			case "net/minecraft/nbt/NBTTagShort":
				return NBT.TAG_SHORT;
			case "net/minecraft/nbt/NBTTagInt":
				return NBT.TAG_INT;
			case "net/minecraft/nbt/NBTTagLong":
				return NBT.TAG_LONG;
			case "net/minecraft/nbt/NBTTagFloat":
				return NBT.TAG_FLOAT;
			case "net/minecraft/nbt/NBTTagDouble":
				return NBT.TAG_DOUBLE;
			case "net/minecraft/nbt/NBTTagByteArray":
				return NBT.TAG_BYTE_ARR;
			case "net/minecraft/nbt/NBTTagString":
				return NBT.TAG_STRING;
			case "net/minecraft/nbt/NBTTagList":
				return NBT.TAG_LIST;
			case "net/minecraft/nbt/NBTTagCompound":
				return NBT.TAG_COMPOUND;
			case "net/minecraft/nbt/NBTTagIntArray":
				return NBT.TAG_INT_ARR;
			default:
				throw new AssertionError();
		}
	}

}
