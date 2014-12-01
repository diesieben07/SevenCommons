package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import net.minecraft.inventory.Slot;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import static de.take_weiland.mods.commons.asm.CodePieces.invokeStatic;
import static org.objectweb.asm.Opcodes.*;

/**
 * This is to prevent picking up blocked player slots (mostly by ItemInventory) via the number keys in GuiContainer.
 * @author diesieben07
 */
public class GuiContainerTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, MCPNames.M_CHECK_HOTBAR_KEYS);

		String owner = "net/minecraft/client/gui/inventory/GuiContainer";
		String name = MCPNames.field(MCPNames.F_GUICONTAINER_THE_SLOT);
		String desc = Type.getDescriptor(Slot.class);
		CodePiece hoveredSlot = CodePieces.getField(owner, name, desc, CodePieces.getThis());

		owner = ASMHooks.CLASS_NAME;
		name = ASMHooks.IS_USEABLE_CLIENT;
		ASMCondition useable = ASMCondition.ifTrue(invokeStatic(owner, name, boolean.class, Slot.class, hoveredSlot));

		useable.doIfFalse(CodePieces.constant(false).append(new InsnNode(IRETURN)))
				.prependTo(method.instructions);

		return true;
	}

	private static IllegalStateException fail() {
		return new IllegalStateException("Failed to find hook in GuiContainer.checkHotbarKeys");
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/client/gui/inventory/GuiContainer".equals(internalName);
	}
}
