package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.sync.FieldProperty;
import de.take_weiland.mods.commons.internal.sync.SyncASMHooks;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static de.take_weiland.mods.commons.asm.CodePieces.constant;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.LONG_TYPE;
import static org.objectweb.asm.Type.getObjectType;

/**
 * @author diesieben07
 */
final class HandlerSyncer extends PropertyHandler {

	ASMVariable watcher;
	ASMVariable property;

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
		FieldNode propertyField = new FieldNode(ACC_PRIVATE | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(propertyField);
		property = ASMVariables.of(state.clazz, propertyField, CodePieces.getThis());

		ASMUtils.initialize(state.clazz, property.set(newProperty()));

		state.firstCstrInit.add(watcher.set(newWatcher()));
	}

	@Override
	CodePiece read(CodePiece stream) {
		return CodePieces.invokeInterface(Watcher.class, "read", watcher.get(),
				void.class,
				MCDataInput.class, stream,
				SyncableProperty.class, property.get());
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.isTrue(CodePieces.invokeInterface(Watcher.class, "hasChanged", watcher.get(),
				boolean.class,
				SyncableProperty.class, property.get()));
	}

	@Override
	CodePiece write(CodePiece stream) {
		return CodePieces.invokeInterface(Watcher.class, "write", watcher.get(), void.class,
				MCDataOutput.class, stream,
				SyncableProperty.class, property.get());
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		return CodePieces.invokeInterface(Watcher.class, "writeAndUpdate", watcher.get(), void.class,
				MCDataOutput.class, stream,
				SyncableProperty.class, property.get());
	}

	private CodePiece newWatcher() {
		CodePiece me = CodePieces.constant(Type.getObjectType(state.clazz.name));
		CodePiece watcher;
		if (var.isField()) {
			CodePiece field = CodePieces.invokeVirtual(Class.class, "getDeclaredField", me, Field.class,
					String.class, var.rawName());
			watcher = CodePieces.invokeStatic(SyncASMHooks.class, "findWatcher", Watcher.class,
					Field.class, field);
		} else {
			CodePiece getter = CodePieces.invokeVirtual(Class.class, "getDeclaredMethod", me, Method.class,
					String.class, var.rawName(),
					Class[].class, new Class[0]);
			watcher = CodePieces.invokeStatic(SyncASMHooks.class, "findWatcher", Watcher.class,
					Method.class, getter);
		}
		return watcher;
	}

	private CodePiece newProperty() {
		if (var.isField()) {
			return newPropertyForField();
		} else {
			return newPropertyForMethod();
		}
	}

	private CodePiece newPropertyForField() {
		String name = "_sc$sync$off$" + SyncTransformer.uniqueSuffix(var);
		FieldNode fieldOffset = new FieldNode(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, name, LONG_TYPE.getDescriptor(), null, null);
		state.clazz.fields.add(fieldOffset);

		CodePiece field = CodePieces.invokeVirtual(Class.class, "getDeclaredField", constant(getObjectType(state.clazz.name)),
				Field.class,
				String.class, var.rawName());

		CodePiece unsafe = CodePieces.invokeStatic(JavaUtils.class, "getUnsafe", Object.class);
		unsafe = CodePieces.castTo("sun/misc/Unsafe", unsafe);

		CodePiece computeOffset = CodePieces.invokeVirtual("sun/misc/Unsafe", "objectFieldOffset", unsafe, long.class,
				Field.class, field);

		CodePiece setOffset = CodePieces.setField(state.clazz, fieldOffset, computeOffset);
		ASMUtils.initializeStatic(state.clazz, setOffset);

		CodePiece offset = CodePieces.getField(state.clazz, fieldOffset);
		return CodePieces.instantiate(FieldProperty.class,
				long.class, offset,
				Object.class, CodePieces.getThis());
	}

	private CodePiece newPropertyForMethod() {
		String setter = var.isWritable() ? var.setterName() : null;
		Class<?> propertyClass = SyncASMHooks.makePropertyClass(state.clazz, var.rawName(), setter, var.getType());
		return CodePieces.instantiate(propertyClass,
				Object.class, CodePieces.getThis());
	}
}
