package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.transformers.AbstractAnalyzingTransformer;
import de.take_weiland.mods.commons.sync.Sync;
import org.objectweb.asm.tree.*;

/**
 * @author diesieben07
 */
public class SyncingTransformer extends AbstractAnalyzingTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (!ASMUtils.hasMemberAnnotation(clazz, Sync.class)) {
			return false;
		}


		new AutoSyncImpl(clazz, classInfo).transform();

		return true;
	}

}
