package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.ASMNames.M_SET_WORLD_AND_RESOLUTION_MCP;
import static de.take_weiland.mods.commons.asm.ASMNames.M_SET_WORLD_AND_RESOLUTION_SRG;

public final class GuiScreenTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_SET_WORLD_AND_RESOLUTION_MCP, M_SET_WORLD_AND_RESOLUTION_SRG);
		InsnList insns = new InsnList();

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));

		String name = "onGuiInit";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(clazz.name));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));

		AbstractInsnNode lastReturn = ASMUtils.findLastReturn(method);
		method.instructions.insertBefore(lastReturn, insns);
		return true;
	}

	@Override
	public boolean transforms(String className) {
		return className.equals("net/minecraft/client/gui/GuiScreen");
	}

}
