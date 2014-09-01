package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.sync.SyncableProxyInternal;
import de.take_weiland.mods.commons.internal.transformers.AbstractAnalyzingTransformer;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Syncable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
public class SyncableTransformer extends AbstractAnalyzingTransformer {

	private static final ClassInfo syncableCI = ClassInfo.of(Syncable.class);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isInterface()) {
			return false;
		}
		if (!syncableCI.isAssignableFrom(classInfo) || syncableCI.isAssignableFrom(classInfo.superclass())) {
			return false;
		}

		addRedirect(clazz, SyncableProxyInternal.NEEDS_SYNCING, SyncableProxyInternal.NEEDS_SYNCING_ORIG, Type.getMethodDescriptor(BOOLEAN_TYPE));
		addRedirect(clazz, SyncableProxyInternal.READ, SyncableProxyInternal.READ_ORIG, ASMUtils.getMethodDescriptor(void.class, MCDataInputStream.class));
		addRedirect(clazz, SyncableProxyInternal.WRITE, SyncableProxyInternal.WRITE_ORIG, ASMUtils.getMethodDescriptor(void.class, MCDataOutputStream.class));

		addAsSyncable(clazz);

		clazz.interfaces.add(SyncableProxyInternal.CLASS_NAME);

		return true;
	}

	private void addAsSyncable(ClassNode clazz) {
		String name = SyncableProxyInternal.AS_SYNCABLE;
		String desc = Type.getMethodDescriptor(getType(Syncable.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(method);
	}

	private void addRedirect(ClassNode clazz, String newMethod, String targetMethod, String desc) {
		MethodNode method = new MethodNode(ACC_PUBLIC, newMethod, desc, null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(ALOAD, 0));

		int argIdx = 1;
		for (Type arg : Type.getArgumentTypes(desc)) {
			insns.add(new VarInsnNode(arg.getOpcode(ILOAD), argIdx++));
		}
		insns.add(new MethodInsnNode(INVOKEVIRTUAL, clazz.name, targetMethod, desc));
		insns.add(new InsnNode(Type.getReturnType(desc).getOpcode(IRETURN)));

		clazz.methods.add(method);
	}

}
