package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.PropertySyncer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class HandlerWithSyncer extends PropertyHandler {

	private ASMVariable syncer;

	HandlerWithSyncer(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		String name = "_sc$sync$syncer$" + SyncTransformer.uniqueSuffix(var);
		String desc = Type.getDescriptor(PropertySyncer.class);
		FieldNode watcherField = new FieldNode(ACC_PRIVATE  | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(watcherField);
		syncer = ASMVariables.of(state.clazz, watcherField, CodePieces.getThis());

		CodePiece newWatcher = CodePieces.invokeDynamic(SyncingManager.CREATE_SYNCER, Type.getMethodDescriptor(getType(PropertySyncer.class)))
				.withBootstrap(SyncingManager.CLASS_NAME, SyncingManager.BOOTSTRAP, var.getType());

		ASMUtils.initialize(state.clazz, syncer.set(newWatcher));
	}

	@Override
	ASMCondition hasChanged() {
		String owner = Type.getInternalName(PropertySyncer.class);
		String name = "hasChanged";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Object.class));
		return ASMCondition.ifTrue(CodePieces.invokeInterface(owner, name, desc, syncer.get(), var.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(PropertySyncer.class);
		String name = "writeAndUpdate";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(MCDataOutputStream.class));
		return CodePieces.invokeInterface(owner, name, desc, syncer.get(), var.get(), stream);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(PropertySyncer.class);
		String name = "read";
		String desc = Type.getMethodDescriptor(getType(Object.class), getType(MCDataInputStream.class));
		return var.set(CodePieces.invokeInterface(owner, name, desc, syncer.get(), stream));
	}
}
