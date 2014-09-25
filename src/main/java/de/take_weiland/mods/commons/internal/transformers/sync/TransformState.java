package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
* @author diesieben07
*/
class TransformState {

	List<PropertyHandler> handlers;
	final ClassNode clazz;
	int superSyncCount;
	int level;
	final SyncType type;

	final List<CodePiece> firstConstructInit = Lists.newArrayList();
	final List<CodePiece> constructorInit = Lists.newArrayList();

	MethodNode readIdx;
	MethodNode writeIdx;

	MethodNode syncRead;
	MethodNode syncWrite;

	TransformState(ClassNode clazz, SyncType type) {
		this.clazz = clazz;
		this.type = type;
	}

	boolean isSuperSynced() {
		return superSyncCount > 0;
	}
}
