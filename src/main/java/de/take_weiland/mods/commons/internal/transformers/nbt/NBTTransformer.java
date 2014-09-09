package de.take_weiland.mods.commons.internal.transformers.nbt;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.transformers.AbstractAnalyzingTransformer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author diesieben07
 */
public class NBTTransformer extends AbstractAnalyzingTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (!ASMUtils.hasMemberAnnotation(clazz, ToNbt.class)) {
			return false;
		}

		new AutoNBTImpl(clazz).transform();

		return true;
	}
}
