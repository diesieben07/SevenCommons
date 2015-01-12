package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.sync.SyncASMHooks;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.PropertyMetadata;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
final class HandlerSyncer extends PropertyHandler {

	ASMVariable watcher;
	ASMVariable property;
	ASMVariable data;

	HandlerSyncer(ASMVariable var, int idx, TransformState state) {
		super(var, idx, state);
	}

	@Override
	void initialTransform() {
		String suffix = SyncTransformer.uniqueSuffix(var);
		String name = "_sc$sync$watcher$" + suffix;
		String desc = Type.getDescriptor(Watcher.class);
		FieldNode watcherField = new FieldNode(ACC_PRIVATE  | ACC_FINAL | ACC_STATIC, name, desc, null, null);
		state.clazz.fields.add(watcherField);
		watcher = ASMVariables.of(state.clazz, watcherField);

		name = "_sc$sync$prop$" + suffix;
		desc = Type.getType(SyncableProperty.class).getDescriptor();
		FieldNode propertyField = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(propertyField);
		property = ASMVariables.of(state.clazz, propertyField);

		name = "_sc$sync$data$" + suffix;
		desc = Type.getDescriptor(Object.class);
		FieldNode dataField = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		state.clazz.fields.add(dataField);
		data = ASMVariables.of(state.clazz, dataField, CodePieces.getThis());

		ASMUtils.initializeStatic(state.clazz, property.set(newProperty()));

		state.firstCstrInit.add(watcher.set(newWatcher()));

		ASMUtils.initialize(state.clazz, CodePieces.invokeInterface(Watcher.class, "setup", watcher.get(), void.class,
				SyncableProperty.class, property.get(),
				Object.class, CodePieces.getThis()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		return CodePieces.invokeInterface(Watcher.class, "read", watcher.get(),
				void.class,
				MCDataInput.class, stream,
				SyncableProperty.class, property.get(),
				Object.class, CodePieces.getThis());
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.isTrue(CodePieces.invokeInterface(Watcher.class, "hasChanged", watcher.get(),
				boolean.class,
				SyncableProperty.class, property.get(),
				Object.class, CodePieces.getThis()));
	}

	@Override
	CodePiece write(CodePiece stream) {
		return CodePieces.invokeInterface(Watcher.class, "initialWrite", watcher.get(), void.class,
				MCDataOutput.class, stream,
				SyncableProperty.class, property.get(),
				Object.class, CodePieces.getThis());
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		return CodePieces.invokeInterface(Watcher.class, "writeAndUpdate", watcher.get(), void.class,
				MCDataOutput.class, stream,
				SyncableProperty.class, property.get(),
				Object.class, CodePieces.getThis());
	}

	private CodePiece newWatcher() {
		return CodePieces.invokeStatic(SyncASMHooks.class, "findWatcher", Watcher.class,
				PropertyMetadata.class, property.get());
	}

	private CodePiece newProperty() {
		if (var.isField()) {
			return CodePieces.invokeStatic(SyncASMHooks.class, "makeProperty", SyncableProperty.class,
					Field.class, getReflectiveField(var),
					Field.class, getReflectiveField(data));
		} else {
			return CodePieces.invokeStatic(SyncASMHooks.class, "makeProperty", SyncableProperty.class,
					Method.class, getReflectiveGetter(),
					Method.class, getReflectiveSetter(),
					Field.class, getReflectiveField(data));
		}
	}

	private CodePiece getReflectiveField(ASMVariable var) {
		CodePiece myClass = CodePieces.constant(Type.getObjectType(state.clazz.name));
		return CodePieces.invokeVirtual(Class.class, "getDeclaredField", myClass, Field.class,
				String.class, var.rawName());
	}

	private CodePiece getReflectiveGetter() {
		CodePiece myClass = CodePieces.constant(Type.getObjectType(state.clazz.name));
		return CodePieces.invokeVirtual(Class.class, "getDeclaredMethod", myClass, Method.class,
				String.class, var.rawName(),
				Object[].class, CodePieces.constant(new Object[0]));
	}

	private CodePiece getReflectiveSetter() {
		if (!var.isWritable()) {
			return CodePieces.constantNull();
		}
		CodePiece myClass = CodePieces.constant(Type.getObjectType(state.clazz.name));
		return CodePieces.invokeStatic(Class.class, "getDeclaredMethod", myClass, Method.class,
				String.class, var.setterName(),
				Object[].class, CodePieces.constant(new Object[] { var.getType() }));
	}

}
