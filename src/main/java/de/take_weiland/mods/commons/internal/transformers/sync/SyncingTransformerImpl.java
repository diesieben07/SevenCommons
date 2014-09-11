package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.asm.info.FieldInfo;
import de.take_weiland.mods.commons.asm.info.MethodInfo;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.net.ProtocolException;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
* @author diesieben07
*/
abstract class SyncingTransformerImpl {

	private static final int TERMINATOR = 0;
	private static final int FIRST_IDX = 1;

	final boolean superIsSynced;
	private final int maxIdx;
	private final List<SyncHandler> handlers;
	private final Class<? extends Annotation> markerAnnotation;
	private MethodNode writeIdxMethod;
	private MethodNode readIdxMethod;

	final ClassNode clazz;
	MethodNode doSyncMethod;

	SyncingTransformerImpl(Class<? extends Annotation> markerAnnotation, ClassNode clazz, ClassInfo classInfo) {
		this.clazz = clazz;
		this.markerAnnotation = markerAnnotation;

		List<ASMVariable> vars = ASMVariables.allWith(clazz, markerAnnotation, CodePieces.getThis());
		handlers = Lists.newArrayListWithCapacity(vars.size());

		int idx = FIRST_IDX;
		ClassInfo ci = classInfo.superclass();
		while (true) {
			if (ci == null) {
				break;
			}
			idx += countSyncs(ci);
			ci = ci.superclass();
		}
		superIsSynced = idx != FIRST_IDX;

		for (ASMVariable var : vars) {
			handlers.add(SyncHandler.create(this, idx++, var));
		}
		maxIdx = idx - 1;
	}

	void preTransform() { }
	void postTransform() { }
	abstract boolean isOutStreamLazy();

	CodePiece makeStreamLazy(ASMVariable stream) {
		return stream.get();
	}

	CodePiece handleWriteFinished(CodePiece stream) {
		return CodePieces.of();
	}

	abstract String readMethodName();
	abstract String writeMethodName();
	abstract String uniqueIdentifier();

	private int countSyncs(ClassInfo ci) {
		int result = 0;
		for (FieldInfo field : ci.getFields()) {
			if (field.hasAnnotation(markerAnnotation)) {
				++result;
			}
		}
		for (MethodInfo method : ci.getMethods()) {
			if (method.hasAnnotation(markerAnnotation)) {
				++result;
			}
		}
		return result;
	}

	final void transform() {
		preTransform();
		addIdxIO();
		addDoSync();
		addSyncRead();
		postTransform();
	}

	private CodePiece doWriteIndex(CodePiece stream, CodePiece index) {
		return CodePieces.invoke(clazz, writeIdxMethod, CodePieces.getThis(), stream, index);
	}

	private void addDoSync() {
		String name = writeMethodName();
		String desc = ASMUtils.getMethodDescriptor(MCDataOutputStream.class, boolean.class, MCDataOutputStream.class);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		final int $this = 0;
		final int $isSuperCall = 1;
		final int $stream = 2;
		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();
		method.localVariables.add($this, new LocalVariableNode("this", Type.getObjectType(clazz.name).getDescriptor(), null, start, end, $this));
		method.localVariables.add($isSuperCall, new LocalVariableNode("isSuperCall", Type.getDescriptor(boolean.class), null, start, end, $isSuperCall));
		method.localVariables.add($stream, new LocalVariableNode("stream", Type.getDescriptor(MCDataOutputStream.class), null, start, end, $stream));

		final ASMVariable stream = ASMVariables.local(method, $stream);
		final ASMVariable isSuperCallVar = ASMVariables.local(method, $isSuperCall);

		InsnList insns = method.instructions;
		insns.add(start);

		if (superIsSynced) {
			stream.set(CodePieces.invokeSuper(clazz, method, CodePieces.constant(true), stream.get())).appendTo(insns);
		}

		doWrite(insns, stream.get(), makeStreamLazy(stream), isSuperCallVar.get());

		stream.get().appendTo(insns);
		insns.add(new InsnNode(ARETURN));
		insns.add(end);
		clazz.methods.add(method);
		doSyncMethod = method;
	}

	private void doWrite(InsnList insns, CodePiece directStream, CodePiece lazyStream, CodePiece isSuperCall) {
		for (SyncHandler handler : handlers) {
			handler.initialTransform();

			CodePiece writeIndex = doWriteIndex(lazyStream, CodePieces.constant(handler.index));
			CodePiece writeAndUpdate = handler.writeDataAndUpdate(directStream);

			handler.doChangeCheck(writeIndex.append(writeAndUpdate)).appendTo(insns);
		}

		CodePiece writeTerm = doWriteIndex(directStream, CodePieces.constant(TERMINATOR));
		CodePiece streamFinished = writeTerm.append(handleWriteFinished(directStream));
		CodePiece checkStreamNull = ASMCondition.ifNotNull(directStream).doIfTrue(streamFinished);

		CodePiece body = isOutStreamLazy() ? checkStreamNull : streamFinished;
		ASMCondition.ifFalse(isSuperCall).doIfTrue(body).appendTo(insns);
	}

