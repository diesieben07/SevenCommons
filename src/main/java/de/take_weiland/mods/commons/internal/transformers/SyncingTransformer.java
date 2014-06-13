package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.SyncASMHooks;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.internal.SyncedEntityProperties;
import de.take_weiland.mods.commons.internal.SyncedObject;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static de.take_weiland.mods.commons.asm.MCPNames.CLASS_ENTITY;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * contains black bytecode magic. Do not touch.
 */
public final class SyncingTransformer implements ASMClassTransformer {

	private static final Logger LOGGER;
	private static final ClassInfo extPropsCI = ClassInfo.of(IExtendedEntityProperties.class);
	private static final ClassInfo entityCI = ClassInfo.of("net/minecraft/entity/Entity");
	private static final ClassInfo tileEntityCI = ClassInfo.of("net/minecraft/tileentity/TileEntity");
	private static final ClassInfo containerCI = ClassInfo.of("net/minecraft/inventory/Container");


	static {
		FMLLog.makeLog("SevenCommonsSync");
		LOGGER = Logger.getLogger("SevenCommonsSync");
	}

	@Override
	public boolean transforms(String internalName) {
		return !internalName.startsWith("net/minecraft/")
				&& !internalName.startsWith("net/minecraftforge/")
				&& !internalName.startsWith("cpw/mods/fml/")
				&& !internalName.startsWith("org/apache/");
	}

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (classInfo.isInterface() || classInfo.isEnum()) {
			return false;
		}

		if (!ASMUtils.hasAnnotationOnAnything(clazz, Sync.class)) {
			return false;
		}

		SyncType type = null;
		if (entityCI.isAssignableFrom(classInfo)) {
			type = SyncType.ENTITY;
		} else if (tileEntityCI.isAssignableFrom(classInfo)) {
			type = SyncType.TILE_ENTITY;
		} else if (containerCI.isAssignableFrom(classInfo)) {
			type = SyncType.CONTAINER;
		}

		if (extPropsCI.isAssignableFrom(classInfo)) {
			if (type == null) {
				type = SyncType.ENTITY_PROPS;
			} else {
				throw new IllegalStateException("Cannot sync on IExtendedEntityProperties class which extends Entity, TileEntity or Container!");
			}
		}

		if (type == null) {
			throw new IllegalStateException(String.format("Can't sync class %s", clazz.name));
		}

		List<ASMVariable> variables = ASMVariables.allWith(clazz, Sync.class, CodePieces.getThis());
		List<SyncedElement> elements = Lists.newArrayListWithCapacity(variables.size());

		Map<Type, SyncPacketTarget> knownPacketTargets = Maps.newHashMap();
		Map<Type, Syncer> knownSyncers = Maps.newHashMap();

		for (ASMVariable var : variables) {
			AnnotationNode syncedAnnotation = var.getterAnnotation(Sync.class);
			Type packetTargetType = ASMUtils.getAnnotationProperty(syncedAnnotation, "target");
			SyncPacketTarget packetTarget;
			if (packetTargetType == null) {
				packetTarget = new PacketTargetDefault(type);
			} else if (!knownPacketTargets.containsKey(packetTargetType)) {
				CodePiece packetTargetInstance = obtainInstance(clazz, packetTargetType);
				packetTarget = new PacketTargetCustom(packetTargetInstance);
				knownPacketTargets.put(packetTargetType, packetTarget);
			} else {
				packetTarget = knownPacketTargets.get(packetTargetType);
			}
			Type syncerType = ASMUtils.getAnnotationProperty(syncedAnnotation, "syncer");
			Syncer syncer;
			if (syncerType == null) {
				syncer = Syncer.forType(var.getType());
			} else if (!knownSyncers.containsKey(syncerType)) {
				CodePiece syncerInstance = obtainInstance(clazz, syncerType);
				syncer = new CustomSyncer(syncerInstance, var.getType());
				knownSyncers.put(syncerType, syncer);
			} else {
				syncer = knownSyncers.get(syncerType);
			}

			elements.add(new SyncedElement(var, makeCompanion(clazz, var), packetTarget, syncer));
		}

