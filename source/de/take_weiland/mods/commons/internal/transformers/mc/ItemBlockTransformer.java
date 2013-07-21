package de.take_weiland.mods.commons.internal.transformers.mc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.take_weiland.mods.commons.asm.MethodTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.ASMUtils;

public class ItemBlockTransformer extends MethodTransformer {

	@Override
	protected boolean transform(ClassNode clazz, MethodNode method) {
		final Type[] hookParams = new Type[] {
			Type.getType(EntityPlayer.class),
			Type.INT_TYPE,
			Type.INT_TYPE,
			Type.INT_TYPE,
			Type.INT_TYPE,
			Type.FLOAT_TYPE,
			Type.FLOAT_TYPE,
			Type.FLOAT_TYPE,
			Type.getType(ItemStack.class)
		};
		
		InsnList preHook = new InsnList();
		loadParams(preHook);
		
		preHook.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "onBlockPlacePre", Type.BOOLEAN_TYPE, hookParams));
		
		LabelNode skipCancel = new LabelNode();
		preHook.add(new JumpInsnNode(Opcodes.IFEQ, skipCancel)); // skip canceling if method returned false
		
		preHook.add(new InsnNode(Opcodes.ICONST_0)); // push false
		preHook.add(new InsnNode(Opcodes.IRETURN));
		
		preHook.add(skipCancel);
		
		method.instructions.insert(preHook);
		
		// ----- //
		
		InsnList postHook = new InsnList();
		loadParams(postHook);
		postHook.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "onBlockPlacePost", Type.VOID_TYPE, hookParams));
		
		method.instructions.insertBefore(ASMUtils.findLastReturn(method), postHook);
		
		return true;
	}
	
	private static final void loadParams(InsnList insns) {
		insns.add(new VarInsnNode(Opcodes.ALOAD, 2)); // load the player
		
		for (int i = 4; i <= 7; i++) { // load x, y, z, side
			insns.add(new VarInsnNode(Opcodes.ILOAD, i));
		}
		
		for (int i = 8; i <= 10; i++) { // load hitX, hitY, hitZ
			insns.add(new VarInsnNode(Opcodes.FLOAD, i));
		}
		
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load the item
	}
	
	@Override
	protected String getMcpMethod() {
		return "placeBlockAt";
	}

	@Override
	protected String getSrgMethod() {
		return getMcpMethod(); // it's a forge added method so it's name doesn't change
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.item.ItemBlock");
	}
}