	private void addSyncRead() {
		String name = readMethodName();
		String desc = Type.getMethodDescriptor(INT_TYPE, getType(MCDataInputStream.class), BOOLEAN_TYPE);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();

		final int $this = 0;
		final int $stream = 1;
		final int $isSuperCall = 2;
		final int $index = 3;
		method.localVariables.add($this, new LocalVariableNode("this", getObjectType(clazz.name).getDescriptor(), null, start, end, $this));
		method.localVariables.add($stream, new LocalVariableNode("in", getDescriptor(MCDataInputStream.class), null, start, end, $stream));
		method.localVariables.add($isSuperCall, new LocalVariableNode("isSuperCall", BOOLEAN_TYPE.getDescriptor(), null, start, end, $isSuperCall));
		method.localVariables.add($index, new LocalVariableNode("index", INT_TYPE.getDescriptor(), null, start, end, $index));

		ASMVariable stream = ASMVariables.local(method, $stream);
		ASMVariable isSuperCall = ASMVariables.local(method, $isSuperCall);
		ASMVariable index = ASMVariables.local(method, $index);

		InsnList insns = method.instructions;
		insns.add(start);

		CodePiece readNextIdx = CodePieces.invoke(clazz, readIdxMethod, CodePieces.getThis(), stream.get());
		if (superIsSynced) {
			CodePiece superCall = CodePieces.invokeSuper(clazz, method, stream.get(), CodePieces.constant(true));
			index.set(superCall).appendTo(insns);
		} else {
			index.set(readNextIdx).appendTo(insns);
		}

		SwitchBuilder switchBuilder = new SwitchBuilder(index.get());
		CodePiece _break = switchBuilder.getBreak();
		for (SyncHandler handler : handlers) {
			switchBuilder.add(handler.index, handler.readData(stream.get()).append(_break));
		}
		switchBuilder.add(TERMINATOR, CodePieces.constant(TERMINATOR).append(new InsnNode(IRETURN)));

		CodePiece returnIndex = index.get().append(new InsnNode(IRETURN));
		CodePiece defaultHandler = ASMCondition.ifTrue(isSuperCall.get()).doIfElse(returnIndex, throwInvalidIdx());

		switchBuilder.onDefault(defaultHandler);
		switchBuilder.build().appendTo(insns);

		if (superIsSynced) {
			index.set(readNextIdx).appendTo(insns);
		}

		insns.add(new JumpInsnNode(GOTO, start));
		insns.add(end);
		clazz.methods.add(method);
	}

	private CodePiece throwInvalidIdx() {
		CodePiece newEx = CodePieces.instantiate(ProtocolException.class,
				new Type[] { getType(String.class) },
				CodePieces.constant("Invalid SyncIndex in " + clazz.name));
		return newEx.append(new InsnNode(ATHROW));
	}

	private void addIdxIO() {
		String name = memberName("writeIdx");
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(MCDataOutputStream.class), INT_TYPE);
		MethodNode write = new MethodNode(ACC_PROTECTED, name, desc, null, null);

		String size;
		Type sizeType;

		if (maxIdx <= 0xFF) {
			size = "Byte";
			sizeType = BYTE_TYPE;
		} else if (maxIdx <= 0xFFFF) {
			size = "Short";
			sizeType = SHORT_TYPE;
		} else {
			throw new UnsupportedOperationException(String.format("Need to sync %d fields in class %s. Max value is 65535.", maxIdx, clazz.name));
		}

		String owner = Type.getInternalName(MCDataOutputStream.class);
		name = "write" + size;
		desc = Type.getMethodDescriptor(VOID_TYPE, INT_TYPE);
		CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc,
				CodePieces.of(new VarInsnNode(ALOAD, 1)),
				CodePieces.of(new VarInsnNode(ILOAD, 2))).appendTo(write.instructions);
		write.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(write);
		writeIdxMethod = write;

		name = memberName("readIdx");
		desc = Type.getMethodDescriptor(INT_TYPE, getType(MCDataInputStream.class));
		MethodNode read = new MethodNode(ACC_PROTECTED, name, desc, null, null);

		owner = Type.getInternalName(MCDataInputStream.class);
		name = "readUnsigned" + size;
		desc = Type.getMethodDescriptor(sizeType);
		CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc,
				CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(read.instructions);
		read.instructions.add(new InsnNode(IRETURN));
		clazz.methods.add(read);
		readIdxMethod = read;
	}

	String memberName(String name) {
		return "_sc$" + uniqueIdentifier() + "$" + name;
	}

}
