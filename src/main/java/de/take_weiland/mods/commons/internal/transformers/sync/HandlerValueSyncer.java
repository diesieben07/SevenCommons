package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;

import java.lang.annotation.Annotation;

/**
 * @author diesieben07
 */
class HandlerValueSyncer extends HandlerSyncer {

	HandlerValueSyncer(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	CodePiece newSyncer(CodePiece context) {
		return CodePieces.invokeStatic(SyncingManager.class, "getValueSyncer", ValueSyncer.class,
				SyncContext.class, context);
	}

	@Override
	Class<?> syncerClass() {
		return ValueSyncer.class;
	}

	@Override
	CodePiece read(CodePiece stream) {
		CodePiece value = CodePieces.invokeInterface(ValueSyncer.class, "read", syncer.get(), Object.class,
				MCDataInputStream.class, stream,
				Object.class, syncerData.get());
		return var.set(CodePieces.castTo(var.getType(), value));
	}

	@Override
	Class<? extends Annotation> getAnnotation() {
		return Sync.class;
	}
}
