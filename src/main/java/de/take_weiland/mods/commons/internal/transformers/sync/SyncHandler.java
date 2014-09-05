package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.sync.SyncableProxyInternal;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncableProxy;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
abstract class SyncHandler {

	private static final ClassInfo syncableProxyCI = ClassInfo.of(SyncableProxy.class);
	private static final ClassInfo enumCI = ClassInfo.of(Enum.class);

	static SyncHandler create(SyncingTransformerImpl impl, int idx, ASMVariable var) {
		Type type = var.getType();
		ClassInfo ci;
		String internalName;
		if (ASMUtils.isPrimitive(type)) {
			return new ForPrimitive(impl, idx, var);
		} else if ((internalName = type.getInternalName()).equals("net/minecraft/item/ItemStack")) {
			return new ForItemStack(impl, idx, var);
		} else if (internalName.equals("net/minecraftforge/fluids/FluidStack")) {
			return new ForFluidStack(impl, idx, var);
		} else if (internalName.equals("java/lang/String")) {
			return new ForString(impl, idx, var);
		} else if (internalName.equals("java/util/BitSet")) {
			return new ForBitSet(impl, idx, var);
		} else if (internalName.equals("java/util/EnumSet")) {
			return new ForEnumSet(impl, idx, var);
		} else if (internalName.equals("java/util/UUID")) {
			return new ForUUID(impl, idx, var);
		} else if (unwrap(internalName) != null) {
			return new ForPrimitiveWrapper(impl, idx, var);
		}
		ci = ClassInfo.of(internalName);
		if (!internalName.equals("java/lang/Enum") && enumCI.isAssignableFrom(ci)) {
			return new ForEnum(impl, idx, var);
		} else if (syncableProxyCI.isAssignableFrom(ci)) {
			return new ForSyncable(impl, idx, var);
		}

		throw new UnsupportedOperationException(String.format("Don't know how to sync field %s in %s!", var.rawName(), impl.clazz.name));

	}

	final int index;
	final ASMVariable var;
	final SyncingTransformerImpl impl;

	SyncHandler(SyncingTransformerImpl impl, int index, ASMVariable var) {
		this.var = var;
		this.index = index;
		this.impl = impl;
	}

	void initialTransform() { }


	private Boolean canBeNull;
	boolean canBeNull() {
		if (canBeNull == null) {
			if (var.getterAnnotation("org/jetbrains/annotations/NotNull") != null) {
				canBeNull = false;
			} else {
				AnnotationNode ann = var.getterAnnotation(Sync.class);
				canBeNull = ASMUtils.getAnnotationProperty(ann, "nullable", true);
			}
		}
		return canBeNull;
	}

	abstract CodePiece doChangeCheck(CodePiece onDifference);

	abstract CodePiece writeDataAndUpdate(CodePiece stream);

	abstract CodePiece readData(CodePiece stream);

	private static abstract class WithCompanion extends SyncHandler {

		ASMVariable companion;

		WithCompanion(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		void initialTransform() {
			String name = impl.memberName(varNameUnique() + "$companion");
			String desc = companionType().getDescriptor();
			FieldNode field = new FieldNode(ACC_PRIVATE | ACC_TRANSIENT, name, desc, null, null);
			impl.clazz.fields.add(field);
			companion = ASMVariables.of(impl.clazz, field, CodePieces.getThis());
		}

		final String varNameUnique() {
			return var.isMethod() ? "m$" + var.name() : var.name();
		}

		Type companionType() {
			return var.getType();
		}

		@Override
		CodePiece writeDataAndUpdate(CodePiece stream) {
			return writeData(stream).append(updateCompanion());
		}

		abstract CodePiece writeData(CodePiece stream);

		CodePiece updateCompanion() {
			return companion.set(var.get());
		}
	}

	private static class ForPrimitive extends WithCompanion {

		ForPrimitive(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfSame(var.get(), companion.get(), onDifference, var.getType());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			return writePrimitive(stream, var.get(), var.getType());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			return var.set(readPrimitive(stream, var.getType()));
		}
	}

	private static class ForItemStack extends WithCompanion {

		ForItemStack(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			String owner = ASMHooks.CLASS_NAME;
			String name = ASMHooks.STACKS_EQUAL;
			Type itemStackType = Type.getObjectType("net/minecraft/item/ItemStack");
			String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, itemStackType, itemStackType);

