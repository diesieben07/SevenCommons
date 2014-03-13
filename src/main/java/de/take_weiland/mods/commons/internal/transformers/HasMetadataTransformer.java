package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.tree.ClassNode;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;

/**
 * @author diesieben07
 */
public class HasMetadataTransformer {

	private static final ClassInfo blockCI = getClassInfo("net/minecraft/block/Block");
	private static final ClassInfo itemCI = getClassInfo("net/minecraft/item/Item");

	static void transform(ClassNode clazz, ClassInfo classInfo) {
		if (blockCI.isAssignableFrom(classInfo)) {
			transformBlock(clazz);
		} else if (itemCI.isAssignableFrom(classInfo)) {
			transformItem(clazz);
		} else {
			throw new IllegalArgumentException(String.format("Can only implement HasMetadata on Block or Item! Class: %s", clazz.name));
		}
	}

	private static void transformBlock(ClassNode clazz) {

	}

	private static void transformItem(ClassNode clazz) {

	}

}
