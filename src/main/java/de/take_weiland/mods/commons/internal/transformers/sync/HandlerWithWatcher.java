package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.PropertyWatcher;
import de.take_weiland.mods.commons.sync.Watchers;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;

import java.lang.reflect.Constructor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
* @author diesieben07
*/
class HandlerWithWatcher extends PropertyHandler {

	private ASMVariable watcherCstr;
	private ASMVariable watcher;

	HandlerWithWatcher(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		createWatcherField(state.clazz);
		createWatcherCstrField(state.clazz);

		String owner = Type.getInternalName(Watchers.class);
		String name = "getWatcherConstructor";
		String desc = Type.getMethodDescriptor(getType(Constructor.class), getType(Class.class));
		CodePiece getCstr = CodePieces.invokeStatic(owner, name, desc, CodePieces.constant(var.getType()));
		CodePiece putCstr = watcherCstr.set(getCstr);

		state.firstConstructInit.add(putCstr);

		owner = Type.getInternalName(ArrayUtils.class);
		name = "EMPTY_OBJECT_ARRAY";
		desc = Type.getDescriptor(Object[].class);
		CodePiece emptyArray = CodePieces.getField(owner, name, desc);

		owner = Type.getInternalName(Constructor.class);
		name = "newInstance";
		desc = Type.getMethodDescriptor(getType(Object.class), getType(Object[].class));
		CodePiece newInstance = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, watcherCstr.get(), emptyArray);
		CodePiece casted = CodePieces.castTo(PropertyWatcher.class, newInstance);

		CodePiece putInstance = watcher.set(casted);

		state.constructorInit.add(putInstance);
	}

	private void createWatcherField(ClassNode clazz) {
		String name = "_sc$sync$watcher$" + SyncTransformer.uniqueSuffix(var);
		String desc = Type.getDescriptor(PropertyWatcher.class);
		FieldNode field = new FieldNode(ACC_PRIVATE  | ACC_FINAL, name, desc, null, null);
		clazz.fields.add(field);
		watcher = ASMVariables.of(clazz, field, CodePieces.getThis());
	}

	private void createWatcherCstrField(ClassNode clazz) {
		String name = "_sc$sync$watcherCstr$" + SyncTransformer.uniqueSuffix(var);
		String desc = Type.getDescriptor(Constructor.class);
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(field);
		watcherCstr = ASMVariables.of(clazz, field);
	}

	@Override
	ASMCondition hasChanged() {
		String owner = Type.getInternalName(PropertyWatcher.class);
		String name = "hasChanged";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Object.class));
		return ASMCondition.ifTrue(CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, watcher.get(), var.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(PropertyWatcher.class);
		String name = "writeAndUpdate";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(MCDataOutputStream.class));
		return CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, watcher.get(), var.get(), stream);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(PropertyWatcher.class);
		String name = "readBase";
		String desc = Type.getMethodDescriptor(getType(Object.class), getType(Object.class), getType(MCDataInputStream.class));
		CodePiece read = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, watcher.get(), var.get(), stream);
		if (var.isWritable()) {
			return var.set(CodePieces.castTo(var.getType(), read));
		} else {
			return read.append(new InsnNode(POP));
		}
	}
}
