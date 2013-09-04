package de.take_weiland.mods.commons.asm.transformers;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.syncing.Synced;
import de.take_weiland.mods.commons.syncing.SyncedFieldAccessor;
import de.take_weiland.mods.commons.util.CollectionUtils;

public class SyncedTransformer extends SelectiveTransformer {

	private static final Type SYNCED = Type.getType(Synced.class);
	static final Type SYNC = Type.getType(Synced.Sync.class);
	private static final Type SYNCED_FIELD_ACCESSOR = Type.getType(SyncedFieldAccessor.class);
	
	private static final Type BYTE_ARRAY_DATA_OUTPUT = Type.getType(ByteArrayDataOutput.class);
	private static final Type BYTE_ARRAY_DATA_INPUT = Type.getType(ByteArrayDataInput.class);
	
	private static final Type ASM_HOOKS = Type.getObjectType(ASMUtils.makeNameInternal(SevenCommons.ASM_HOOK_CLASS));
	
	private static final Type RUNTIME_EXCEPTION = Type.getType(RuntimeException.class);
	private static final String[] RUNTIME_EXCEPTION_NAME_ARRAY = new String[] { RUNTIME_EXCEPTION.getInternalName() };
	
	
	private static final String METHOD_SYNC_SEND = "syncSend";
	private static final String METHOD_SYNC_COMPARE = "syncCompare";
	private static final String METHOD_SYNC_RECEIVE = "syncReceive";
	
