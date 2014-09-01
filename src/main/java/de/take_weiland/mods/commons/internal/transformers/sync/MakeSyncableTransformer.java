package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.transformers.AbstractAnalyzingTransformer;
import de.take_weiland.mods.commons.sync.MakeSyncable;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author diesieben07
 */
public class MakeSyncableTransformer extends AbstractAnalyzingTransformer {

	private static final ClassInfo makeSyncableCI = ClassInfo.of(MakeSyncable.class);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (!makeSyncableCI.isAssignableFrom(classInfo)) {
			return false;
		}

		new MakeSyncableImpl(clazz, classInfo).transform();

		return true;
	}
}
