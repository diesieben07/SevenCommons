package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AbstractASMTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class GuiScreenTransformer extends AbstractASMTransformer {

	@Override
	public void transform(ClassNode clazz) {
		String mName = ASMUtils.useMcpNames() ? ASMConstants.M_SET_WORLD_AND_RESOLUTION_MCP : ASMConstants.M_SET_WORLD_AND_RESOLUTION_SRG;
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(mName)) {
				transformSetWorldAndResolution(clazz, method);
			}
		}
	}

	private void transformSetWorldAndResolution(ClassNode clazz, MethodNode method) {
		InsnList insns = new InsnList();

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));

		String name = "onGuiInit";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(clazz.name));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/take_weiland/mods/commons/internal/ASMHooks", name, desc));

		AbstractInsnNode lastReturn = ASMUtils.findLastReturn(method);
		method.instructions.insertBefore(lastReturn, insns);

	}

	@Override
	public boolean transforms(String className) {
		return className.equals("net/minecraft/client/gui/GuiScreen");
	}

}
