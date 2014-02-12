package de.take_weiland.mods.commons.asm.transformers;

import de.take_weiland.mods.commons.Internal;
import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.AppendingTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

@Internal
public final class GuiScreenTransformer extends AppendingTransformer {

	@Override
	protected InsnList getAppends(ClassNode clazz, MethodNode method) {
		String buttonListField = ASMUtils.useMcpNames() ? ASMConstants.F_BUTTON_LIST_MCP : ASMConstants.F_BUTTON_LIST_OBF;
		
		InsnList insns = new InsnList();
		
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new InsnNode(Opcodes.DUP));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, buttonListField, ASMUtils.getFieldDescriptor(clazz, buttonListField)));
		
		insns.add(ASMUtils.generateStaticMethodCall(SevenCommons.ASM_HOOK_CLASS, "onGuiInit", Type.VOID_TYPE, Type.getObjectType(clazz.name), Type.getType(List.class)));
		
		return insns;
	}
	
	@Override
	protected String getMcpMethod() {
		return ASMConstants.M_SET_WORLD_AND_RESOLUTION_MCP;
	}

	@Override
	protected String getSrgMethod() {
		return ASMConstants.M_SET_WORLD_AND_RESOLUTION_SRG;
	}

	@Override
	protected boolean transforms(String className) {
		return className.equals("net.minecraft.client.gui.GuiScreen");
	}

}
