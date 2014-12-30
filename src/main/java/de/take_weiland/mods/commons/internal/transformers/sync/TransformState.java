package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
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
	final ClassInfo classInfo;

	MethodNode readIdx;
	MethodNode writeIdx;

	MethodNode syncRead;
	MethodNode syncWrite;

	List<CodePiece> firstCstrInit = new ArrayList<>();

	TransformState(ClassNode clazz, SyncType type, ClassInfo classInfo) {
		this.clazz = clazz;
		this.type = type;
		this.classInfo = classInfo;
	}

	boolean isSuperSynced() {
		return superSyncCount > 0;
	}

}
