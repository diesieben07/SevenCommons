package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.*;
import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.internal.ASMConstants;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.internal.SyncedEntityProperties;
import de.take_weiland.mods.commons.internal.SyncedObject;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;
import de.take_weiland.mods.commons.net.PacketTarget;
import de.take_weiland.mods.commons.sync.Synced;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * contains black bytecode magic. Do not touch.
 */
public final class SyncingTransformer {

	private static final Logger LOGGER;
	private static final String syncAsmHooks = "de/take_weiland/mods/commons/internal/SyncASMHooks";

	static {
		FMLLog.makeLog("SevenCommonsSync");
		LOGGER = Logger.getLogger("SevenCommonsSync");
	}

	public static boolean transform(ClassNode clazz) {
		Class<?> superClass;
		try {
			superClass = SyncingTransformer.class.getClassLoader().loadClass(ASMUtils.binaryName(clazz.superName));
		} catch (ClassNotFoundException e) {
			return false;
		}

		SyncType type;
		if (Entity.class.isAssignableFrom(superClass)) {
			type = SyncType.ENTITY;
		} else if (TileEntity.class.isAssignableFrom(superClass)) {
			type = SyncType.TILE_ENTITY;
		} else if (Container.class.isAssignableFrom(superClass)) {
			type = SyncType.CONTAINER;
		} else if (ASMUtils.isAssignableFrom(ASMUtils.getClassInfo(IExtendedEntityProperties.class), ASMUtils.getClassInfo(clazz))) {
			type = SyncType.ENTITY_PROPS;
		} else {
			LOGGER.warning(String.format("Can't sync class %s, it will be ignored.", clazz.name));
			return false;
		}

		LinkedListMultimap<Type, SyncedElement> elements = LinkedListMultimap.create(); // need LinkedList to preserve iteration order
		Map<String, MethodNode> setters = Maps.newHashMap();
		Map<Type, FieldNode> targets = Maps.newHashMap();

		int idx = 0;
		for (FieldNode field : ImmutableList.copyOf(clazz.fields)) {
			if (makeSyncedElement(clazz, targets, elements, idx, false, field, Type.getType(field.desc), ASMUtils.getAnnotation(field, Synced.class))) {
				++idx;
			}
		}

		for (MethodNode method : ImmutableList.copyOf(clazz.methods)) {
			if (makeSyncedElement(clazz, targets, elements, idx, true, method, Type.getReturnType(method.desc), ASMUtils.getAnnotation(method, Synced.class))) {
				++idx;
			}
			checkIsSetter(setters, method);
		}

		addToConstructors(clazz, findRootConstructors(clazz), makeNonStaticInitMethod(clazz, elements.values(), targets), makeStaticInitMethod(clazz, elements.values(), targets, createHasInitField(clazz)));

		List<MethodNode> syncMethods = Lists.newArrayListWithCapacity(elements.keySet().size());
		for (Type target : elements.keySet()) {
			syncMethods.add(createSyncMethod(clazz, type, target, target == null ? null : targets.get(target), elements.get(target)));
		}

		createReadMethod(clazz, elements.values(), setters);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/SyncedObject");

		injectSyncCalls(clazz, type, syncMethods);

		if (type == SyncType.ENTITY_PROPS) {
			FieldNode owner = createPrivateField(clazz, "_sc$syncedPropsOwner", Type.getType(Entity.class));
			FieldNode index = createPrivateField(clazz, "_sc$syncedPropsIndex", Type.INT_TYPE);
			FieldNode ident = createPrivateField(clazz, "_sc$syncedPropsIdentifier", Type.getType(String.class));
			addInjectDataMethod(clazz, owner, index, ident);
			createGetter(clazz, SyncedEntityProperties.GET_ENTITY, owner);
			createGetter(clazz, SyncedEntityProperties.GET_INDEX, index);
			createGetter(clazz, SyncedEntityProperties.GET_IDENTIFIER, ident);

			clazz.interfaces.add("de/take_weiland/mods/commons/internal/SyncedEntityProperties");
		}
		return true;
	}

