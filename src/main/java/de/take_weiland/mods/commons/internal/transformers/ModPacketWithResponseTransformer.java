package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * @author diesieben07
 */
public class ModPacketWithResponseTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("onResponse") || method.name.equals("discardResponse")) {
				method.access |= ACC_PUBLIC;
			}
		}

		clazz.interfaces.add("de/take_weiland/mods/commons/net/SimplePacket$ResponseSentToServer");
		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return internalName.equals("de/take_weiland/mods/commons/net/ModPacket$WithResponse");
	}
}
