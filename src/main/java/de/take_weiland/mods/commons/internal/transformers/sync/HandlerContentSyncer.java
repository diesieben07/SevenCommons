package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.SyncContents;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.lang.annotation.Annotation;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
* @author diesieben07
*/
class HandlerContentSyncer extends PropertyHandler {

	private ASMVariable watcher;

	HandlerContentSyncer(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		String name = "_sc$sync$watcher$" + SyncTransformer.uniqueSuffix(var);
		String desc = Type.getDescriptor(ContentSyncer.class);
		FieldNode watcherField = new FieldNode(ACC_PRIVATE  | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(watcherField);
		watcher = ASMVariables.of(state.clazz, watcherField, CodePieces.getThis());

		CodePiece newWatcher = CodePieces.invokeDynamic(SyncingManager.CREATE_CONTAINER_SYNCER, Type.getMethodDescriptor(getType(ContentSyncer.class)))
				.withBootstrap(SyncingManager.CLASS_NAME, SyncingManager.BOOTSTRAP,
						var.getType(), var.rawName(), var.isMethod() ? SyncingManager.METHOD : SyncingManager.FIELD);

		ASMUtils.initialize(state.clazz, watcher.set(newWatcher));
	}

	@Override
	ASMCondition hasChanged() {
		String owner = Type.getInternalName(ContentSyncer.class);
		String name = "hasChanged";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Object.class));
		return ASMCondition.ifTrue(CodePieces.invokeInterface(owner, name, desc, watcher.get(), var.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(ContentSyncer.class);
		String name = "writeAndUpdate";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(MCDataOutputStream.class));
		return CodePieces.invokeInterface(owner, name, desc, watcher.get(), var.get(), stream);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(ContentSyncer.class);
		String name = "read";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(MCDataInputStream.class));
		return CodePieces.invokeInterface(owner, name, desc, watcher.get(), var.get(), stream);
	}

	@Override
	Class<? extends Annotation> getAnnotation() {
		return SyncContents.class;
	}
}
