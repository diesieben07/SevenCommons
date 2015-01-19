package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableList;
import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.transformers.sync.SyncTransformer;
import de.take_weiland.mods.commons.internal.transformers.tonbt.ToNBTTransformer;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
class PropertyBasedTransformerWrapper implements ASMClassTransformer {

	private final List<PropertyBasedTransformer> transformers = ImmutableList.of(
			new SyncTransformer(),
			new ToNBTTransformer()
	);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		ClassWithProperties properties = new ClassWithProperties(clazz, classInfo);
		boolean hasTransformed = false;

		for (PropertyBasedTransformer transformer : transformers) {
			hasTransformed |= transformer.transform(properties);
		}

		if (hasTransformed) {
			properties.createFields();
		}
		return hasTransformed;
	}

	@Override
	public boolean transforms(String internalName) {
		return true;
	}

}
