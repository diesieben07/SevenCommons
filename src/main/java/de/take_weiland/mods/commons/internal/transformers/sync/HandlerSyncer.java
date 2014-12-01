package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.sync.ctx.FieldContext;
import de.take_weiland.mods.commons.sync.ctx.MethodContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
abstract class HandlerSyncer extends PropertyHandler {

	ASMVariable syncer;
	ASMVariable syncerData;

	HandlerSyncer(ASMVariable var, int idx) {
		super(var, idx);
	}

	abstract CodePiece newSyncer(CodePiece context);
	abstract Class<?> syncerClass();

	@Override
	void initialTransform(TransformState state) {
		String suffix = SyncTransformer.uniqueSuffix(var);
		String name = "_sc$sync$syncer$" + suffix;
		String desc = Type.getDescriptor(syncerClass());
		FieldNode syncerField = new FieldNode(ACC_PRIVATE  | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(syncerField);
		syncer = ASMVariables.of(state.clazz, syncerField, CodePieces.getThis());

		name = "_sc$sync$data$" + suffix;
		desc = ASMUtils.OBJECT_TYPE.getDescriptor();
		FieldNode syncerDataField = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		state.clazz.fields.add(syncerDataField);
		syncerData = ASMVariables.of(state.clazz, syncerDataField, CodePieces.getThis());

		ASMUtils.initialize(state.clazz, syncer.set(newSyncer(getSyncContext(state.clazz, var))));
	}

	@Override
	ASMCondition hasChanged() {
		return ASMCondition.ifTrue(CodePieces.invokeInterface(Syncer.class, "hasChanged", syncer.get(),
				boolean.class,
				Object.class, var.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		CodePiece newData = CodePieces.invokeInterface(Syncer.class, "writeAndUpdate", syncer.get(), Object.class,
				Object.class, var.get(),
				MCDataOutputStream.class, stream,
				Object.class, syncerData.get());
		return syncerData.set(newData);
	}

	static CodePiece getSyncContext(ClassNode clazz, ASMVariable variable) {
		CodePiece clazzObj = CodePieces.constant(Type.getObjectType(clazz.name));
		CodePiece context;
		if (variable.isMethod()) {
			CodePiece method = CodePieces.invokeVirtual(Class.class, "getDeclaredMethod", getType(clazz.name),
					clazzObj, Method.class,
					String.class, variable.rawName(),
					Class[].class, new Class[0]);

			context = CodePieces.instantiate(MethodContext.class, Method.class, method);
		} else if (variable.isField()) {
			CodePiece field = CodePieces.invokeVirtual(Class.class, "getDeclaredField",
					clazzObj, Field.class,
					String.class, variable.rawName());

			context = CodePieces.instantiate(FieldContext.class, Field.class, field);
		} else {
			throw new AssertionError("@Sync variable neither Field nor method!?");
		}
		return context;
	}
}
