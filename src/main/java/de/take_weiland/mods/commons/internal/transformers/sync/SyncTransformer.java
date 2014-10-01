package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.asm.info.MemberInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.internal.sync.SyncedObjectProxy;
import de.take_weiland.mods.commons.internal.transformers.AbstractAnalyzingTransformer;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.Watch;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.take_weiland.mods.commons.asm.CodePieces.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class SyncTransformer extends AbstractAnalyzingTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		boolean hasAnn = false;
		for (FieldNode field : clazz.fields) {
			if (ASMUtils.hasAnnotation(field, Sync.class) || ASMUtils.hasAnnotation(field, Watch.class)) {
				hasAnn = true;
				break;
			}
		}

		if (!hasAnn) {
			for (MethodNode method : clazz.methods) {
				if (ASMUtils.hasAnnotation(method, Sync.class) || ASMUtils.hasAnnotation(method, Watch.class)) {
					hasAnn = true;
					break;
				}
			}
		}

		if (!hasAnn) {
			return false;
		}

		SyncType type;
		if (ClassInfo.of(Entity.class).isAssignableFrom(classInfo)) {
			type = SyncType.ENTITY;
		} else if (ClassInfo.of(TileEntity.class).isAssignableFrom(classInfo)) {
			type = SyncType.TILE_ENTITY;
		} else if (ClassInfo.of(Container.class).isAssignableFrom(classInfo)) {
			type = SyncType.CONTAINER;
		} else {
			throw new RuntimeException("Don't know how to @Sync in class " + clazz.name);
		}

		TransformState state = new TransformState(clazz, type);
		countSupers(state, classInfo);

		List<ASMVariable> vars = JavaUtils.concat(
				ASMVariables.allWith(clazz, Sync.class, CodePieces.getThis()),
				ASMVariables.allWith(clazz, Watch.class, CodePieces.getThis()));

		List<PropertyHandler> handlers = state.handlers = Lists.newArrayListWithCapacity(vars.size());

		int idx = state.superSyncCount;

		for (ASMVariable var : vars) {
			handlers.add(PropertyHandler.create(state, var, ++idx));
		}

		for (PropertyHandler handler : handlers) {
			handler.initialTransform(state);
		}

		createIdxIO(state);

		createWrite(state, handlers);
		createRead(state, handlers);

		createInitCode(clazz, state);

		createSyncCall(state);

		clazz.interfaces.add(SyncedObjectProxy.CLASS_NAME);

		return true;
	}

	private static void createSyncCall(TransformState state) {
		String name = "_sc$sync$level";
		String desc = Type.getMethodDescriptor(INT_TYPE);
		MethodNode syncClass = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		state.clazz.methods.add(syncClass);

		CodePiece myLevel = CodePieces.constant(state.level);
		myLevel.append(new InsnNode(IRETURN)).appendTo(syncClass.instructions);

		CodePiece actualLevel = CodePieces.invoke(state.clazz, syncClass, getThis());

		ASMCondition isSyncClass = ASMCondition.ifSame(myLevel, actualLevel, INT_TYPE);
		ASMCondition isServer = state.type.checkServer(state.clazz);

		ASMCondition conditionForSync = isServer == null ? isSyncClass : isServer.and(isSyncClass);

		CodePiece doSync = CodePieces.invoke(state.clazz, state.syncWrite, getThis(), constantNull(), constant(false))
				.append(new InsnNode(POP));

		state.type.addSyncCall(state.clazz, conditionForSync.doIfTrue(doSync));
	}

	private static void countSupers(TransformState state, ClassInfo clazz) {
		int superCount = 0;
		int level = 0;
		do {
			clazz = clazz.superclass();
			if (clazz == null || clazz.internalName().equals("java/lang/Object")) {
				state.superSyncCount = superCount;
				state.level = level;
				break;
			}
			int prevCount = superCount;
			for (MemberInfo member : Iterables.concat(clazz.getMethods(), clazz.getFields())) {
				if (member.hasAnnotation(Sync.class)) {
					superCount++;
				}
			}
			if (prevCount != superCount) {
				level++;
			}
		} while (true);
	}

	private static void createInitCode(ClassNode clazz, TransformState state) {
		CodeBuilder all = new CodeBuilder();
		if (!state.firstConstructInit.isEmpty()) {
			String name = "_sc$sync$hasInit";
			String desc = Type.BOOLEAN_TYPE.getDescriptor();
			FieldNode hasInitField = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
			clazz.fields.add(hasInitField);
			ASMVariable hasInitVar = ASMVariables.of(clazz, hasInitField);

			ASMCondition hasInit = ASMCondition.ifTrue(hasInitVar.get());

			CodeBuilder firstInit = new CodeBuilder();
			for (CodePiece code : state.firstConstructInit) {
				firstInit.add(code);
			}

			CodePiece firstInitCode = hasInit.doIfFalse(firstInit.build().append(hasInitVar.set(CodePieces.constant(true))));

			name = "_sc$sync$init";
			desc = Type.getMethodDescriptor(VOID_TYPE);
			MethodNode method = new MethodNode(ACC_PRIVATE | ACC_STATIC | ACC_SYNCHRONIZED, name, desc, null, null);
			clazz.methods.add(method);
			firstInitCode.appendTo(method.instructions);
			method.instructions.add(new InsnNode(RETURN));

			all.add(hasInit.doIfFalse(CodePieces.invokeStatic(clazz.name, name, desc)));
		}
		for (CodePiece code : state.constructorInit) {
			all.add(code);
		}
		ASMUtils.initialize(clazz, all.build());
	}

	private static void createIdxIO(TransformState state) {
		String name = "_sc$sync$writeIdx";
		String desc = Type.getMethodDescriptor(VOID_TYPE, INT_TYPE, getType(MCDataOutputStream.class));
		MethodNode method = new MethodNode(ACC_PROTECTED, name, desc, null, null);

		int maxIdx = state.superSyncCount + state.handlers.size();

		String owner = Type.getInternalName(MCDataOutputStream.class);
		String writeName, readName;
		if (maxIdx <= 255) {
			writeName = "writeByte";
			readName = "readUnsignedByte";
		} else if (maxIdx <= UnsignedShorts.MAX_VALUE) {
			writeName = "writeShort";
			readName = "readUnsignedShort";
		} else {
			writeName = "writeInt";
			readName = "readInt";
		}
		desc = Type.getMethodDescriptor(VOID_TYPE, INT_TYPE);

		CodePiece write = CodePieces.invoke(INVOKEVIRTUAL, owner, writeName, desc, CodePieces.of(new VarInsnNode(ALOAD, 2)), CodePieces.of(new VarInsnNode(ILOAD, 1)));
		write.appendTo(method.instructions);
		method.instructions.add(new InsnNode(RETURN));
		state.clazz.methods.add(method);
		state.writeIdx = method;

		name = "_sc$sync$readIdx";
		desc = Type.getMethodDescriptor(INT_TYPE, getType(MCDataInputStream.class));
		method = new MethodNode(ACC_PROTECTED, name, desc, null, null);

		owner = Type.getInternalName(MCDataInputStream.class);
		desc = Type.getMethodDescriptor(INT_TYPE);

		CodePiece read = CodePieces.invoke(INVOKEVIRTUAL, owner, readName, desc, CodePieces.of(new VarInsnNode(ALOAD, 1)));
		read.appendTo(method.instructions);
		method.instructions.add(new InsnNode(IRETURN));
		state.clazz.methods.add(method);
		state.readIdx = method;
	}

	private static void createWrite(TransformState state, List<PropertyHandler> handlers) {
		String name = "_sc$sync$write";
		String desc = Type.getMethodDescriptor(getType(MCDataOutputStream.class), getType(MCDataOutputStream.class), BOOLEAN_TYPE);
		MethodNode write = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		InsnList insns = write.instructions;

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();

		write.localVariables.add(new LocalVariableNode("this", getObjectType(state.clazz.name).getDescriptor(), null, start, end, 0));
		write.localVariables.add(new LocalVariableNode("out", getDescriptor(MCDataOutputStream.class), null, start, end, 1));
		write.localVariables.add(new LocalVariableNode("isSuperCall", BOOLEAN_TYPE.getDescriptor(), null, start, end, 2));

		ASMVariable stream = ASMVariables.local(write, 1);
		CodePiece lazyStream = createLazyStream(stream, state);
		ASMCondition isSuperCall = ASMCondition.ifTrue(ASMVariables.local(write, 2).get());

		insns.add(start);

		if (state.isSuperSynced()) {
			stream.set(CodePieces.invokeSuper(state.clazz, write, stream.get(), CodePieces.constant(true))).appendTo(insns);
		}

		for (PropertyHandler handler : handlers) {
			CodeBuilder cb = new CodeBuilder();
			cb.add(CodePieces.invoke(state.clazz, state.writeIdx, CodePieces.getThis(), CodePieces.constant(handler.idx), lazyStream));

			cb.add(handler.writeAndUpdate(stream.get()));

			handler.hasChanged().doIfTrue(cb.build()).appendTo(insns);
		}

		CodePiece finishStream = CodePieces.invoke(state.clazz, state.writeIdx, getThis(), constant(0), stream.get());

		CodePiece sendStream = CodePieces.invokeStatic(ASMHooks.CLASS_NAME, ASMHooks.SEND_SYNC_STREAM,
				getMethodDescriptor(VOID_TYPE, getType(Object.class), getType(SyncType.class), getType(MCDataOutputStream.class)),
				getThis(), constant(state.type), stream.get());


		ASMCondition streamNotNull = ASMCondition.ifNotNull(stream.get());
		isSuperCall.negate().and(streamNotNull)
				.doIfTrue(finishStream.append(sendStream))
				.appendTo(insns);

		stream.get().appendTo(insns);
		insns.add(new InsnNode(ARETURN));

		insns.add(end);
		state.clazz.methods.add(write);
		state.syncWrite = write;
	}

	private static void createRead(TransformState state, List<PropertyHandler> handlers) {
		String name = SyncedObjectProxy.READ;
		String desc = Type.getMethodDescriptor(INT_TYPE, getType(MCDataInputStream.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		state.clazz.methods.add(method);
		InsnList insns = method.instructions;

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();

		method.localVariables.add(new LocalVariableNode("this", getObjectType(state.clazz.name).getDescriptor(), null, start, end, 0));
		method.localVariables.add(new LocalVariableNode("in", getDescriptor(MCDataInputStream.class), null, start, end, 1));
		method.localVariables.add(new LocalVariableNode("idx", INT_TYPE.getDescriptor(), null, start, end, 2));

		ASMVariable stream = ASMVariables.local(method, 1);
		ASMVariable idx = ASMVariables.local(method, 2);

		insns.add(start);

		CodePiece nextIdxFromStream = CodePieces.invoke(state.clazz, state.readIdx, getThis(), stream.get());

		if (state.isSuperSynced()) {
			CodePiece idxFromSuper = CodePieces.invokeSuper(state.clazz, method, stream.get());
			idx.set(idxFromSuper).appendTo(insns);
		} else {
			idx.set(nextIdxFromStream).appendTo(insns);
		}

		LabelNode beginLoop = new LabelNode();
		insns.add(beginLoop);

		SwitchBuilder sb = new SwitchBuilder(idx.get());
		sb.add(0, constant(0).append(new InsnNode(IRETURN)));

		for (PropertyHandler handler : handlers) {
			sb.add(handler.idx, handler.read(stream.get()).append(sb.getBreak()));
		}

		sb.onDefault(idx.get().append(new InsnNode(IRETURN)));

		sb.build().appendTo(insns);

		idx.set(nextIdxFromStream).appendTo(insns);

		insns.add(new JumpInsnNode(GOTO, beginLoop));

		idx.get().appendTo(insns);
		insns.add(new InsnNode(IRETURN));
		insns.add(end);
		state.syncRead = method;
	}

	static CodePiece createLazyStream(ASMVariable stream, TransformState state) {
		String owner = ASMHooks.CLASS_NAME;
		String name = ASMHooks.NEW_SYNC_STREAM;
		String desc = Type.getMethodDescriptor(getType(MCDataOutputStream.class), getType(Object.class), getType(SyncType.class));
		CodePiece newStream = CodePieces.invokeStatic(owner, name, desc, getThis(), constant(state.type));
		return ASMCondition.ifNull(stream.get()).doIfTrue(stream.set(newStream)).append(stream.get());
	}

	static String uniqueSuffix(ASMVariable var) {
		return (var.isField() ? "f" : "m") + "$" + var.rawName();
	}

	static String companionPrefix() {
		return "_sc$sync$comp$";
	}

	static String companionName(ASMVariable var) {
		return companionPrefix() + uniqueSuffix(var);
	}

}
