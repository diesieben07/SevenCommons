package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class EnumSetHandler extends PropertyHandler {

	private ASMVariable companion;
	private ASMVariable enumType;

	EnumSetHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
		String name = "_sc$sync$esType$" + SyncTransformer.uniqueSuffix(var);
		String desc = Type.getDescriptor(Class.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(field);
		enumType = ASMVariables.of(state.clazz, field);

		CodePiece myClass = CodePieces.constant(Type.getObjectType(state.clazz.name));
		CodePiece genericType;
		if (var.isField()) {
			String owner = Type.getInternalName(Class.class);
			name = "getDeclaredField";
			desc = Type.getMethodDescriptor(getType(Field.class), getType(String.class));
			CodePiece theField = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, myClass, CodePieces.constant(var.rawName()));

			owner = Type.getInternalName(Field.class);
			name = "getGenericType";
			desc = Type.getMethodDescriptor(getType(java.lang.reflect.Type.class));
			genericType = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, theField);
		} else {
			String owner = Type.getInternalName(Class.class);
			name = "getDeclaredMethod";
			desc = Type.getMethodDescriptor(getType(Method.class), getType(String.class), getType(Class[].class));
			CodePiece theMethod = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc,
					myClass, CodePieces.constant(var.rawName()), CodePieces.constant(new Class[0]));

			owner = Type.getInternalName(Method.class);
			name = "getGenericReturnType";
			desc = Type.getMethodDescriptor(getType(java.lang.reflect.Type.class));
			genericType = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, theMethod);
		}

		String owner = ASMHooks.CLASS_NAME;
		name = ASMHooks.FIND_ENUM_SET_TYPE;
		desc = Type.getMethodDescriptor(getType(Class.class), getType(java.lang.reflect.Type.class));
		CodePiece initType = enumType.set(CodePieces.invokeStatic(owner, name, desc, genericType));
		ASMUtils.initializeStatic(state.clazz, initType);
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifEqual(var.get(), companion.get(), Type.getType(EnumSet.class)).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "writeEnumSet";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(EnumSet.class));
		CodePiece write = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());

		ASMCondition companionNull = ASMCondition.ifNull(companion.get());
		ASMCondition varNull = ASMCondition.ifNull(var.get());

		CodePiece setCompanionNull = companion.set(CodePieces.constantNull());

		owner = Type.getInternalName(EnumSet.class);
		name = "clone";
		desc = Type.getMethodDescriptor(getType(EnumSet.class));
		CodePiece setCompanionNew = companion.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, var.get()));

		owner = Type.getInternalName(EnumSet.class);
		name = "clear";
		desc = Type.getMethodDescriptor(VOID_TYPE);
		CodePiece clearComp = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, companion.get());

		owner = Type.getInternalName(EnumSet.class);
		name = "addAll";
		desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Collection.class));
		CodePiece addAll = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, companion.get(), var.get()).append(new InsnNode(POP));

		CodePiece copy = clearComp.append(addAll);

		CodePiece updateNonNull = companionNull.doIfElse(setCompanionNew, copy);

		CodePiece update = varNull.doIfElse(setCompanionNull, updateNonNull);
		return write.append(update);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readEnumSet";
		String desc = Type.getMethodDescriptor(getType(EnumSet.class), getType(Class.class));
		return var.set(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, enumType.get()));
	}
}
