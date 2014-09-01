package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMVariables;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.sync.SyncableProxyInternal;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.MakeSyncable;
import de.take_weiland.mods.commons.sync.Syncable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class MakeSyncableImpl extends SyncingTransformerImpl {

	MakeSyncableImpl(ClassNode clazz, ClassInfo classInfo) {
		super(MakeSyncable.Watch.class, clazz, classInfo);
	}

	@Override
	boolean isOutStreamLazy() {
		return false;
	}

	@Override
	String readMethodName() {
		return "_sc$syncable$read0";
	}

	@Override
	String writeMethodName() {
		return "_sc$syncable$write0";
	}

	@Override
	String uniqueIdentifier() {
		return "syncable";
	}

	@Override
	void postTransform() {
		if (superIsSynced) {
			return;
		}

		String name = SyncableProxyInternal.READ;
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(MCDataInputStream.class));
		MethodNode actualRead = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = actualRead.instructions;

		String owner = clazz.name;
		name = readMethodName();
		desc = Type.getMethodDescriptor(INT_TYPE, getType(MCDataInputStream.class), BOOLEAN_TYPE);
		CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc,
				CodePieces.getThis(), CodePieces.of(new VarInsnNode(ALOAD, 1)), CodePieces.constant(false)).appendTo(insns);

		insns.add(new InsnNode(POP));
		insns.add(new InsnNode(RETURN));

		clazz.methods.add(actualRead);

		name = SyncableProxyInternal.WRITE;
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(MCDataOutputStream.class));
		MethodNode actualWrite = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		insns = actualWrite.instructions;

		owner = clazz.name;
		name = writeMethodName();
		desc = Type.getMethodDescriptor(getType(MCDataOutputStream.class), BOOLEAN_TYPE, getType(MCDataOutputStream.class));
		CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc,
				CodePieces.getThis(), CodePieces.constant(false), CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(insns);

		insns.add(new InsnNode(POP));
		insns.add(new InsnNode(RETURN));

		clazz.methods.add(actualWrite);

		name = SyncableProxyInternal.AS_SYNCABLE;
		desc = Type.getMethodDescriptor(getType(Syncable.class));
		MethodNode asSyncable = new MethodNode(ACC_PUBLIC, name, desc, null, null);

		owner = ASMHooks.CLASS_NAME;
		name = ASMHooks.SYNC_PROXY_AS_SYNCABLE;
		desc = Type.getMethodDescriptor(getType(Syncable.class), getType(SyncableProxyInternal.class));

		CodePiece newSyncable = CodePieces.invokeStatic(owner, name, desc, CodePieces.getThis());

		name = "_sc$syncable$wrapper";
		desc = Type.getDescriptor(Syncable.class);
		FieldNode field = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		clazz.fields.add(field);

		CodePieces.makeLazy(ASMVariables.of(clazz, field, CodePieces.getThis()), newSyncable).appendTo(asSyncable.instructions);

		asSyncable.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(asSyncable);

		clazz.interfaces.add(SyncableProxyInternal.CLASS_NAME);
	}

}
