package de.take_weiland.mods.commons.asm.transformers;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getObjectType;
import static org.objectweb.asm.Type.getType;

import java.io.DataInput;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ASMUtils.ClassInfo;
import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.SelectiveTransformer;
import de.take_weiland.mods.commons.asm.SyncASMHooks;
import de.take_weiland.mods.commons.sync.SyncType;
import de.take_weiland.mods.commons.sync.Synced;
import de.take_weiland.mods.commons.sync.TypeSyncer;

/**
 * This class is pretty much pure black magic.
 * Only touch if you know your way around bytecode.
 * @author diesieben07
 *
 */
public class SyncingTransformer extends SelectiveTransformer {

	public static final Logger LOGGER;
	
	static {
		FMLLog.makeLog("SevenCommonsSync");
		LOGGER = Logger.getLogger("SevenCommonsSync");
	}
	
	@Override
	protected boolean transforms(String className) {
		return !className.startsWith("net.minecraft.") && !className.startsWith("de.take_weiland.mods.commons.sync.");
	}

	@Override
	protected boolean transform(ClassNode clazz, String className) {
		if (!ASMUtils.hasAnnotation(clazz, Synced.class) || (clazz.access & ACC_INTERFACE) == ACC_INTERFACE) {
			return false;
		}
		
		Class<?> superClass = null;
		try {
			superClass = getClass().getClassLoader().loadClass(ASMUtils.undoInternalName(clazz.superName));
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		List<FieldNode> syncedFields = Lists.newArrayList();
		List<FieldNode> companions = Lists.newArrayList();
		List<FieldNode> syncerFields = Lists.newArrayList();
		BitSet specialCases = new BitSet();
		int i = 0;
		for (FieldNode field : ImmutableList.copyOf(clazz.fields)) { // copy the list because we add to it
			AnnotationNode synced = ASMUtils.getAnnotation(field, Synced.class);
			if (synced != null) {
				boolean isSpecialCase = isSpecialCase(getType(field.desc));
				specialCases.set(i++, isSpecialCase);
				
				syncedFields.add(field);
				companions.add(createCompanion(clazz, field));
				syncerFields.add(findOrCreateSyncer(clazz, field, synced, isSpecialCase));
			}
		}
		
		if (syncedFields.isEmpty()) {
			LOGGER.warning(String.format("Class %s is @Synced but has no @Synced fields!", className));
		} else {
			SyncType type = null;
			if (Entity.class.isAssignableFrom(superClass)) {
				type = SyncType.ENTITY;
			} else if (TileEntity.class.isAssignableFrom(superClass)) {
				type = SyncType.TILE_ENTITY;
			} else if (Container.class.isAssignableFrom(superClass)) {
				type = SyncType.CONTAINER;
			} else if (ASMUtils.isAssignableFrom(ASMUtils.getClassInfo(Type.getInternalName(IExtendedEntityProperties.class)), ASMUtils.getClassInfo(className))) {
				type = SyncType.ENTITY_PROPS;
			} else {
				LOGGER.warning("Cannot sync class " + clazz.name + "!");
				return false;
			}
			
			FieldNode isInit = createIsInitField(clazz);
			MethodNode initMethod = createInitMethod(clazz, isInit, syncedFields, syncerFields, specialCases);
			injectInitCalls(clazz, initMethod);
			
			MethodNode syncMethod = createSyncMethod(clazz, type, syncedFields, companions, syncerFields, specialCases);
			
			InsnList insns = createPerformSyncCall(clazz, type.getRootClass(), type.getWorldFieldName(), syncMethod);
			String name;
			switch (type) {
			case ENTITY:
				name = ASMUtils.useMcpNames() ? ASMConstants.M_ON_UPDATE_MCP : ASMConstants.M_ON_UPDATE_SRG;
				addOrCreateMethod(clazz, name, getMethodDescriptor(VOID_TYPE), insns);
				break;
			case TILE_ENTITY:
				name = ASMUtils.useMcpNames() ? ASMConstants.M_UPDATE_ENTITY_MCP : ASMConstants.M_UPDATE_ENTITY_SRG;
				addOrCreateMethod(clazz, name, getMethodDescriptor(VOID_TYPE), insns);
				break;
			case CONTAINER:
				name = ASMUtils.useMcpNames() ? ASMConstants.M_DETECT_AND_SEND_CHANGES_MCP : ASMConstants.M_DETECT_AND_SEND_CHANGES_SRG;
				addOrCreateMethod(clazz, name, getMethodDescriptor(VOID_TYPE), insns);
				break;
			case ENTITY_PROPS:
				addTickMethod(clazz, syncMethod);
				
				FieldNode owner = createOwnerField(clazz);
				FieldNode index = createIndexField(clazz);
				FieldNode identifier = createIdentifierField(clazz);
				addInjectDataMethod(clazz, owner, index, identifier);
				
				createGetter(clazz, "_sc_sync_getEntity", owner);
				createGetter(clazz, "_sc_sync_getIndex", index);
				createGetter(clazz, "_sc_sync_getIdentifier", identifier);

				clazz.interfaces.add("de/take_weiland/mods/commons/sync/SyncedEntityProperties");
				
				break;
			}
			
			createReadMethod(clazz, syncedFields, syncerFields, specialCases);
			clazz.interfaces.add("de/take_weiland/mods/commons/sync/SyncedObject");
			
			LOGGER.info(String.format("Made class %s @Synced", className));
		}
		
		return true;
	}

	private void createGetter(ClassNode clazz, String name, FieldNode field) {
		Type fieldType = getType(field.desc);
		String desc = getMethodDescriptor(fieldType);
		MethodNode getter = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		getter.instructions.add(new VarInsnNode(ALOAD, 0));
		getter.instructions.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
		getter.instructions.add(new InsnNode(fieldType.getOpcode(IRETURN)));
		clazz.methods.add(getter);
	}
	
	
	private FieldNode createIdentifierField(ClassNode clazz) {
		String name = "_sc_sync_propsident";
		String desc = getDescriptor(String.class);
		FieldNode field = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	}
	
	private FieldNode createIndexField(ClassNode clazz) {
		String name = "_sc_sync_propsindex";
		String desc = INT_TYPE.getDescriptor();
		FieldNode field = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	}
	
	private FieldNode createOwnerField(ClassNode clazz) {
		String name = "_sc_sync_propsowner";
		String desc = getDescriptor(Entity.class);
		FieldNode field = new FieldNode(ACC_PRIVATE, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	}

	private void addInjectDataMethod(ClassNode clazz, FieldNode owner, FieldNode index, FieldNode identifier) {
		String name = "_sc_sync_injectData";
		String desc = getMethodDescriptor(VOID_TYPE, getType(Entity.class), getType(String.class), INT_TYPE);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new FieldInsnNode(PUTFIELD, clazz.name, owner.name, owner.desc));
		
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ALOAD, 2));
		insns.add(new FieldInsnNode(PUTFIELD, clazz.name, identifier.name, identifier.desc));
		
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new VarInsnNode(ILOAD, 3));
		insns.add(new FieldInsnNode(PUTFIELD, clazz.name, index.name, index.desc));
		
		insns.add(new InsnNode(RETURN));
		
		clazz.methods.add(method);
	}

	private void addTickMethod(ClassNode clazz, MethodNode syncMethod) {
		String name = "_sc_sync_tick";
		String desc = getMethodDescriptor(VOID_TYPE);
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 0));
		method.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.name, syncMethod.name, syncMethod.desc));
		method.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(method);
	}

	private void createReadMethod(ClassNode clazz, List<FieldNode> syncedFields, List<FieldNode> syncerFields, BitSet specialCases) {
		String name = "_sc_sync_read";
		String desc = getMethodDescriptor(VOID_TYPE, getType(DataInput.class));
		MethodNode method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;
		
		LabelNode readNext = new LabelNode();
		
		insns.add(readNext);
		insns.add(new VarInsnNode(ALOAD, 0)); // for PUTFIELD
		
		insns.add(new VarInsnNode(ALOAD, 1));
		insns.add(new InsnNode(DUP));
		String owner = "de/take_weiland/mods/commons/asm/SyncASMHooks";
		name = "nextIdx";
		desc = getMethodDescriptor(INT_TYPE, getType(DataInput.class));
		insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
		insns.add(new InsnNode(DUP));
		
		int len = syncedFields.size();
		LabelNode[] labels = new LabelNode[len];
		for (int i = 0; i < len; ++i) {
			labels[i] = new LabelNode();
		}
		LabelNode dflt = new LabelNode();
		LabelNode finish = new LabelNode();
		
		insns.add(new TableSwitchInsnNode(-1, len - 1, dflt, ObjectArrays.concat(finish, labels)));
		for (int i = 0; i < len; ++i) {
			FieldNode field = syncedFields.get(i);
			FieldNode syncer = syncerFields.get(i);
			boolean isSpecialCase = specialCases.get(i);
			Type fieldType = getType(field.desc);
			
			insns.add(labels[i]);
			
			name = "read";
			if (!isSpecialCase) {
				insns.add(new VarInsnNode(ALOAD, 0));
				insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
				insns.add(new FieldInsnNode(GETSTATIC, clazz.name, syncer.name, syncer.desc));
				desc = getMethodDescriptor(getType(Object.class), getType(DataInput.class), INT_TYPE, getType(Object.class), getType(TypeSyncer.class));
				insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
				insns.add(new TypeInsnNode(CHECKCAST, fieldType.getInternalName()));
			} else {
				insns.add(new InsnNode(POP));
				if (ASMUtils.isPrimitive(fieldType)) {
					name += "_" + fieldType.getClassName();
					desc = getMethodDescriptor(fieldType, getType(DataInput.class));
					insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
				} else {
					name += "_Enum";
					desc = getMethodDescriptor(getType(Enum.class), getType(DataInput.class), getType(Class.class));
					insns.add(new LdcInsnNode(fieldType));
					insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
					insns.add(new TypeInsnNode(CHECKCAST, fieldType.getInternalName()));
				}
			}
			
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
			
			insns.add(new JumpInsnNode(GOTO, readNext));
		}
		
		insns.add(dflt);
		insns.add(finish);
		for (int j = 0; j < 3; ++j) {
			insns.add(new InsnNode(POP));
		}
		insns.add(new InsnNode(RETURN));
		clazz.methods.add(method);
	}

	private static ClassInfo enumCI = ASMUtils.getClassInfo(Enum.class);
	private boolean isSpecialCase(Type type) {
		return ASMUtils.isPrimitive(type) || ASMUtils.isAssignableFrom(enumCI, ASMUtils.getClassInfo(type.getClassName()));
	}
	
	private FieldNode createIsInitField(ClassNode clazz) {
		String name = "_sc_sync_isInit";
		String desc = Type.BOOLEAN_TYPE.getDescriptor();
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(field);
		return field;
	}
	
	private MethodNode createInitMethod(ClassNode clazz, FieldNode isInit, List<FieldNode> fields, List<FieldNode> syncers, BitSet specialCases) {
		String name = "_sc_sync_doInit";
		String desc = getMethodDescriptor(VOID_TYPE);
		MethodNode method = new MethodNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		
		InsnList insns = method.instructions;
		LabelNode needInit = new LabelNode();
		insns.add(new FieldInsnNode(GETSTATIC, clazz.name, isInit.name, isInit.desc));
		insns.add(new JumpInsnNode(IFEQ, needInit));
		insns.add(new InsnNode(RETURN));
		
		insns.add(needInit);
		
		Type typeSyncerType = getType(TypeSyncer.class);
		Type classType = getType(Class.class);
		String owner = "de/take_weiland/mods/commons/asm/SyncASMHooks";
		name = "obtainSyncer";
		desc = getMethodDescriptor(typeSyncerType, classType);
		int len = fields.size();
		for (int i = 0; i < len; ++i) {
			FieldNode field = fields.get(i);
			FieldNode syncer = syncers.get(i);
			boolean isSpecialCase = specialCases.get(i);
			
			Type fieldType = getType(field.desc);
			if (!isSpecialCase) {
				insns.add(new LdcInsnNode(fieldType));
				insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
				insns.add(new FieldInsnNode(PUTSTATIC, clazz.name, syncer.name, syncer.desc));
			}
		}
		insns.add(new InsnNode(ICONST_1));
		insns.add(new FieldInsnNode(PUTSTATIC, clazz.name, isInit.name, isInit.desc));
		insns.add(new InsnNode(RETURN));
		
		clazz.methods.add(method);
		return method;
	}
	
	private void injectInitCalls(ClassNode clazz, MethodNode initMethod) {
		List<MethodNode> constructors = findConstructors(clazz);
		filterConstructors(clazz, constructors);
		for (MethodNode cstr : constructors) {
			cstr.instructions.insert(new MethodInsnNode(INVOKESTATIC, clazz.name, initMethod.name, initMethod.desc));
		}
	}
	
	private void filterConstructors(ClassNode clazz, List<MethodNode> cstrs) {
		for (Iterator<MethodNode> it = cstrs.iterator(); it.hasNext();) {
			MethodNode cstr = it.next();
			int len = cstr.instructions.size();
			for (int i = 0; i < len; ++i) {
				AbstractInsnNode insn = cstr.instructions.get(i);
				if (insn.getOpcode() == INVOKESPECIAL) {
					MethodInsnNode min = (MethodInsnNode) insn;
					if (min.owner.equals(clazz.name) && min.name.equals("<init>")) {
						it.remove(); // only need the constructors which don't call another constructor
					}
				}
			}
		}
	}
	
	private List<MethodNode> findConstructors(ClassNode clazz) {
		List<MethodNode> constructors = Lists.newArrayList();
		for (MethodNode candidate : clazz.methods) {
			if (candidate.name.equals("<init>")) {
				constructors.add(candidate);
			}
		}
		return constructors;
	}

	private FieldNode findOrCreateSyncer(ClassNode clazz, FieldNode toSync, AnnotationNode synced, boolean isSpecialCase) {
		if (isSpecialCase) {
			return null;
		}
		int syncId;
		if (synced.values != null && synced.values.size() == 2) {
			syncId = ((Integer)synced.values.get(1)).intValue();
		} else {
			syncId = -1;
		}
		
		for (FieldNode candidate : clazz.fields) {
			AnnotationNode defineSyncer = ASMUtils.getAnnotation(candidate, Synced.DefineSyncer.class);
			if (defineSyncer != null && defineSyncer.values != null && defineSyncer.values.size() == 2 && ((Integer)defineSyncer.values.get(1)).intValue() == syncId) {
				if ((candidate.access & ACC_STATIC) != ACC_STATIC) {
					LOGGER.warning(String.format("@DefineSyncer field %s in class %s must be static, will be ignored!", candidate.name, clazz.name));
				} else {
					return candidate;
				}
			}
		}
		
		// none found, create a new one
		String name = "_sc_sync_syncer_" + toSync.name;
		String desc = getDescriptor(TypeSyncer.class);
		FieldNode syncer = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		clazz.fields.add(syncer);
		return syncer;
	}
	
	private MethodNode createSyncMethod(ClassNode clazz, SyncType syncType, List<FieldNode> fields, List<FieldNode> companions, List<FieldNode> syncers, BitSet specialCases) {
		assert(fields.size() == companions.size() && companions.size() == syncers.size());
		
		Type BADOType = getType(ByteArrayDataOutput.class);
		Type objectType = getType(Object.class);
		Type syncerType = getType(TypeSyncer.class);
		Type syncTypeType = getType(SyncType.class);
		
		String syncASMHooks = getInternalName(SyncASMHooks.class);
		String name = "_sc_sync_sync";
		String desc = Type.getMethodDescriptor(VOID_TYPE);
		MethodNode method = new MethodNode(ACC_PRIVATE, name, desc, null, null);
		InsnList insns = method.instructions;
		
		insns.add(new InsnNode(ACONST_NULL)); // initial value for the ByteArrayDataOuput
		
		int len = fields.size();
		for (int i = 0; i < len; ++i) {
			FieldNode field = fields.get(i);
			FieldNode companion = companions.get(i);
			FieldNode syncer = syncers.get(i);
			boolean isSpecialCase = specialCases.get(i);
			
			Type fieldType = getType(field.desc);
			
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new FieldInsnNode(GETSTATIC, syncTypeType.getInternalName(), syncType.name(), syncTypeType.getDescriptor()));
			insns.add(new IntInsnNode(BIPUSH, i));
			
			if (!isSpecialCase) {
				insns.add(new FieldInsnNode(GETSTATIC, clazz.name, syncer.name, syncer.desc));
			}
			
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, companion.name, companion.desc));
			
			name = "sync";
			if (isSpecialCase) {
				if (!ASMUtils.isPrimitive(fieldType)) {
					fieldType = getType(Enum.class);
				}
				desc = getMethodDescriptor(BADOType, BADOType, objectType, syncTypeType, INT_TYPE, fieldType, fieldType);
				insns.add(new MethodInsnNode(INVOKESTATIC, syncASMHooks, name, desc));
			} else {
				desc = getMethodDescriptor(BADOType, BADOType, objectType, syncTypeType, INT_TYPE, syncerType, objectType, objectType);
				insns.add(new MethodInsnNode(INVOKESTATIC, syncASMHooks, name, desc));
			}
			
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new InsnNode(DUP));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, companion.name, companion.desc));
		}
		
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new FieldInsnNode(GETSTATIC, syncTypeType.getInternalName(), syncType.name(), syncTypeType.getDescriptor()));
		
		name = "endSync";
		desc = getMethodDescriptor(VOID_TYPE, BADOType, objectType, syncTypeType);
		insns.add(new MethodInsnNode(INVOKESTATIC, syncASMHooks, name, desc));
		
		insns.add(new InsnNode(RETURN));
		
		clazz.methods.add(method);
		return method;
	}

	private void addOrCreateMethod(ClassNode clazz, String name, String desc, InsnList insns) {
		MethodNode method = null;
		for (MethodNode m : clazz.methods) {
			if (m.name.equals(name) && m.desc.equals(desc)) {
				method = m;
				break;
			}
		}
		if (method == null) {
			method = new MethodNode(ACC_PUBLIC, name, desc, null, null);
			if (!"java/lang/Object".equals(clazz.superName)) {
				insns.insert(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
				insns.insert(new VarInsnNode(ALOAD, 0)); // reverse order because of insert!
				insns.add(new InsnNode(RETURN));
			}
			method.instructions = insns;
			clazz.methods.add(method);
		} else {
			method.instructions.insert(insns);
		}
	}
	
	private InsnList createPerformSyncCall(ClassNode clazz, String worldOwner, String worldName, MethodNode syncMethod) {
		InsnList insns = new InsnList();
		LabelNode whenClient = null;
		if (worldOwner != null) {
			insns.add(new VarInsnNode(ALOAD, 0));
			
			Type world = getObjectType("net/minecraft/world/World");
			insns.add(new FieldInsnNode(GETFIELD, worldOwner, worldName, world.getDescriptor()));
			
			String name = ASMUtils.useMcpNames() ? ASMConstants.F_IS_REMOTE_MCP : ASMConstants.F_IS_REMOTE_SRG;
			insns.add(new FieldInsnNode(GETFIELD, world.getInternalName(), name, BOOLEAN_TYPE.getDescriptor()));
			
			whenClient = new LabelNode();
			
			insns.add(new JumpInsnNode(IFNE, whenClient));
		}
		
		insns.add(new VarInsnNode(ALOAD, 0));
		insns.add(new MethodInsnNode(INVOKESPECIAL, clazz.name, syncMethod.name, syncMethod.desc));
		
		if (worldOwner != null) {
			insns.add(whenClient);
		}
		
		return insns;
	}
	
	private FieldNode createCompanion(ClassNode clazz, FieldNode field) {
		FieldNode companion = new FieldNode(ACC_PRIVATE, field.name + "_sc_sync", field.desc, null, null);
		clazz.fields.add(companion);
		return companion;
	}

}
