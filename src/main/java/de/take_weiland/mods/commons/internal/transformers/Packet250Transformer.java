package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.DataInput;
import java.io.DataOutput;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Makes Packet250CustomPayload support up to about 8 Megabytes in size while still staying somewhat compatible
 * to the original protocol. Borrowed from FML: <a href="https://github.com/MinecraftForge/FML/commit/d0dd05a15c2eca9eabd308319c2ed85cb632922b">https://github.com/MinecraftForge/FML/commit/d0dd05a15c2eca9eabd308319c2ed85cb632922b</a>
 * @author diesieben07
 */
public class Packet250Transformer implements ASMClassTransformer {

	// 16+7 = 23 bits
	private static final Integer VAR_SHORT_MAX = 8388607;
	private static final Type DATA_INPUT = getType(DataInput.class);
	private static final String DATA_INPUT_NAME = DATA_INPUT.getInternalName();
	private static final Type DATA_OUTPUT = getType(DataOutput.class);
	private static final String DATA_OUTPUT_NAME = DATA_OUTPUT.getInternalName();
	private static final String READ_SHORT = "readShort";
	private static final String WRITE_SHORT = "writeShort";
	private static final String CSTR_DESC = getMethodDescriptor(VOID_TYPE, getType(String.class), getType(byte[].class));

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		String readMethod = use() ? M_READ_PACKET_DATA_MCP : M_READ_PACKET_DATA_SRG;
		String writeMethod = use() ? M_WRITE_PACKET_DATA_MCP : M_WRITE_PACKET_DATA_SRG;
		String getPacketSizeMethod = use() ? M_GET_PACKET_SIZE_MCP : M_GET_PACKET_SIZE_SRG;
		boolean foundCstr = false, foundRead = false, foundWrite = false, foundSize = false;
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>") && method.desc.equals(CSTR_DESC)) {
				replaceCount(method);
				foundCstr = true;
			} else if (method.name.equals(readMethod)) {
				replaceCount(method);
				replaceRead(method);
				foundRead = true;
			} else if (method.name.equals(writeMethod)) {
				replaceWrite(method);
				foundWrite = true;
			} else if (method.name.equals(getPacketSizeMethod)) {
				modifySize(clazz, method);
				foundSize = true;
			}
		}
		if (!foundCstr || !foundRead || !foundWrite || !foundSize) {
			throw wrongState();
		}

		return true;
	}

	private void modifySize(ClassNode clazz, MethodNode method) {
		AbstractInsnNode ret = ASMUtils.findLastReturn(method);
		InsnList hook = new InsnList();
		hook.add(new VarInsnNode(ALOAD, 0));

		String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
		String name = "additionalPacketSize";
		String desc = getMethodDescriptor(INT_TYPE, getObjectType(clazz.name));
		hook.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		hook.add(new InsnNode(IADD));
		method.instructions.insertBefore(ret, hook);
	}

	private void replaceCount(MethodNode method) {
		AbstractInsnNode insn = method.instructions.getFirst();
		do {
			if (insn.getOpcode() == SIPUSH && ((IntInsnNode) insn).operand == 32767) {
				method.instructions.set(insn, new LdcInsnNode(VAR_SHORT_MAX));
				break;
			}
			insn = requireNext(insn);
		} while (true);
	}

	private void replaceRead(MethodNode method) {
		replaceCall(method, DATA_INPUT_NAME, READ_SHORT, "readVarShort", getMethodDescriptor(INT_TYPE, DATA_INPUT));
	}

	private void replaceWrite(MethodNode method) {
		AbstractInsnNode loc = replaceCall(method, DATA_OUTPUT_NAME, WRITE_SHORT, "writeVarShort", getMethodDescriptor(VOID_TYPE, DATA_OUTPUT, INT_TYPE));
		do {
			loc = loc.getPrevious();
			if (loc == null) {
				throw wrongState();
			}
			if (loc.getOpcode() == I2S) {
				method.instructions.remove(loc);
				break;
			}
		} while (true);
	}

	private AbstractInsnNode replaceCall(MethodNode method, String origOwner, String origName, String newName, String newDesc) {
		AbstractInsnNode insn = method.instructions.getFirst();
		MethodInsnNode methodInsn;
		do {
			if (insn.getOpcode() == INVOKEINTERFACE
					&& (methodInsn = (MethodInsnNode) insn).owner.equals(origOwner)
					&& methodInsn.name.equals(origName)) {
				String owner = "de/take_weiland/mods/commons/internal/ASMHooks";
				method.instructions.set(insn, (insn = new MethodInsnNode(INVOKESTATIC, owner, newName, newDesc)));
				return insn;
			}
			insn = requireNext(insn);
		} while (true);
	}

	private AbstractInsnNode requireNext(AbstractInsnNode insn) {
		insn = insn.getNext();
		if (insn == null) {
			throw wrongState();
		}
		return insn;
	}

	private static RuntimeException wrongState() {
		return new IllegalStateException("Illegal Classfile structure in Packet250CustomPayload!");
	}

	@Override
	public boolean transforms(String internalName) {
		return internalName.equals("net/minecraft/network/packet/Packet250CustomPayload");
	}
}
