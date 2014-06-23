package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.sync.Sync;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author diesieben07
 */
public class SyncingTransformer implements ASMClassTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isInterface() || classInfo.isEnum()) {
			return false;
		}

		if (!ASMUtils.hasAnnotationOnAnything(clazz, Sync.class)) {
			return false;
		}

		new SyncHandler(clazz, classInfo).transform();
		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/fml/")
				&& !internalName.startsWith("org/apache/");
	}
}
