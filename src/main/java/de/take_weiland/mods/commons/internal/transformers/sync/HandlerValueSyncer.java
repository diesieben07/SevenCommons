package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.ctx.FieldContext;
import de.take_weiland.mods.commons.sync.ctx.MethodContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import scala.tools.nsc.doc.model.*;
import scala.tools.nsc.doc.model.Class;

import java.lang.Object;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class HandlerValueSyncer extends PropertyHandler {

	private ASMVariable syncer;

	HandlerValueSyncer(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		String name = "_sc$sync$syncer$" + SyncTransformer.uniqueSuffix(var);
		String desc = Type.getDescriptor(ValueSyncer.class);
		FieldNode syncerField = new FieldNode(ACC_PRIVATE  | ACC_FINAL, name, desc, null, null);
		state.clazz.fields.add(syncerField);
		syncer = ASMVariables.of(state.clazz, syncerField, CodePieces.getThis());

		CodePiece myClass = CodePieces.constant(Type.getObjectType(state.clazz.name));
		CodePiece fieldObj = CodePieces.invokeVirtual(getInternalName(Class.class), "getDeclaredMethod")
		CodePiece newWatcher = CodePieces.invokeDynamic(SyncingManager.CREATE_SYNCER, Type.getMethodDescriptor(getType(ValueSyncer.class)))
				.withBootstrap(SyncingManager.CLASS_NAME, SyncingManager.BOOTSTRAP,
						var.getType(), var.rawName(), var.isMethod() ? SyncingManager.METHOD : SyncingManager.FIELD);

		ASMUtils.initialize(state.clazz, syncer.set(newWatcher));
	}

	private static CodePiece getSyncContext(ClassNode clazz, ASMVariable variable) {
		CodePiece clazzObj = CodePieces.constant(Type.getObjectType(clazz.name));
		CodePiece context;
		if (variable.isMethod()) {
			CodePiece method = CodePieces.invokeVirtual(Class.class, "getDeclaredMethod", getType(clazz.name),
					Method.class,
					String.class, variable.rawName(),
					Class[].class, new Class[0]);

			context = CodePieces.instantiate(MethodContext.class, Method.class, method);
		} else if (variable.isField()) {
			CodePiece field = CodePieces.invokeVirtual(Class.class, "getDeclaredField", Field.class,
					Class.class, )
		}
	}

	@Override
	ASMCondition hasChanged() {
		String owner = Type.getInternalName(ValueSyncer.class);
		String name = "hasChanged";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(Object.class));
		return ASMCondition.ifTrue(CodePieces.invokeInterface(owner, name, desc, syncer.get(), var.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(ValueSyncer.class);
		String name = "writeAndUpdate";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(MCDataOutputStream.class));
		return CodePieces.invokeInterface(owner, name, desc, syncer.get(), var.get(), stream);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(ValueSyncer.class);
		String name = "read";
		String desc = Type.getMethodDescriptor(getType(Object.class), getType(MCDataInputStream.class));
		return var.set(CodePieces.invokeInterface(owner, name, desc, syncer.get(), stream));
	}

	@Override
	Class<? extends Annotation> getAnnotation() {
		return Sync.class;
	}
}
