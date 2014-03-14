package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author diesieben07
 */
public class HasExtendedMetadataTransformer extends AbstractMetadataTransformer {

	@Override
	boolean matches(ClassInfo classInfo) {
		return hasExtendedMetaCI.isAssignableFrom(classInfo);
	}

	@Override
	void transformBlock(ClassNode clazz) {
		MethodNode method = getBlockGetter();
		InsnList insns = method.instructions;



		clazz.methods.add(method);
	}

	@Override
	void transformCommon(ClassNode clazz) {

	}
}
