package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
* @author diesieben07
*/
class TransformState {

	List<PropertyHandler> handlers;

	final ClassWithProperties properties;
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

	TransformState(SyncType type, ClassWithProperties properties) {
		this.clazz = properties.clazz;
		this.type = type;
		this.classInfo = properties.classInfo;
		this.properties = properties;
	}

	boolean isSuperSynced() {
		return superSyncCount > 0;
	}

}
