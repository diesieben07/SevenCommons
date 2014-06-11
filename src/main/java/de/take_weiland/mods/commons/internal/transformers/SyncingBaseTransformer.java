package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.SyncType;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author diesieben07
 */
public class SyncingBaseTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		SyncingTransformer.addBaseSyncMethod(clazz);
		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return internalName.equals(SyncType.CLASS_TILE_ENTITY)
			|| internalName.equals(SyncType.CLASS_ENTITY)
			|| internalName.equals(SyncType.CLASS_CONTAINER);
	}
}