	private static void createGetter(ClassNode clazz, String name, FieldNode field) {
		Type fieldType = Type.getType(field.desc);
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, Type.getMethodDescriptor(fieldType), null, null);
		InsnList insns = method.instructions;
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, field.name, field.desc));
		insns.add(new InsnNode(fieldType.getOpcode(Opcodes.IRETURN)));
		clazz.methods.add(method);
	}

	private static void addInjectDataMethod(ClassNode clazz, FieldNode owner, FieldNode index, FieldNode identifier) {
		String name = SyncedEntityProperties.INJECT_DATA;
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Entity.class), Type.getType(String.class), Type.INT_TYPE);
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, owner.name, owner.desc));

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
		insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, identifier.name, identifier.desc));

		insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
		insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, index.name, index.desc));

		insns.add(new InsnNode(Opcodes.RETURN));

		clazz.methods.add(method);
	}

	private static FieldNode createPrivateField(ClassNode clazz, String name, Type type) {
		FieldNode field = new FieldNode(Opcodes.ACC_PRIVATE, name, type.getDescriptor(), null, null);
		clazz.fields.add(field);
		return field;
	}

	private static void injectSyncCalls(ClassNode clazz, SyncType type, List<MethodNode> syncMethods) {
		String methodName = type.getTickMethod();
		boolean isIfaceMethod = type == SyncType.ENTITY_PROPS;
		String worldField = type.getWorldFieldName();

		InsnList call = new InsnList();

		LabelNode lbl = null;
		if (worldField != null) {
			Type world = Type.getObjectType("net/minecraft/world/World");
			lbl = new LabelNode();
			call.add(new VarInsnNode(Opcodes.ALOAD, 0));
			call.add(new FieldInsnNode(Opcodes.GETFIELD, type.getRootClass(), worldField, world.getDescriptor()));
			call.add(new FieldInsnNode(Opcodes.GETFIELD, world.getInternalName(), ASMUtils.useMcpNames() ? ASMConstants.F_IS_REMOTE_MCP : ASMConstants.F_IS_REMOTE_SRG, Type.BOOLEAN_TYPE.getDescriptor()));
			call.add(new JumpInsnNode(Opcodes.IFNE, lbl));
		}

		for (MethodNode method : syncMethods) {
			call.add(new VarInsnNode(Opcodes.ALOAD, 0));
			call.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz.name, method.name, method.desc));
		}

		if (worldField != null) {
			call.add(lbl);
		}

		MethodNode found = null;
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(methodName) && method.desc.equals(desc)) {
				found = method;
				break;
			}
		}
		if (found == null) {
			found = new MethodNode(Opcodes.ACC_PUBLIC, methodName, desc, null, null);
			clazz.methods.add(found);
			if (!isIfaceMethod) {
				found.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				found.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz.superName, methodName, desc));
			}
			found.instructions.add(new InsnNode(Opcodes.RETURN));
		}
		found.instructions.insert(call);
	}

	private static void createReadMethod(ClassNode clazz, List<SyncedElement> elements, Map<String, MethodNode> setters) {
		String name = SyncedObject.READ;
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(DataBuf.class));
		MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, desc, null, null);
		InsnList insns = method.instructions;

		LabelNode readNext = new LabelNode();

		insns.add(readNext);
		insns.add(new VarInsnNode(Opcodes.ALOAD, 0)); // for PUTFIELD / INVOKE of setter

		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insns.add(new InsnNode(Opcodes.DUP));
		name = "nextIdx";
		desc = Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(DataBuf.class));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, name, desc));
		insns.add(new InsnNode(Opcodes.DUP));

		int len = elements.size();
		LabelNode[] labels = new LabelNode[len];
		for (int i = 0; i < len; ++i) {
			labels[i] = new LabelNode();
		}
		LabelNode dflt = new LabelNode();
		LabelNode finish = new LabelNode();

		insns.add(new TableSwitchInsnNode(-1, len - 1, dflt, ObjectArrays.concat(finish, labels)));
		Iterator<SyncedElement> it = elements.iterator(); // it's a linkedList, so no random access
		for (int i = 0; i < len; ++i) {
			SyncedElement element = it.next();

			insns.add(labels[i]);

			Type typeToSync = element.getTypeToSync();
			name = "read";
			if (!element.isPrimitive && !element.isEnum) {
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				if (element.isMethod) {
					insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, clazz.name, element.method.name, element.method.desc));
				} else {
					insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, element.field.name, element.field.desc));
				}
				if (!element.syncerStatic) {
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				}
				insns.add(new FieldInsnNode(element.syncerStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD, clazz.name, element.syncerField.name, element.syncerField.desc));
				desc = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(DataBuf.class), Type.INT_TYPE, Type.getType(Object.class), Type.getType(TypeSyncer.class));
				insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, name, desc));
				insns.add(new TypeInsnNode(Opcodes.CHECKCAST, typeToSync.getInternalName()));
			} else {
				insns.add(new InsnNode(Opcodes.POP));
				if (element.isPrimitive) {
					name += "_" + typeToSync.getClassName();
					desc = Type.getMethodDescriptor(typeToSync, Type.getType(DataBuf.class));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, name, desc));
				} else {
					name += "_Enum";
					desc = Type.getMethodDescriptor(Type.getType(Enum.class), Type.getType(DataBuf.class), Type.getType(Class.class));
					insns.add(new LdcInsnNode(typeToSync));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, name, desc));
					insns.add(new TypeInsnNode(Opcodes.CHECKCAST, typeToSync.getInternalName()));
				}
			}

			if (element.isMethod) {
				MethodNode setter = setters.get(element.setter);
				if (setter == null) {
					throw new IllegalArgumentException(String.format("No setter found for @Synced Method %s in class %s!", element.method.name, clazz.name));
				}
				insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, clazz.name, setter.name, setter.desc));
			} else {
				insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, element.field.name, element.field.desc));
			}

			insns.add(new JumpInsnNode(Opcodes.GOTO, readNext));
		}

		insns.add(dflt);
		insns.add(finish);
		for (int j = 0; j < 3; ++j) {
			insns.add(new InsnNode(Opcodes.POP));
		}
		insns.add(new InsnNode(Opcodes.RETURN));
		clazz.methods.add(method);
	}

	private static MethodNode createSyncMethod(ClassNode clazz, SyncType type, Type target, FieldNode targetField, List<SyncedElement> elements) {
		String name = "_sc$doSync_" + (target == null ? "default" : target.getInternalName().replace('/', '_'));
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, name, desc, null, null);
		InsnList insns = method.instructions;

		insns.add(new InsnNode(Opcodes.ACONST_NULL)); // initial PacketBuilder is null

		Type syncTypeType = Type.getType(SyncType.class);
		Type object = Type.getType(Object.class);
		Type packetBuilder = Type.getType(PacketBuilder.class);
		for (SyncedElement element : elements) {
			String syncDesc;
			if (element.isPrimitive || element.isEnum) {
				Type typeToSync = element.isPrimitive ? element.getTypeToSync() : Type.getType(Enum.class);
				syncDesc = Type.getMethodDescriptor(packetBuilder, packetBuilder, object, syncTypeType, Type.INT_TYPE, typeToSync, typeToSync);
			} else {
				syncDesc = Type.getMethodDescriptor(packetBuilder, packetBuilder, object, syncTypeType, Type.INT_TYPE, Type.getType(TypeSyncer.class), object, object);
			}

			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new FieldInsnNode(Opcodes.GETSTATIC, syncTypeType.getInternalName(), type.name(), syncTypeType.getDescriptor()));
			insns.add(new IntInsnNode(element.index <= Byte.MAX_VALUE ? Opcodes.BIPUSH : Opcodes.SIPUSH, element.index));

			if (!element.isPrimitive && !element.isEnum) {
				if (!element.syncerStatic) {
					insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
				}
				insns.add(new FieldInsnNode(element.syncerStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD, clazz.name, element.syncerField.name, element.syncerField.desc));
			}

			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			if (element.isMethod) {
				insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, clazz.name, element.method.name, element.method.desc));
			} else {
				insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, element.field.name, element.field.desc));
			}
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, element.companion.name, element.companion.desc));
			insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, "sync", syncDesc));

			// copy the field over to the companion
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new InsnNode(Opcodes.DUP));
			if (element.isMethod) {
				insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, clazz.name, element.method.name, element.method.desc));
			} else {
				insns.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, element.field.name, element.field.desc));
			}
			insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, element.companion.name, element.companion.desc));
		}

		String endSyncDesc;
		if (target == null) {
			endSyncDesc = Type.getMethodDescriptor(Type.VOID_TYPE, packetBuilder, object, syncTypeType);
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			insns.add(new FieldInsnNode(Opcodes.GETSTATIC, syncTypeType.getInternalName(), type.name(), syncTypeType.getDescriptor()));
		} else {
			endSyncDesc = Type.getMethodDescriptor(Type.VOID_TYPE, packetBuilder, Type.getType(PacketTarget.class));
			boolean targetStatic = (targetField.access & Opcodes.ACC_STATIC) != 0;
			if (!targetStatic) {
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			}
			insns.add(new FieldInsnNode(targetStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD, clazz.name, targetField.name, targetField.desc));
		}
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, "endSync", endSyncDesc));

		insns.add(new InsnNode(Opcodes.RETURN));

		clazz.methods.add(method);
		return method;
	}

	private static void checkIsSetter(Map<String, MethodNode> setters, MethodNode method) {
		AnnotationNode ann = ASMUtils.getAnnotation(method, Synced.Setter.class);
		if (ann != null) {
			setters.put((String) ann.values.get(1), method);
		}
	}

	private static void addToConstructors(ClassNode clazz, List<MethodNode> cnstrs, MethodNode init, MethodNode staticInit) {
		for (MethodNode cnstr : cnstrs) {
			int len = cnstr.instructions.size();
			for (int i = 0; i < len; ++i) {
				AbstractInsnNode insn = cnstr.instructions.get(i);
				if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
					InsnList call = new InsnList();
					call.add(new VarInsnNode(Opcodes.ALOAD, 0));
					call.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, clazz.name, init.name, init.desc));

					call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, clazz.name, staticInit.name, staticInit.desc));
					cnstr.instructions.insert(insn, call);
					break;
				}
			}
		}
	}

	private static List<MethodNode> findRootConstructors(ClassNode clazz) {
		List<MethodNode> cnstrs = Lists.newArrayList();
		methods:
		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>")) {
				int len = method.instructions.size();
				for (int i = 0; i < len; ++i) {
					AbstractInsnNode insn = method.instructions.get(i);
					if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
						MethodInsnNode min = (MethodInsnNode) insn;
						if (min.owner.equals(clazz.name) && min.name.equals("<init>")) {
							continue methods;
						}
					}
				}
				cnstrs.add(method);
			}
		}
		return cnstrs;
	}

	private static FieldNode createHasInitField(ClassNode clazz) {
		FieldNode isInit = new FieldNode(Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE, "_sc$syncHasInit", Type.BOOLEAN_TYPE.getDescriptor(), null, null);
		clazz.fields.add(isInit);
		return isInit;
	}

	private static boolean makeSyncedElement(ClassNode clazz, Map<Type, FieldNode> targets, ListMultimap<Type, SyncedElement> list, int index, boolean isMethod, Object fieldOrMethod, Type typeToSync, AnnotationNode syncedAnnotation) {
		if (syncedAnnotation == null) {
			return false;
		}

		String nullInternalName = Type.getInternalName(Synced.NULL.class);
		Type syncer = null;
		Type target = null;
		String setter = null;
		int len = syncedAnnotation.values == null ? 0 : syncedAnnotation.values.size();
		for (int i = 0; i < len; i += 2) {
			String key = (String) syncedAnnotation.values.get(i);
			if (key.equals("setter")) {
				setter = (String) syncedAnnotation.values.get(i + 1);
			} else {
				Type type;
				if (!(type = (Type) syncedAnnotation.values.get(i + 1)).getInternalName().equals(nullInternalName)) {
					if (key.equals("syncer")) {
						syncer = type;
					} else if (key.equals("target")) {
						target = type;
					}
				}
			}
		}
		if (target != null && !targets.containsKey(target)) {
			boolean targetStatic = hasNoArgConstructor(ASMUtils.getClassNode(target.getClassName()));
			FieldNode targetField = new FieldNode(Opcodes.ACC_PRIVATE | (targetStatic ? Opcodes.ACC_STATIC : 0), "_sc_sync_target_" + target.getInternalName().replace('/', '_'), Type.getDescriptor(PacketTarget.class), null, null);
			clazz.fields.add(targetField);
			targets.put(target, targetField);
		}

		SyncedElement element;
		if (ASMUtils.isPrimitive(typeToSync)) {
			list.put(target, (element = isMethod ? new SyncedElement((MethodNode) fieldOrMethod, setter, index) : new SyncedElement((FieldNode) fieldOrMethod, index)));
		} else {
			boolean syncerStatic = syncer == null || hasNoArgConstructor(ASMUtils.getClassNode(syncer.getClassName()));

			if (isMethod) {
				list.put(target, (element = new SyncedElement((MethodNode) fieldOrMethod, index, setter, syncerStatic, syncer)));
			} else {
				list.put(target, (element = new SyncedElement((FieldNode) fieldOrMethod, index, syncerStatic, syncer)));
			}
		}
		element.companion = new FieldNode(Opcodes.ACC_PRIVATE, "_sc$syncCompanion_" + element.getName(), typeToSync.getDescriptor(), null, null);
		clazz.fields.add(element.companion);
		return true;
	}

	private static boolean hasNoArgConstructor(ClassNode clazz) {
		String name = "<init>";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return true;
			}
		}
		return false;
	}

	private static MethodNode makeNonStaticInitMethod(ClassNode clazz, Collection<SyncedElement> fields, Map<Type, FieldNode> targets) {
		String name = "_sc$syncInit";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE, name, desc, null, null);
		InsnList insns = method.instructions;

		for (SyncedElement element : fields) {
			if (!element.isPrimitive && !element.syncerStatic) {
				name = "_sc$syncSyncer_" + element.getName();
				desc = Type.getDescriptor(TypeSyncer.class);
				element.syncerField = new FieldNode(Opcodes.ACC_PRIVATE, name, desc, null, null);
				clazz.fields.add(element.syncerField);

				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));

				String syncerName = element.syncer.getInternalName();
				insns.add(new TypeInsnNode(Opcodes.NEW, syncerName));
				insns.add(new InsnNode(Opcodes.DUP));
				insns.add(new VarInsnNode(Opcodes.ALOAD, 0));

				name = "<init>";
				desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class));
				insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, syncerName, name, desc));

				insns.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name, element.syncerField.name, element.syncerField.desc));
			}
		}

		for (Map.Entry<Type, FieldNode> entry : targets.entrySet()) {
			initTarget(clazz, insns, entry.getKey(), entry.getValue(), false);
		}

		insns.add(new InsnNode(Opcodes.RETURN));
		clazz.methods.add(method);
		return method;
	}

	private static MethodNode makeStaticInitMethod(ClassNode clazz, Collection<SyncedElement> elements, Map<Type, FieldNode> targets, FieldNode isInit) {
		String name = "_sc$syncStaticInit";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE);
		MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name, desc, null, null);
		InsnList insns = method.instructions;

		insns.add(new FieldInsnNode(Opcodes.GETSTATIC, clazz.name, isInit.name, isInit.desc));
		LabelNode lbl = new LabelNode();
		insns.add(new JumpInsnNode(Opcodes.IFEQ, lbl));
		insns.add(new InsnNode(Opcodes.RETURN));

		insns.add(lbl);
		insns.add(new InsnNode(Opcodes.ICONST_1));
		insns.add(new FieldInsnNode(Opcodes.PUTSTATIC, clazz.name, isInit.name, isInit.desc));

		for (SyncedElement element : elements) {
			if (!element.isPrimitive && element.syncerStatic) {
				name = "_sc$syncSyncer_" + element.getName();
				desc = Type.getDescriptor(TypeSyncer.class);
				element.syncerField = new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name, desc, null, null);
				clazz.fields.add(element.syncerField);

				if (element.syncer == null) {
					insns.add(new LdcInsnNode(element.getTypeToSync()));
					name = "getSyncerFor";
					desc = Type.getMethodDescriptor(Type.getType(TypeSyncer.class), Type.getType(Class.class));
					insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, syncAsmHooks, name, desc));
				} else {
					insns.add(new TypeInsnNode(Opcodes.NEW, element.syncer.getInternalName()));
					insns.add(new InsnNode(Opcodes.DUP));
					insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, element.syncer.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE)));
				}
				insns.add(new FieldInsnNode(Opcodes.PUTSTATIC, clazz.name, element.syncerField.name, element.syncerField.desc));
			}
		}

		for (Map.Entry<Type, FieldNode> entry : targets.entrySet()) {
			initTarget(clazz, insns, entry.getKey(), entry.getValue(), true);
		}

		insns.add(new InsnNode(Opcodes.RETURN));
		clazz.methods.add(method);
		return method;
	}

	private static void initTarget(ClassNode clazz, InsnList insns, Type targetType, FieldNode targetField, boolean staticPass) {
		boolean targetStatic = (targetField.access & Opcodes.ACC_STATIC) != 0;
		if (targetStatic != staticPass) {
			return;
		}
		if (!targetStatic) {
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		}
		insns.add(new TypeInsnNode(Opcodes.NEW, targetType.getInternalName()));
		insns.add(new InsnNode(Opcodes.DUP));
		if (!targetStatic) {
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
		}
		String desc = targetStatic ? Type.getMethodDescriptor(Type.VOID_TYPE) : Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class));
		insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetType.getInternalName(), "<init>", desc));

		insns.add(new FieldInsnNode(targetStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD, clazz.name, targetField.name, targetField.desc));
	}

	private static class SyncedElement {

		boolean isMethod;
		FieldNode field;
		MethodNode method;
		boolean isPrimitive;
		boolean syncerStatic;
		Type syncer;
		int index;
		String setter;
		boolean isEnum;

		FieldNode syncerField;
		FieldNode companion;

		SyncedElement(FieldNode field, int index) {
			this.field = field;
			this.isPrimitive = true;
			this.isMethod = false;
			this.index = index;
		}

		SyncedElement(FieldNode field, int index, boolean syncerStatic, Type syncer) {
			this.field = field;
			this.isMethod = false;
			this.isPrimitive = false;
			this.syncerStatic = syncerStatic;
			this.syncer = syncer;
			this.index = index;
			checkEnum();
		}

		SyncedElement(MethodNode method, String setter, int index) {
			this.method = method;
			this.setter = setter;
			this.isMethod = true;
			this.isPrimitive = true;
			this.index = index;
		}

		SyncedElement(MethodNode method, int index, String setter, boolean syncerStatic, Type syncer) {
			this.method = method;
			this.isMethod = true;
			this.isPrimitive = false;
			this.syncerStatic = syncerStatic;
			this.syncer = syncer;
			this.index = index;
			this.setter = setter;
			checkEnum();
		}

		private void checkEnum() {
			isEnum = ASMUtils.isAssignableFrom(ASMUtils.getClassInfo(Enum.class), ASMUtils.getClassInfo(getTypeToSync().getClassName()));
		}

		String getName() {
			return isMethod ? method.name : field.name;
		}

		Type getTypeToSync() {
			return isMethod ? Type.getReturnType(method.desc) : Type.getType(field.desc);
		}
	}

}
