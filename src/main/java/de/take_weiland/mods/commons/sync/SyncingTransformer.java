package de.take_weiland.mods.commons.sync;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

public class SyncingTransformer extends SelectiveTransformer {

    @Override
    protected boolean transform(ClassNode clazz, String className) {
        AnnotationNode synced;
        if ((synced = ASMUtils.getAnnotation(clazz, Synced.class)) == null) {
            return false;
        }

        List<SyncedFieldOrMethod> fields = Lists.newArrayList();

        String nullInternalName = Type.getInternalName(Synced.NULL.class);
        for (FieldNode field : clazz.fields) {
            AnnotationNode fieldSynced;
            if ((fieldSynced = ASMUtils.getAnnotation(field, Synced.class)) != null) {
	            if (ASMUtils.isPrimitive(Type.getType(field.desc))) {
		            fields.add(new SyncedFieldOrMethod(field));
	            } else {
		            Type syncer = null;
		            Type target = null;
		            boolean syncerStatic = false;
		            boolean targetStatic = false;
		            boolean isPrimitive;
		            int len = fieldSynced.values == null ? 0 : fieldSynced.values.size();
		            for (int i = 0; i < len; i += 2) {
			            String key = (String) fieldSynced.values.get(i);
			            Type value;
			            if (!(value = (Type)fieldSynced.values.get(i + 1)).getInternalName().equals(nullInternalName)) {
				            if (key.equals("syncer")) {
					            syncer = value;
				            } else if (key.equals("target")) {
					            target = value;
				            }
			            }
		            }
		            if (syncer == null) {
			            syncerStatic = true;
		            } else {
			            syncerStatic = hasNoArgConstructor(ASMUtils.getClassNode(syncer.getClassName()));
		            }

		            if (target == null) {
			            targetStatic = true;
		            } else {
			            targetStatic = hasNoArgConstructor(ASMUtils.getClassNode(target.getClassName()));
		            }
		            fields.add(new SyncedFieldOrMethod(field, syncerStatic, targetStatic, syncer, target));
	            }
            }
        }

	    makeNonStaticInitMethod(clazz, fields);
	    makeStaticInitMethod(clazz, fields);
        return true;
    }
	
	private boolean hasNoArgConstructor(ClassNode clazz) {
		String name = "<init>";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return true;
			}
		}
		return false;
	}

	private MethodNode makeNonStaticInitMethod(ClassNode clazz, List<SyncedFieldOrMethod> fields) {
		String owner;
		String name = "_sc_sync_init";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, name, desc, null, null);
		InsnList insns = method.instructions;

		for (SyncedFieldOrMethod field : fields) {
			if (!field.isPrimitive && !field.syncerStatic) {
				name = "_sc_sync_syncer_" + field.field.name;
				desc = Type.getDescriptor(TypeSyncer.class);
				FieldNode syncer = new FieldNode(Opcodes.ACC_PRIVATE, name, desc, null, null);
				clazz.fields.add(syncer);

				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));

				String syncerName = field.syncer.getInternalName();
				insns.add(new TypeInsnNode(Opcodes.NEW, syncerName));
				insns.add(new InsnNode(Opcodes.DUP));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				
				name = "<init>";
				desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class));
				insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, syncerName, name, desc));

				insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, syncer.name, syncer.desc));
			}
		}

		insns.add(new InsnNode(Opcodes.RETURN));
		clazz.methods.add(method);
		return method;
	}

	private MethodNode makeStaticInitMethod(ClassNode clazz, List<SyncedFieldOrMethod> fields) {
		String owner;
		String name = "_sc_sync_init_static";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name, desc, null, null);
		InsnList insns = method.instructions;

		for (SyncedFieldOrMethod field : fields) {
			if (!field.isPrimitive && field.syncerStatic) {
				name = "_sc_sync_syncer_" + field.field.name;
				desc = Type.getDescriptor(TypeSyncer.class);
				FieldNode syncer = new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name, desc, null, null);
				clazz.fields.add(syncer);

				owner = "de/take_weiland/mods/commons/sync/SyncASMHooks";
				desc = Type.getMethodDescriptor(Type.getType(TypeSyncer.class), Type.getType(Class.class));
				if (field.syncer == null) {
					insns.add(new LdcInsnNode(Type.getType(field.field.desc)));
					name = "getSyncerFor";
				} else {
					insns.add(new LdcInsnNode(field.syncer));
					name = "getSyncerInstance";
				}
				insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc));
				insns.add(new FieldInsnNode(Opcodes.PUTSTATIC, clazz.name, syncer.name, syncer.desc));
			}
		}

		insns.add(new InsnNode(Opcodes.RETURN));
		clazz.methods.add(method);
		return method;
	}

    private static class SyncedFieldOrMethod {

        boolean isMethod;
	    FieldNode field;
	    MethodNode method;
	    boolean isPrimitive;
	    boolean syncerStatic;
	    boolean targetStatic;
	    Type syncer;
        Type target;

	    SyncedFieldOrMethod(FieldNode field) {
		    this.field = field;
		    this.isPrimitive = true;
		    this.isMethod = false;
	    }

	    SyncedFieldOrMethod(FieldNode field, boolean syncerStatic, boolean targetStatic, Type syncer, Type target) {
		    this.field = field;
		    this.isMethod = false;
		    this.isPrimitive = false;
		    this.syncerStatic = syncerStatic;
		    this.targetStatic = targetStatic;
		    this.syncer = syncer;
		    this.target = target;
	    }

	    SyncedFieldOrMethod(MethodNode method) {
		    this.method = method;
		    this.isMethod = true;
		    this.isPrimitive = true;
	    }

	    SyncedFieldOrMethod(MethodNode method, boolean isPrimitive, boolean syncerStatic, boolean targetStatic, Type syncer, Type target) {
		    this.method = method;
		    this.isMethod = true;
		    this.isPrimitive = isPrimitive;
		    this.syncerStatic = syncerStatic;
		    this.targetStatic = targetStatic;
		    this.syncer = syncer;
		    this.target = target;
	    }

	    @Override
	    public String toString() {
		    return "SyncedFieldOrMethod{" +
				    "field=" + field +
				    ", syncer='" + syncer + '\'' +
				    ", target='" + target + '\'' +
				    '}';
	    }
    }

    @Override
    protected boolean transforms(String className) {
	    if (className.startsWith("de.take_weiland.mods.commons.testmod_sc")) {
		    return true;
	    }
        return !className.startsWith("net.minecraft.")
                && !className.startsWith("net.minecraftforge.")
                && !className.startsWith("cpw.mods.")
                && !className.startsWith("de.take_weiland.mods.commons.");
    }
}