		// checks if any superclass of this class already has @Sync properties
		int superSyncCount = countSuperSyncs(clazz);

		// only need to add the SyncedEntityProperties interface if superclass has not already done it
		if (type == SyncType.ENTITY_PROPS && superSyncCount > 0) {
			addEntityPropertyStuff(clazz);
		}

		// this checks if the virtual context ("this") matches the static context.
		// this is needed so that only the class furthest down in the hierarchy chain calls the sync method from the tick method
		ASMCondition isActualClass = makeActualClassCheck(clazz);

		// create the method that actually syncs the data
		MethodNode syncMethod = createSyncMethod(clazz, elements, type, superSyncCount);

		// create the method that reads the data back from the packet
		createReadMethod(clazz, superSyncCount, elements);

		// call the sync method from the tick method
		makeSyncCall(clazz, syncMethod, type, superSyncCount > 0, isActualClass);

		clazz.interfaces.add(Type.getInternalName(SyncedObject.class));
		return true;
	}

	private static MethodNode addElementCountGetter(ClassNode clazz, boolean isSuperSynced, int elements) {
		MethodNode method = new MethodNode(ACC_PROTECTED, "_sc$syncElementCount", ASMUtils.getMethodDescriptor(int.class), null, null);
		clazz.methods.add(method);
		if (!isSuperSynced) {
			CodePieces.constant(elements).append(CodePieces.ofOpcode(IRETURN)).appendTo(method.instructions);
		} else {
			CodePieces.invokeSuper(clazz, method)
					.append(CodePieces.constant(elements))
					.append(CodePieces.ofOpcode(IADD))
					.append(CodePieces.ofOpcode(IRETURN))
					.appendTo(method.instructions);
		}

		return method;
	}

	private static ASMCondition makeActualClassCheck(ClassNode clazz) {
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_FINAL | ACC_TRANSIENT, "_sc$thisIsSameClass", Type.BOOLEAN_TYPE.getDescriptor(), null, null);
		clazz.fields.add(field);

		CodePiece set = CodePieces.setField(clazz, field,
				CodePieces.getThis(),
				Conditions.ifEqual(
					CodePieces.constant(Type.getObjectType(clazz.name)),
					CodePieces.invoke(INVOKEVIRTUAL, "java/lang/Object", "getClass", ASMUtils.getMethodDescriptor(Class.class), CodePieces.getThis()), getType(Class.class), false)
				.then(CodePieces.constant(true))
				.otherwise(CodePieces.constant(false))
				.build());

		ASMUtils.initialize(clazz, set);
		return Conditions.ifTrue(CodePieces.getField(clazz, field, CodePieces.getThis()));
	}


	private static void addEntityPropertyStuff(ClassNode clazz) {
		// tick method is handled by makeSyncCall

		Type entityType = getObjectType(CLASS_ENTITY);
		Type stringType = getType(String.class);

		FieldNode ownerField = new FieldNode(ACC_PRIVATE, "_sc$syncPropsOwner", entityType.getDescriptor(), null, null);
		clazz.fields.add(ownerField);

		MethodNode ownerGetter = new MethodNode(ACC_PUBLIC, SyncedEntityProperties.GET_ENTITY, getMethodDescriptor(entityType), null, null);
		clazz.methods.add(ownerGetter);
		CodePieces.getField(clazz, ownerField, CodePieces.getThis()).appendTo(ownerGetter.instructions);
		ownerGetter.instructions.add(new InsnNode(ARETURN));

		FieldNode identField = new FieldNode(ACC_PRIVATE, "_sc$syncPropsIdent", stringType.getDescriptor(), null, null);
		clazz.fields.add(identField);

		MethodNode identGetter = new MethodNode(ACC_PUBLIC, SyncedEntityProperties.GET_IDENTIFIER, getMethodDescriptor(stringType), null, null);
		clazz.methods.add(identGetter);
		CodePieces.getField(clazz, identField, CodePieces.getThis()).appendTo(identGetter.instructions);
		identGetter.instructions.add(new InsnNode(ARETURN));

		MethodNode method = new MethodNode(ACC_PUBLIC, SyncedEntityProperties.INJECT_DATA, getMethodDescriptor(VOID_TYPE, entityType, stringType), null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		CodePieces.setField(clazz, ownerField, CodePieces.getThis(), CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(insns);
		CodePieces.setField(clazz, identField, CodePieces.getThis(), CodePieces.of(new VarInsnNode(ALOAD, 2))).appendTo(insns);

		insns.add(new InsnNode(RETURN));

		clazz.interfaces.add(SyncedEntityProperties.CLASS_NAME);
	}

	private static void makeSyncCall(ClassNode clazz, MethodNode syncMethod, SyncType type, boolean isSuperSynced, ASMCondition isActualClass) {
		String name = type.getTickMethod();
		MethodNode tickMethod = ASMUtils.findMethod(clazz, name);
		if (tickMethod == null) {
			tickMethod = new MethodNode(ACC_PUBLIC, name, getMethodDescriptor(VOID_TYPE), null, null);
			// tick method for ENTITY_PROPS only exist if class is Synced, so only call super tick method, if a superclass is actually synced
			if (type != SyncType.ENTITY_PROPS || isSuperSynced) {
				CodePieces.invokeSuper(clazz, tickMethod).appendTo(tickMethod.instructions);
			}
			tickMethod.instructions.add(new InsnNode(RETURN));
			clazz.methods.add(tickMethod);
		}
		isActualClass.then(CodePieces.invoke(clazz, syncMethod, CodePieces.getThis(), CodePieces.constantNull(), CodePieces.constant(true))
							.append(CodePieces.ofOpcode(POP)))
				.build()
				.prependTo(tickMethod.instructions);
	}

	private static int countSuperSyncs(ClassNode clazz) {
		int count = 0;
		while (clazz != null && !"java/lang/Object".equals(clazz.superName)) {
			clazz = ASMUtils.getThinClassNode(clazz.superName);
			for (MethodNode method : clazz.methods) {
				if (ASMUtils.hasAnnotation(method, Sync.class)) ++count;
			}
			for (FieldNode field : clazz.fields) {
				if (ASMUtils.hasAnnotation(field, Sync.class)) ++count;
			}
		}
		return count;
	}

	private static void createReadMethod(ClassNode clazz, int superSyncCount, List<SyncedElement> elements) {
		MethodNode method = new MethodNode(ACC_PUBLIC, SyncedObject.READ, ASMUtils.getMethodDescriptor(void.class, DataBuf.class), null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		LabelNode start = new LabelNode();
		insns.add(start);
		CodePieces.invokeStatic(SyncASMHooks.CLASS_NAME, SyncASMHooks.READ_INDEX, ASMUtils.getMethodDescriptor(int.class, DataBuf.class),
				CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(insns);

		Conditions.ifEqual(CodePieces.ofOpcode(DUP), CodePieces.constant(-1), Type.INT_TYPE)
				.then(CodePieces.ofOpcode(POP).append(CodePieces.ofOpcode(RETURN)))
				.build()
				.appendTo(insns);

		CodePieces.constant(-superSyncCount).appendTo(insns);
		insns.add(new InsnNode(IADD));

		LabelNode[] jumpLabels = new LabelNode[elements.size()];
		for (int i = 0, len = jumpLabels.length; i < len; ++i) {
			jumpLabels[i] = new LabelNode();
		}
		LabelNode dflt = new LabelNode();

		insns.add(new TableSwitchInsnNode(0, elements.size() - 1, dflt, jumpLabels));
		for (int i = 0, len = elements.size(); i < len; ++i) {
			SyncedElement element = elements.get(i);
			insns.add(jumpLabels[i]);
			element.variable.set(element.syncer.read(element.variable.get(), CodePieces.of(new VarInsnNode(ALOAD, 1))))
					.appendTo(insns);
			insns.add(new JumpInsnNode(GOTO, start));
		}
		insns.add(dflt);
		// TODO: this is a problem, produce some sort of warning
		CodePieces.invoke(INVOKEVIRTUAL, "java/io/PrintStream", "println", ASMUtils.getMethodDescriptor(void.class, Object.class),
				CodePieces.of(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", Type.getDescriptor(PrintStream.class))),
				CodePieces.constant("Invalid Index!"))
				.appendTo(insns);
		insns.add(new JumpInsnNode(GOTO, start));
	}

	private static MethodNode createSyncMethod(ClassNode clazz, List<SyncedElement> elements, SyncType type, int superSyncCount) {
		Type packetBuilderType = getType(PacketBuilder.class);
		MethodNode syncMethod = new MethodNode(ACC_PROTECTED, "_sc$doSync", Type.getMethodDescriptor(packetBuilderType, packetBuilderType, Type.BOOLEAN_TYPE), null, null);
		clazz.methods.add(syncMethod);
		InsnList insns = syncMethod.instructions;

		int packetBuilderVar = 3;
		CodePiece initialBuilderValue;
		if (superSyncCount > 0) {
			initialBuilderValue = CodePieces.invokeSuper(clazz, syncMethod, CodePieces.of(new VarInsnNode(ALOAD, 1)), CodePieces.constant(false));
		} else {
			initialBuilderValue = CodePieces.constantNull();
		}

		initialBuilderValue.append(CodePieces.of(new VarInsnNode(ASTORE, packetBuilderVar))).appendTo(insns);

		CodePiece packetBuilderDirect = CodePieces.of(new VarInsnNode(ALOAD, packetBuilderVar));
		CodePiece packetBuilderCache = Conditions.ifNull(packetBuilderDirect)
				.then(CodePieces.invokeStatic(SyncASMHooks.CLASS_NAME, SyncASMHooks.CREATE_BUILDER,
						ASMUtils.getMethodDescriptor(PacketBuilder.class, Object.class, SyncType.class),
						CodePieces.getThis(), CodePieces.constant(type))
						.append(CodePieces.of(new VarInsnNode(ASTORE, packetBuilderVar))))
				.build()
				.append(packetBuilderDirect);

		for (int i = 0, len = elements.size(); i < len; i++) {
			SyncedElement element = elements.get(i);
			CodePiece writeIndex = writeIndex(packetBuilderCache, i + superSyncCount);

			CodePiece writeData = element.syncer.write(element.variable.get(), packetBuilderDirect);
			CodePiece updateCompanion = element.companion.set(element.variable.get());

			element.syncer.equals(element.companion.get(), element.variable.get())
					.negate()
					.then(writeIndex.append(writeData).append(updateCompanion))
					.build().appendTo(insns);

		}

		Conditions.ifNull(packetBuilderDirect)
				.otherwise(Conditions.ifTrue(CodePieces.of(new VarInsnNode(ILOAD, 2)))
						.then(writeIndex(packetBuilderDirect, -1)
								.append(CodePieces.invokeStatic(SyncASMHooks.CLASS_NAME, SyncASMHooks.SEND_FINISHED,
										ASMUtils.getMethodDescriptor(void.class, Object.class, SyncType.class, PacketBuilder.class),
										CodePieces.getThis(), CodePieces.constant(type), packetBuilderDirect)))
						.build())
				.build()
				.appendTo(insns);

		packetBuilderDirect.appendTo(insns);
		insns.add(new InsnNode(ARETURN));

		return syncMethod;
	}

	private static CodePiece writeIndex(CodePiece packetBuilder, int index) {
		return CodePieces.invokeStatic(
				SyncASMHooks.CLASS_NAME, SyncASMHooks.WRITE_INDEX,
				Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(PacketBuilder.class), Type.INT_TYPE),
				packetBuilder, CodePieces.constant(index));
	}

	private static CodePiece obtainInstance(ClassNode clazz, Type packetTargetType) {
		ClassNode targetClazz = ASMUtils.getThinClassNode(packetTargetType.getInternalName());

		boolean canBeStatic = false;

		CodePiece instanceLoader = CodePieces.obtainInstance(clazz, targetClazz,
				new Type[]{ Type.getObjectType(clazz.name) },
				CodePieces.getThis());

		if (instanceLoader == null) {
			instanceLoader = CodePieces.obtainInstance(clazz, targetClazz,
					new Type[] { Type.getType(Object.class) },
					CodePieces.getThis());

			if (instanceLoader == null) {
				instanceLoader = CodePieces.obtainInstance(clazz, targetClazz);
				canBeStatic = true;
				if (instanceLoader == null) {
					throw new IllegalStateException(String.format("Failed to obtain instance of %s for @Synced class %s", packetTargetType.getInternalName(), clazz.name));
				}
			}
		}

		return CodePieces.cache(clazz, packetTargetType, instanceLoader, canBeStatic, false);
	}

	private static ASMVariable makeCompanion(ClassNode clazz, ASMVariable var) {
		FieldNode field = new FieldNode(ACC_PRIVATE, var.name() + "_sc$syncCompanion", var.getType().getDescriptor(), null, null);
		clazz.fields.add(field);
		return ASMVariables.of(clazz, field, CodePieces.getThis());
	}

	private static class SyncedElement {

		final ASMVariable variable;
		final ASMVariable companion;
		final SyncPacketTarget packetTarget;
		final Syncer syncer;

		SyncedElement(ASMVariable variable, ASMVariable companion, SyncPacketTarget packetTarget, Syncer syncer) {
			this.variable = variable;
			this.companion = companion;
			this.packetTarget = packetTarget;
			this.syncer = syncer;
		}
	}

	private static abstract class Syncer {

		private static final Set<Type> integratedTypes = ImmutableSet.of(
			Type.getType(String.class)
		);

		static Syncer forType(Type t) {
			if (ASMUtils.isPrimitive(t) || integratedTypes.contains(t)) {
				return new IntegratedSyncer(t);
			} else if (t.getInternalName().equals("net/minecraft/item/ItemStack")) {
				return ItemStackSyncer.instance();
			} else if (ClassInfo.of(t).isEnum()) {
				return new EnumSyncer(t);
			} else {
				throw new UnsupportedOperationException("NYI");
			}
		}

		abstract ASMCondition equals(CodePiece oldValue, CodePiece newValue);

		/**
		 * writes the value to the PacketBuilder and leaves the PacketBuilder on the stack, if last == false
		 */
		abstract CodePiece write(CodePiece newValue, CodePiece packetBuilder);

		abstract CodePiece read(CodePiece oldValue, CodePiece packetBuilder);

	}

	private static class ItemStackSyncer extends Syncer {

		private static ItemStackSyncer instance;

		static ItemStackSyncer instance() {
			return instance == null ? (instance = new ItemStackSyncer()) : instance;
		}

		@Override
		ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
			return Conditions.ifTrue(CodePieces.invokeStatic(Type.getInternalName(ItemStacks.class),
					"equal", ASMUtils.getMethodDescriptor(boolean.class, ItemStack.class, ItemStack.class),
					oldValue, newValue));
		}

		@Override
		CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
			return CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
					"writeItemStack", ASMUtils.getMethodDescriptor(void.class, WritableDataBuf.class, ItemStack.class),
					packetBuilder, newValue);
		}

		@Override
		CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
			return CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
					"readItemStack", ASMUtils.getMethodDescriptor(ItemStack.class, DataBuf.class),
					packetBuilder);
		}
	}

	private static class EnumSyncer extends Syncer {

		private final Type type;

		EnumSyncer(Type type) {
			this.type = type;
		}

		@Override
		ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
			return Conditions.ifEqual(oldValue, newValue, type, false);
		}

		@Override
		CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
			return CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
					"writeEnum",
					ASMUtils.getMethodDescriptor(void.class, WritableDataBuf.class, Enum.class),
					packetBuilder, newValue);
		}

		@Override
		CodePiece read(CodePiece oldValue, CodePiece dataBuf) {
			return CodePieces.castTo(type, CodePieces.invokeStatic(Type.getInternalName(DataBuffers.class),
					"readEnum",
					ASMUtils.getMethodDescriptor(Enum.class, DataBuf.class, Class.class),
					dataBuf, CodePieces.constant(type)));
		}
	}

	private static class IntegratedSyncer extends Syncer {

		final Type typeToSync;

		IntegratedSyncer(Type typeToSync) {
			this.typeToSync = typeToSync;
		}

		@Override
		CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
			String owner = SyncASMHooks.CLASS_NAME;
			String name = SyncASMHooks.WRITE_INTEGRATED;
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, typeToSync, Type.getType(WritableDataBuf.class));

			return CodePieces.invokeStatic(owner, name, desc, newValue, packetBuilder);
		}

		@Override
		ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
			return Conditions.ifEqual(oldValue, newValue, typeToSync, true);
		}

		@Override
		CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
			String owner = SyncASMHooks.CLASS_NAME;
			String name = String.format(SyncASMHooks.READ_PRIMITIVE, typeToSync.getClassName().replace('.', '_'));
			String desc = Type.getMethodDescriptor(typeToSync, Type.getType(DataBuf.class));

			return CodePieces.invokeStatic(owner, name, desc, packetBuilder);
		}
	}

	private static class CustomSyncer extends Syncer {

		private final CodePiece syncer;
		private final Type actualType;

		CustomSyncer(CodePiece syncer, Type actualType) {
			this.syncer = syncer;
			this.actualType = actualType;
		}

		@Override
		ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
			String owner = TypeSyncer.CLASS_NAME;
			String name = TypeSyncer.METHOD_EQUAL;
			Type objectType = Type.getType(Object.class);
			String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, objectType, objectType);

			CodePiece invoke = CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, syncer, newValue, oldValue);

			return Conditions.of(invoke, IFNE, IFEQ);
		}

		@Override
		CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
			String owner = TypeSyncer.CLASS_NAME;
			String name = TypeSyncer.METHOD_WRITE;
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(WritableDataBuf.class));

			return CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, syncer, newValue, packetBuilder);
		}

		@Override
		CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
			String owner = TypeSyncer.CLASS_NAME;
			String name = TypeSyncer.METHOD_READ;
			Type objectType = Type.getType(Object.class);
			String desc = Type.getMethodDescriptor(objectType, objectType, Type.getType(DataBuf.class));

			CodePiece invoke = CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, syncer, oldValue, packetBuilder);
			if (!ASMUtils.isPrimitive(actualType) || !actualType.equals(objectType)) {
				return CodePieces.castTo(actualType, invoke);
			} else {
				return invoke;
			}
		}
	}

	private static abstract class SyncPacketTarget {

		abstract CodePiece send(CodePiece simplePacket);

	}

	private static class PacketTargetDefault extends SyncPacketTarget {

		private final SyncType type;

		PacketTargetDefault(SyncType type) {
			this.type = type;
		}

		@Override
		CodePiece send(CodePiece simplePacket) {
			String owner = SyncASMHooks.CLASS_NAME;
			String name = String.format(SyncASMHooks.SEND_PACKET, type.getSimpleName());
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(SimplePacket.class));

			return CodePieces.invokeStatic(owner, name, desc, simplePacket);
		}
	}

	private static class PacketTargetCustom extends SyncPacketTarget {

		private final CodePiece customTarget;

		PacketTargetCustom(CodePiece customTarget) {
			this.customTarget = customTarget;
		}

		@Override
		CodePiece send(CodePiece simplePacket) {
			String owner = SimplePacket.CLASS_NAME;
			String name = SimplePacket.METHOD_SEND_TO;
			String desc = Type.getMethodDescriptor(Type.getType(SimplePacket.class), Type.getType(PacketTarget.class));

			return CodePieces.invoke(INVOKEINTERFACE, owner, name, desc, customTarget, simplePacket)
					.append(CodePieces.ofOpcode(POP));
		}
	}

}