			CodePiece stacksIdentical = CodePieces.invokeStatic(owner, name, desc, var.get(), companion.get());
			return CodePieces.doIfNot(stacksIdentical, onDifference);
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = "de/take_weiland/mods/commons/net/MCDataOutputStream";
			String name = "writeItemStack";
			String desc = Type.getMethodDescriptor(VOID_TYPE, getObjectType("net/minecraft/item/ItemStack"));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readItemStack";
			String desc = Type.getMethodDescriptor(getObjectType("net/minecraft/item/ItemStack"));
			return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream));
		}

		@Override
		CodePiece updateCompanion() {
			String owner = "de/take_weiland/mods/commons/util/ItemStacks";
			String name = "clone";
			Type itemStackType = Type.getObjectType("net/minecraft/item/ItemStack");
			String desc = Type.getMethodDescriptor(itemStackType, itemStackType);

			return companion.set(CodePieces.invokeStatic(owner, name, desc, var.get()));
		}
	}

	private static class ForPrimitiveWrapper extends WithCompanion {

		ForPrimitiveWrapper(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfNotEqual(var.get(), companion.get(), onDifference, var.getType(), canBeNull());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			Type primitive = unwrap(var.getType().getInternalName());
			if (canBeNull()) {
				String owner = Type.getInternalName(MCDataOutputStream.class);
				String name = "write" + StringUtils.capitalize(primitive.getClassName()) + "Box";
				String desc = Type.getMethodDescriptor(VOID_TYPE, var.getType());
				return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
			} else {
				return writePrimitive(stream, CodePieces.unbox(var.get(), primitive), primitive);
			}
		}

		@Override
		CodePiece readData(CodePiece stream) {
			Type primitive = unwrap(var.getType().getInternalName());
			if (canBeNull()) {
				String owner = Type.getInternalName(MCDataInputStream.class);
				String name = "read" + StringUtils.capitalize(primitive.getClassName()) + "Box";
				String desc = Type.getMethodDescriptor(var.getType());
				return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream));
			} else {
				return var.set(CodePieces.box(readPrimitive(stream, primitive), primitive));
			}
		}
	}

	private static class ForEnum extends WithCompanion {

		ForEnum(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfNotSame(var.get(), companion.get(), onDifference, var.getType());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataOutputStream.class);
			String name = "writeEnum";
			String desc = ASMUtils.getMethodDescriptor(void.class, Enum.class);
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readEnum";
			String desc = ASMUtils.getMethodDescriptor(Enum.class, Class.class);
			CodePiece value = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, CodePieces.constant(var.getType()));
			return var.set(CodePieces.castTo(var.getType(), value));
		}
	}

	private static class ForSyncable extends SyncHandler {

		ForSyncable(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			String owner = SyncableProxyInternal.CLASS_NAME;
			String name = SyncableProxyInternal.NEEDS_SYNCING;
			String desc = Type.getMethodDescriptor(BOOLEAN_TYPE);
			return CodePieces.doIf(CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, var.get()), onDifference);
		}

		@Override
		CodePiece writeDataAndUpdate(CodePiece stream) {
			String owner = SyncableProxyInternal.CLASS_NAME;
			String name = SyncableProxyInternal.WRITE;
			String desc = Type.getMethodDescriptor(VOID_TYPE, Type.getType(MCDataOutputStream.class));
			return CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, var.get(), stream);
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = SyncableProxyInternal.CLASS_NAME;
			String name = SyncableProxyInternal.READ;
			String desc = Type.getMethodDescriptor(VOID_TYPE, Type.getType(MCDataInputStream.class));
			return CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, var.get(), stream);
		}
	}

	private static class ForFluidStack extends WithCompanion {

		ForFluidStack(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			String owner = "de/take_weiland/mods/commons/util/Fluids";
			String name = "identical";
			String desc = ASMUtils.getMethodDescriptor(boolean.class, FluidStack.class, FluidStack.class);
			CodePiece equal = CodePieces.invokeStatic(owner, name, desc, var.get(), companion.get());
			return CodePieces.doIfNot(equal, onDifference);
		}

		@Override
		CodePiece updateCompanion() {
			String owner = "de/take_weiland/mods/commons/util/Fluids";
			String name = "clone";
			String desc = ASMUtils.getMethodDescriptor(FluidStack.class, FluidStack.class);
			return companion.set(CodePieces.invokeStatic(owner, name, desc, var.get()));
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataOutputStream.class);
			String name = "writeFluidStack";
			String desc = ASMUtils.getMethodDescriptor(void.class, FluidStack.class);
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readFluidStack";
			String desc = ASMUtils.getMethodDescriptor(FluidStack.class);
			return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream));
		}
	}

	private static class ForString extends WithCompanion {

		ForString(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfNotEqual(var.get(), companion.get(), onDifference, Type.getType(String.class), canBeNull());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataOutputStream.class);
			String name = "writeString";
			String desc = Type.getMethodDescriptor(VOID_TYPE, getType(String.class));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readString";
			String desc = Type.getMethodDescriptor(getType(String.class));
			return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream));
		}
	}

	private static class ForBitSet extends WithCompanion {

		ForBitSet(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfNotEqual(var.get(), companion.get(), onDifference, Type.getType(BitSet.class), canBeNull());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataOutputStream.class);
			String name = "writeBitSet";
			String desc = Type.getMethodDescriptor(VOID_TYPE, getType(BitSet.class));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readBitSet";
			String desc = Type.getMethodDescriptor(VOID_TYPE, getType(BitSet.class));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece updateCompanion() {
			String owner = Type.getInternalName(BitSet.class);
			String name;
			String desc;

			CodePiece newBitSet = CodePieces.instantiate(BitSet.class);

			name = "clear";
			desc = Type.getMethodDescriptor(VOID_TYPE);
			CodePiece clearComp = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, companion.get());

			name = "or";
			desc = Type.getMethodDescriptor(VOID_TYPE, getType(BitSet.class));
			CodePiece doOr = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, companion.get(), var.get());

			CodePiece copy = clearComp.append(doOr);

			CodePiece checkedCopy = CodePieces.doIfElse(IFNULL, companion.get(), companion.set(newBitSet).append(doOr), copy);


			if (canBeNull()) {
				CodePiece setNull = companion.set(CodePieces.constantNull());
				return CodePieces.doIfElse(IFNULL, var.get(), setNull, checkedCopy);
			} else {
				return checkedCopy;
			}
		}
	}

	private static class ForEnumSet extends WithCompanion {

		private ASMVariable enumSetType;

		ForEnumSet(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece updateCompanion() {
			String owner = getInternalName(EnumSet.class);
			String name = "noneOf";
			String desc = Type.getMethodDescriptor(getType(EnumSet.class), getType(Class.class));
			CodePiece setNew = companion.set(CodePieces.invokeStatic(owner, name, desc, enumSetType.get()));

			owner = getInternalName(EnumSet.class);
			name = "addAll";
			desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Collection.class));
			CodePiece addAll = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, companion.get(), var.get()).append(new InsnNode(POP));

			owner = getInternalName(EnumSet.class);
			name = "clear";
			desc = Type.getMethodDescriptor(VOID_TYPE);
			CodePiece clear = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, companion.get());

			CodePiece copyData = CodePieces.doIfElse(IFNULL, companion.get(), setNew.append(addAll), clear.append(addAll));

			if (canBeNull()) {
				CodePiece setNull = companion.set(CodePieces.constantNull());
				return CodePieces.doIfElse(IFNULL, var.get(), setNull, copyData);
			} else {
				return copyData;
			}
		}

		@Override
		void initialTransform() {
			super.initialTransform();
			String owner;
			String name;
			String desc;

			CodePiece myClass = CodePieces.constant(Type.getObjectType(impl.clazz.name));
			CodePiece getVarType;

			if (var.isField()) {
				owner = getInternalName(Class.class);
				name = "getDeclaredField";
				desc = Type.getMethodDescriptor(getType(Field.class), getType(String.class));
				CodePiece theField = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, myClass, CodePieces.constant(var.rawName()));

				owner = getInternalName(Field.class);
				name = "getGenericType";
				desc = Type.getMethodDescriptor(getType(java.lang.reflect.Type.class));
				getVarType = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, theField);
			} else {
				owner = getInternalName(Class.class);
				name = "getDeclaredMethod";
				desc = Type.getMethodDescriptor(getType(Method.class), getType(String.class), getType(Class[].class));
				CodePiece theMethod = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, myClass, CodePieces.constant(var.rawName()), CodePieces.constant(new Class[0]));

				owner = getInternalName(Method.class);
				name = "getGenericReturnType";
				desc = Type.getMethodDescriptor(getType(java.lang.reflect.Type.class));
				getVarType = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, theMethod);
			}

			owner = getInternalName(TypeToken.class);
			name = "of";
			desc = ASMUtils.getMethodDescriptor(TypeToken.class, java.lang.reflect.Type.class);
			CodePiece createToken = CodePieces.invokeStatic(owner, name, desc, getVarType);

			// above code initializes the TypeToken for the field/method
			// now we have to actually resolve the type

			name = impl.memberName(varNameUnique() + "$enumSetType");
			desc = Type.getDescriptor(Class.class);
			FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_TRANSIENT | ACC_FINAL, name, desc, null, null);
			impl.clazz.fields.add(field);
			enumSetType = ASMVariables.of(impl.clazz, field);

			owner = ASMHooks.CLASS_NAME;
			name = ASMHooks.ITERABLE_TYPE;
			desc = Type.getDescriptor(java.lang.reflect.Type.class);
			CodePiece iterableType = CodePieces.getField(owner, name, desc);

			owner = getInternalName(TypeToken.class);
			name = "resolveType";
			desc = getMethodDescriptor(getType(TypeToken.class), getType(java.lang.reflect.Type.class));
			CodePiece resolvedType = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, createToken, iterableType);

			owner = getInternalName(TypeToken.class);
			name = "getRawType";
			desc = getMethodDescriptor(getType(Class.class));
			CodePiece rawResolvedType = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, resolvedType);

			owner = getInternalName(Class.class);
			name = "isEnum";
			desc = getMethodDescriptor(BOOLEAN_TYPE);
			CodePiece isEnum = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, enumSetType.get());
			CodePiece enumCheck = CodePieces.doIfNot(isEnum, CodePieces.doThrow(IllegalStateException.class, "Cannot resolve Type of EnumSet " + var.rawName()));

			ASMUtils.initializeStatic(impl.clazz, enumSetType.set(rawResolvedType).append(enumCheck));
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfNotEqual(var.get(), companion.get(), onDifference, Type.getType(EnumSet.class), canBeNull());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataOutputStream.class);
			String name = "writeEnumSet";
			String desc = Type.getMethodDescriptor(VOID_TYPE, getType(EnumSet.class), getType(Class.class));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get(), enumSetType.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readEnumSet";
			String desc = Type.getMethodDescriptor(getType(EnumSet.class), getType(EnumSet.class), getType(Class.class));

			return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get(), enumSetType.get()));
		}

	}

	private static class ForUUID extends WithCompanion {

		ForUUID(SyncingTransformerImpl impl, int index, ASMVariable var) {
			super(impl, index, var);
		}

		@Override
		CodePiece doChangeCheck(CodePiece onDifference) {
			return CodePieces.doIfNotEqual(var.get(), companion.get(), onDifference, Type.getType(UUID.class), canBeNull());
		}

		@Override
		CodePiece writeData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataOutputStream.class);
			String name = "writeUUID";
			String desc = Type.getMethodDescriptor(VOID_TYPE, getType(UUID.class));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());
		}

		@Override
		CodePiece readData(CodePiece stream) {
			String owner = Type.getInternalName(MCDataInputStream.class);
			String name = "readUUID";
			String desc = Type.getMethodDescriptor(getType(UUID.class));
			return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream);
		}
	}

	static Type unwrap(String wrapperName) {
		switch (wrapperName) {
			case "java/lang/Boolean":
				return BOOLEAN_TYPE;
			case "java/lang/Byte":
				return BYTE_TYPE;
			case "java/lang/Short":
				return SHORT_TYPE;
			case "java/lang/Character":
				return CHAR_TYPE;
			case "java/lang/Integer":
				return INT_TYPE;
			case "java/lang/Long":
				return LONG_TYPE;
			case "java/lang/Float":
				return FLOAT_TYPE;
			case "java/lang/Double":
				return DOUBLE_TYPE;
			default:
				return null;
		}
	}

	static CodePiece writePrimitive(CodePiece stream, CodePiece value, Type type) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "write" + StringUtils.capitalize(type.getClassName());
		String desc;
		switch (type.getSort()) {
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.CHAR:
				desc = Type.getMethodDescriptor(VOID_TYPE, INT_TYPE);
				break;
			case Type.BOOLEAN:
			case Type.LONG:
			case Type.FLOAT:
			case Type.DOUBLE:
				desc = Type.getMethodDescriptor(VOID_TYPE, type);
				break;
			default:
				throw new AssertionError();
		}

		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, value);
	}

	static CodePiece readPrimitive(CodePiece stream, Type type) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "read" + StringUtils.capitalize(type.getClassName());
		String desc = Type.getMethodDescriptor(type);

		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream);
	}

}