	private static final String METHOD_SEND_FIELD = "sendField";
	private static final String SEND_FIELD_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, BYTE_ARRAY_DATA_OUTPUT);
	
	private static final String METHOD_NEEDS_UPDATE = "needsUpdate";
	private static final String NEEDS_UPDATE_DESC = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.INT_TYPE);
	
	private static final String METHOD_RECEIVE_FIELD = "receiveField";
	private static final String RECEIVE_FIELD_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, BYTE_ARRAY_DATA_INPUT);
	
	private static final String METHOD_GET_FIELD_COUNT = "getFieldCount";
	private static final String GET_FIELD_COUNT_DESC = Type.getMethodDescriptor(Type.INT_TYPE);
	
	private static final String METHOD_UPDATE_FIELDS = "updateFields";
	private static final String UPDATE_FIELDS_DESC = Type.getMethodDescriptor(Type.VOID_TYPE);
	
	private static final Type FLUID_STACK = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraftforge.fluids.FluidStack"));
	
	private static final String SYNCED_FIELD_POSTFIX = "_sevencommons_sync";
	
	private static final String BOOLEAN_SIGNATURE = Type.BOOLEAN_TYPE.getDescriptor();
	private static final String BYTE_SIGNATURE = Type.BYTE_TYPE.getDescriptor();
	private static final String SHORT_SIGNATURE = Type.SHORT_TYPE.getDescriptor();
	private static final String INT_SIGNATURE = Type.INT_TYPE.getDescriptor();
	private static final String LONG_SIGNATURE = Type.LONG_TYPE.getDescriptor();
	private static final String FLOAT_SIGNATURE = Type.FLOAT_TYPE.getDescriptor();
	private static final String DOUBLE_SIGNATURE = Type.DOUBLE_TYPE.getDescriptor();
	private static final String STRING_SIGNATURE = Type.getDescriptor(String.class);
	private static final String FLUID_STACK_SIGNATURE = FLUID_STACK.getDescriptor();

	static final List<String> VALID_SYNC_TYPES = ImmutableList.of(
			BOOLEAN_SIGNATURE, BYTE_SIGNATURE, SHORT_SIGNATURE, INT_SIGNATURE,
			LONG_SIGNATURE, FLOAT_SIGNATURE, DOUBLE_SIGNATURE, STRING_SIGNATURE, FLUID_STACK_SIGNATURE);
	
	@Override
	protected boolean transforms(String className) {
		return !className.startsWith("net.minecraft.") && !className.startsWith("net.minecraftforge") && !className.equals(Synced.class.getName()); 
	}

	@Override
	protected boolean transform(final ClassNode clazz, String className) {
		if (!clazz.interfaces.contains(SYNCED.getInternalName()) || clazz.interfaces.contains(SYNCED_FIELD_ACCESSOR.getInternalName())) {
			return false;
		}
		
		FieldNode[] syncedFields = Iterators.toArray(Iterators.filter(clazz.fields.iterator(), new Predicate<FieldNode>() {

			@Override
			public boolean apply(FieldNode field) {
				boolean hasAnnotation = ASMUtils.hasAnnotation(field, SYNC);
				if (!hasAnnotation) {
					return false;
				} else if (!VALID_SYNC_TYPES.contains(field.desc)){
					SevenCommons.LOGGER.warning(String.format("Field %s in class %s has @Sync annotation but is of an invalid type!", field.name, clazz.name));
					return false;
				} else {
					return true;
				}
			}
		}), FieldNode.class);
		
		if (syncedFields.length == 0) {
			SevenCommons.LOGGER.warning(String.format("Class %s implements Synced but doesn't have any valid @Sync fields!", className));
			return false;
		}
		
		if (syncedFields.length > 127) {
			SevenCommons.LOGGER.warning(String.format("Class %s contains too many @Sync fields. Max. is 127.", className));
			return false;
		}
		
		prepareFields(clazz, syncedFields);
		
		clazz.methods.add(buildSendField(clazz, syncedFields));
		clazz.methods.add(buildNeedsUpdate(clazz, syncedFields));
		clazz.methods.add(buildReceiveField(clazz, syncedFields));
		clazz.methods.add(buildGetFieldCount(clazz, syncedFields));
		clazz.methods.add(buildUpdateFields(clazz, syncedFields));
		
		clazz.interfaces.add(SYNCED_FIELD_ACCESSOR.getInternalName());
		
		SevenCommons.LOGGER.info("HEYO I TRANSFORMERD: " + clazz.name);
		
		return true;
	}
	
	private static void prepareFields(ClassNode clazz, FieldNode[] syncedFields) {
		clazz.fields.addAll(Lists.transform(Arrays.asList(syncedFields), new Function<FieldNode, FieldNode>() {

			@Override
			public FieldNode apply(FieldNode field) {
				return new FieldNode(ACC_PRIVATE, field.name + SYNCED_FIELD_POSTFIX, field.desc, field.signature, field.value);
			}
			
		}));
	}
	
	private static MethodNode buildUpdateFields(ClassNode clazz, FieldNode[] syncedFields) {
		MethodNode method = new MethodNode(ACC_PUBLIC, METHOD_UPDATE_FIELDS, UPDATE_FIELDS_DESC, null, null);
		InsnList insns = method.instructions;
		
		insns.add(new VarInsnNode(ALOAD, 0)); // load this
		
		for (int fieldIndex = 0; fieldIndex < syncedFields.length; fieldIndex++) {
			FieldNode field = syncedFields[fieldIndex];
			
			boolean isLast = fieldIndex == syncedFields.length - 1;
			insns.add(new InsnNode(DUP)); // we need "this" also for PUTFIELD
			if (!isLast) {
				insns.add(new InsnNode(DUP)); // if this is not the last field we need "this" again for the next round
			}
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name + SYNCED_FIELD_POSTFIX, field.desc));
		}
		
		insns.add(new InsnNode(RETURN));
		
		return method;
	}
	
	private static MethodNode buildGetFieldCount(ClassNode clazz, FieldNode[] syncedFields) {
		MethodNode method = new MethodNode(ACC_PUBLIC, METHOD_GET_FIELD_COUNT, GET_FIELD_COUNT_DESC, null, null);
		InsnList insns = method.instructions;
		
		int count = syncedFields.length;
		
		insns.add(new IntInsnNode(count <= 127 ? BIPUSH : SIPUSH, count));
		insns.add(new InsnNode(IRETURN));
		
		return method;
	}
	
	private static MethodNode buildSendField(ClassNode clazz, FieldNode[] syncedFields) {
		MethodNode method = new MethodNode(ACC_PUBLIC, METHOD_SEND_FIELD, SEND_FIELD_DESC, null, RUNTIME_EXCEPTION_NAME_ARRAY);
		InsnList insns = method.instructions;
		
		LabelNode afterSwitch = new LabelNode();
		LabelNode defaultTarget = new LabelNode();
		LabelNode[] jumpTargets = makeLabels(syncedFields.length);
		
		insns.add(new VarInsnNode(ALOAD, 2)); // the data output for the call to syncSend
		insns.add(new VarInsnNode(ALOAD, 0)); // load this for GETFIELD
		insns.add(new VarInsnNode(ILOAD, 1)); // the fieldIndex for the switch
		insns.add(new TableSwitchInsnNode(0, syncedFields.length - 1, defaultTarget, jumpTargets));
		
		for (int fieldIndex = 0; fieldIndex < syncedFields.length; fieldIndex++) {
			FieldNode field = syncedFields[fieldIndex];
			Type fieldType = Type.getType(field.desc);
			
			insns.add(jumpTargets[fieldIndex]);
			
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS.getInternalName(), METHOD_SYNC_SEND, Type.getMethodDescriptor(Type.VOID_TYPE, BYTE_ARRAY_DATA_OUTPUT, fieldType)));
			insns.add(new JumpInsnNode(GOTO, afterSwitch));
		}
		
		buildDefaultCase(defaultTarget, insns);
		
		insns.add(afterSwitch);
		insns.add(new InsnNode(RETURN));
		return method;
	}

	private static MethodNode buildNeedsUpdate(ClassNode clazz, FieldNode[] syncedFields) {
		MethodNode method = new MethodNode(ACC_PUBLIC, METHOD_NEEDS_UPDATE, NEEDS_UPDATE_DESC, null, RUNTIME_EXCEPTION_NAME_ARRAY);
		InsnList insns = method.instructions;
		
		LabelNode defaultTarget = new LabelNode();
		LabelNode[] jumpTargets = makeLabels(syncedFields.length);
		
		insns.add(new VarInsnNode(ALOAD, 0)); // this for GETFIELD
		insns.add(new VarInsnNode(ILOAD, 1)); // fieldIndex for switch
		
		insns.add(new TableSwitchInsnNode(0, syncedFields.length - 1, defaultTarget, jumpTargets));
		
		for (int fieldIndex = 0; fieldIndex < syncedFields.length; fieldIndex++) {
			FieldNode field = syncedFields[fieldIndex];
			Type fieldType = Type.getType(field.desc);
			
			insns.add(jumpTargets[fieldIndex]);
			
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name + SYNCED_FIELD_POSTFIX, field.desc));
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS.getInternalName(), METHOD_SYNC_COMPARE, Type.getMethodDescriptor(Type.BOOLEAN_TYPE, fieldType, fieldType)));
			insns.add(new InsnNode(IRETURN));
		}
		
		buildDefaultCase(defaultTarget, insns);
		
		return method;
	}
	
	private static MethodNode buildReceiveField(ClassNode clazz, FieldNode[] syncedFields) {
		MethodNode method = new MethodNode(ACC_PUBLIC, METHOD_RECEIVE_FIELD, RECEIVE_FIELD_DESC, null, RUNTIME_EXCEPTION_NAME_ARRAY);
		InsnList insns = method.instructions;
		
		LabelNode afterSwitch = new LabelNode();
		LabelNode defaultTarget = new LabelNode();
		LabelNode[] jumpTargets = makeLabels(syncedFields.length);
		
		insns.add(new VarInsnNode(ALOAD, 0)); // this for PUTFIELD
		insns.add(new InsnNode(DUP)); // this for GETFIELD
		
		insns.add(new VarInsnNode(ILOAD, 1)); // fieldIndex for switch
		insns.add(new TableSwitchInsnNode(0, syncedFields.length - 1, defaultTarget, jumpTargets));
		
		for (int fieldIndex = 0; fieldIndex < syncedFields.length; fieldIndex++) {
			FieldNode field = syncedFields[fieldIndex];
			Type fieldType = Type.getType(field.desc);
			
			insns.add(jumpTargets[fieldIndex]);
			
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new VarInsnNode(ALOAD, 2)); // the ByteArrayDataInput
			insns.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS.getInternalName(), METHOD_SYNC_RECEIVE, Type.getMethodDescriptor(fieldType, fieldType, BYTE_ARRAY_DATA_INPUT)));
			
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
			
			insns.add(new JumpInsnNode(GOTO, afterSwitch));
		}
		
		buildDefaultCase(defaultTarget, insns);
		
		insns.add(afterSwitch);
		insns.add(new InsnNode(RETURN));
		
		return method;
	}
	
	private static LabelNode[] makeLabels(int count) {
		return Iterators.toArray(CollectionUtils.nCallsIterator(new Supplier<LabelNode>() {

			@Override
			public LabelNode get() {
				return new LabelNode();
			}
			
		}, count), LabelNode.class);
	}
	
	private static void buildDefaultCase(LabelNode defaultTarget, InsnList insns) {
		insns.add(defaultTarget);
		
		insns.add(new TypeInsnNode(NEW, RUNTIME_EXCEPTION.getInternalName()));
		insns.add(new InsnNode(DUP));
		insns.add(new MethodInsnNode(INVOKESPECIAL, RUNTIME_EXCEPTION.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE)));
		insns.add(new InsnNode(ATHROW));
	}
}
