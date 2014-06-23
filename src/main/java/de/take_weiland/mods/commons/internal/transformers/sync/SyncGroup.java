package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.Conditions;
import de.take_weiland.mods.commons.internal.SyncASMHooks;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.net.PacketBuilder;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getObjectType;

/**
 * @author diesieben07
 */
class SyncGroup {

	final SyncHandler handler;
	final Type packetTargetType;
	final ASMPacketTarget packetTarget;
	private final List<SyncedElement> elements = Lists.newArrayList();

	SyncGroup(SyncHandler handler, Type packetTargetType, ASMPacketTarget packetTarget) {
		this.handler = handler;
		this.packetTargetType = packetTargetType;
		this.packetTarget = packetTarget;
	}

	void addElement(SyncedElement element) {
		elements.add(element);
	}

	MethodNode createSendMethod(MethodNode writeIndex) {
		String name = "_sc$syncSend$" + packetTarget.methodPostfix();
		String desc = ASMUtils.getMethodDescriptor(PacketBuilder.class, PacketBuilder.class);
		MethodNode method = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		handler.clazz.methods.add(method);

		InsnList insns = method.instructions;

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();
		insns.add(start);

		final int $this = 0;
		final int $builder = 1;

		method.localVariables.add(new LocalVariableNode("this", getObjectType(handler.clazz.name).getDescriptor(), null, start, end, $this));
		method.localVariables.add(new LocalVariableNode("builder", getDescriptor(PacketBuilder.class), null, start, end, $builder));

		CodePiece createPb = CodePieces.invokeStatic(
				SyncASMHooks.CLASS_NAME, SyncASMHooks.CREATE_BUILDER,
				ASMUtils.getMethodDescriptor(PacketBuilder.class, Object.class, SyncType.class),
				CodePieces.getThis(), CodePieces.constant(handler.type));

		CodePiece getPb = CodePieces.of(new VarInsnNode(ALOAD, $builder));

		CodePiece initPb = Conditions.ifNull(getPb)
				.then(createPb.append(new VarInsnNode(ASTORE, $builder)))
				.build();

		for (SyncedElement element : elements) {
			element.setup();

			CodePiece nullSync = CodePieces.invoke(handler.clazz, writeIndex, CodePieces.getThis(), getPb, CodePieces.constant(-(element.index + 1)));
			CodePiece nonNullIdx = CodePieces.invoke(handler.clazz, writeIndex, CodePieces.getThis(), getPb, CodePieces.constant(element.index));

			CodePiece syncCode;
			if (!ASMUtils.isPrimitive(element.variable.getType())) {
				syncCode = Conditions.ifNull(element.variable.get())
						.then(nullSync)
						.otherwise(nonNullIdx.append(element.syncer.write(element.variable.get(), getPb)))
						.build();
			} else {
				syncCode = nonNullIdx.append(element.syncer.write(element.variable.get(), getPb));
			}

			element.syncer.equals(element.variable.get(), element.companion.get())
					.otherwise(initPb.append(syncCode).append(element.companion.set(element.variable.get())))
					.build()
					.appendTo(insns);
		}

		getPb.append(new InsnNode(ARETURN)).appendTo(insns);

		insns.add(end);
		return method;
	}

}
