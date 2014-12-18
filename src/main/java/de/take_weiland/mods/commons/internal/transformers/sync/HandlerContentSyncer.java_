package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.SyncContents;
import de.take_weiland.mods.commons.properties.ClassProperty;

import java.lang.annotation.Annotation;

/**
* @author diesieben07
*/
class HandlerContentSyncer extends HandlerSyncer {

	HandlerContentSyncer(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	CodePiece newSyncer(CodePiece syncElement) {
		return CodePieces.invokeStatic(SyncingManager.class, "getContentSyncer", ContentSyncer.class,
				ClassProperty.class, syncElement);
	}

	@Override
	Class<?> syncerClass() {
		return ContentSyncer.class;
	}

	@Override
	CodePiece read(CodePiece stream) {
		return CodePieces.invokeInterface(ContentSyncer.class, "read", syncer.get(), void.class,
				Object.class, var.get(),
				MCDataInputStream.class, stream,
				Object.class, syncerData.get());
	}

	@Override
	Class<? extends Annotation> getAnnotation() {
		return SyncContents.class;
	}
}
