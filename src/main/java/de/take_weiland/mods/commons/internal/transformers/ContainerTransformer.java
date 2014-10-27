package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import net.minecraft.inventory.ICrafting;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class ContainerTransformer implements ASMClassTransformer {
	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, MCPNames.M_ADD_CRAFTING_TO_CRAFTERS);
		String owner = ASMHooks.CLASS_NAME;
		String name = ASMHooks.NEW_CONTAINER_WATCHER;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getObjectType(clazz.name), getType(ICrafting.class));
		CodePieces.invokeStatic(owner, name, desc, CodePieces.getThis(), CodePieces.of(new VarInsnNode(ALOAD, 1)))
				.prependTo(method.instructions);

		return true;
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/inventory/Container".equals(internalName);
	}
}
