package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.asm.CodePieces;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author diesieben07
 */
public class ContainerTransformer implements ASMClassTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, "func_75142_b");
		SyncingTransformer.addBaseSyncMethodCall(clazz.name, CodePieces.getThis()).prependTo(method.instructions);

		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/inventory/Container".equals(internalName);
	}
}
