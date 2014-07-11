package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.sync.SyncAdapter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public abstract class VarSyncer {

	abstract CodePiece checkAndUpdate(CodePiece onChange);

	static class ForPrimitive extends VarSyncer {

		private final ASMVariable variable;
		private final ASMVariable companion;

		ForPrimitive(ASMVariable variable, ASMVariable companion) {
			this.variable = variable;
			this.companion = companion;
		}

		@Override
		CodePiece checkAndUpdate(CodePiece onChange) {
			Type type = variable.getType();
			LabelNode label = new LabelNode();
			switch (type.getSort()) {
				case Type.BOOLEAN:
					// TODO: Figure out if this may be faster
//					return new CodeBuilder()
//							.add(variable.get())
//							.add(companion.get())
//							.add(new InsnNode(IXOR))
//							.add(new JumpInsnNode(IFNE, label))
//							.add(onChange)
//							.add(label)
//							.build();
				case Type.BYTE:
				case Type.SHORT:
				case Type.INT:
				case Type.CHAR:
					return new CodeBuilder()
							.add(variable.get())
							.add(companion.get())
							.add(new JumpInsnNode(IF_ICMPEQ, label))
							.add(onChange)
							.add(label)
							.build();
				case Type.LONG:
					return new CodeBuilder()
							.add(variable.get())
							.add(companion.get())
							.add(new InsnNode(LCMP))
							.add(new JumpInsnNode(IFEQ, label))
							.add(onChange)
							.add(label)
							.build();
				case Type.FLOAT:
					return new CodeBuilder()
							.add(variable.get())
							.add(companion.get())
							.add(new InsnNode(FCMPG))
							.add(new JumpInsnNode(IFEQ, label))
							.add(onChange)
							.add(label)
							.build();
				case Type.DOUBLE:
					return new CodeBuilder()
							.add(variable.get())
							.add(companion.get())
							.add(new InsnNode(DCMPG))
							.add(new JumpInsnNode(IFEQ, label))
							.add(onChange)
							.add(label)
							.build();
			}
			throw new IllegalStateException();
		}
	}

	static class ForObject extends VarSyncer {

		private final ASMVariable variable;
		private final ASMVariable adapter;

		ForObject(ASMVariable variable, ASMVariable adapter) {
			this.variable = variable;
			this.adapter = adapter;
		}

		@Override
		CodePiece checkAndUpdate(CodePiece onChange) {
			String owner = SyncAdapter.CLASS_NAME;
			String method= SyncAdapter.CHECK_AND_UPDATE;
			String desc = ASMUtils.getMethodDescriptor(boolean.class, Object.class);

			CodePiece getAndUpdate = CodePieces.invoke(INVOKEVIRTUAL, owner, method, desc, adapter.get(), variable.get());
			LabelNode dontUpdate = new LabelNode();
			return new CodeBuilder()
					.add(getAndUpdate)
					.add(new JumpInsnNode(IFEQ, dontUpdate))
					.add(onChange)
					.add(dontUpdate)
					.build();
		}
	}

}
