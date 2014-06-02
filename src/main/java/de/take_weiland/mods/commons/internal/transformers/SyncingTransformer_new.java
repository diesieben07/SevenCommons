package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.SyncASMHooks;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.internal.SyncedObject;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.objectweb.asm.Opcodes.*;

/**
 * contains black bytecode magic. Do not touch.
 */
public final class SyncingTransformer_new implements ASMClassTransformer {

	private static final Logger LOGGER;
	private static final String syncAsmHooks = "de/take_weiland/mods/commons/internal/SyncASMHooks";
	private static final ClassInfo extPropsCI = ClassInfo.of(IExtendedEntityProperties.class);
	private static final ClassInfo entityCI = ClassInfo.of("net/minecraft/entity/Entity");
	private static final ClassInfo tileEntityCI = ClassInfo.of("net/minecraft/tileentity/TileEntity");
	private static final ClassInfo containerCI = ClassInfo.of("net/minecraft/inventory/Container");
	private static final ClassInfo packetTargetCI = ClassInfo.of(PacketTarget.class);


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
		if (classInfo.isInterface() || classInfo.isAbstract() || classInfo.isEnum()) {
			return false;
		}

		if (!ASMUtils.hasAnnotationOnAnything(clazz, Sync.class)) {
			return false;
		}

		SyncType type;
		if (entityCI.isAssignableFrom(classInfo)) {
			type = SyncType.ENTITY;
		} else if (tileEntityCI.isAssignableFrom(classInfo)) {
			type = SyncType.TILE_ENTITY;
		} else if (containerCI.isAssignableFrom(classInfo)) {
			type = SyncType.CONTAINER;
		} else if (extPropsCI.isAssignableFrom(classInfo)) {
			type = SyncType.ENTITY_PROPS;
		} else {
			LOGGER.warning(String.format("Can't sync class %s, it will be ignored.", clazz.name));
			return false;
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

		createSyncMethod(clazz, elements);
		createReadMethod(clazz, elements);
		clazz.interfaces.add(Type.getInternalName(SyncedObject.class));
		return true;
	}

	private static void createReadMethod(ClassNode clazz, List<SyncedElement> elements) {
		MethodNode method = new MethodNode(ACC_PUBLIC, SyncedObject.READ, ASMUtils.getMethodDescriptor(void.class, DataBuf.class), null, null);
		clazz.methods.add(method);
		InsnList insns = method.instructions;

		LabelNode start = new LabelNode();
		insns.add(start);
		CodePieces.invokeStatic(SyncASMHooks.CLASS_NAME, SyncASMHooks.READ_INDEX, ASMUtils.getMethodDescriptor(int.class, DataBuf.class),
				CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(insns);
		LabelNode[] jumpLabels = new LabelNode[elements.size() + 1];
		for (int i = 0, len = jumpLabels.length; i < len; ++i) {
			jumpLabels[i] = new LabelNode();
		}
		LabelNode dflt = new LabelNode();

		insns.add(new TableSwitchInsnNode(-1, elements.size() - 1, dflt, jumpLabels));
		insns.add(jumpLabels[0]);
		insns.add(new InsnNode(RETURN));
		for (int i = 0, len = elements.size(); i < len; ++i) {
			SyncedElement element = elements.get(i);
			insns.add(jumpLabels[i + 1]);
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

	private static void createSyncMethod(ClassNode clazz, List<SyncedElement> elements) {
		MethodNode syncMethod = new MethodNode(ACC_PRIVATE, "_sc$doSync", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
		clazz.methods.add(syncMethod);
		InsnList insns = syncMethod.instructions;

		String desc = Type.getDescriptor(PacketBuilder.class);
		LabelNode beginning = new LabelNode();
		insns.add(beginning);
		LabelNode end = new LabelNode();

		CodePieces.LocalCached packetBuilderCache = CodePieces.cacheLocal(clazz, syncMethod,
				Type.getType(PacketBuilder.class),
				CodePieces.invokeStatic(SyncASMHooks.CLASS_NAME, SyncASMHooks.CREATE_BUILDER, Type.getMethodDescriptor(Type.getType(PacketBuilder.class))));

		for (int i = 0, len = elements.size(); i < len; i++) {
			SyncedElement element = elements.get(i);
			CodePiece writeIndex = CodePieces.invokeStatic(
					SyncASMHooks.CLASS_NAME, SyncASMHooks.WRITE_INDEX,
					ASMUtils.getMethodDescriptor(void.class, WritableDataBuf.class, int.class),
					packetBuilderCache.getValue, CodePieces.constant(i));

			CodePiece writeData = element.syncer.write(element.variable.get(), packetBuilderCache.getValue);
			CodePiece updateCompanion = element.companion.set(element.variable.get());

			element.syncer.equals(element.companion.get(), element.variable.get())
					.ifFalse(writeIndex.append(writeData).append(updateCompanion))
					.appendTo(insns);
		}
		insns.add(end);
		insns.add(new InsnNode(RETURN));

		insns.add(packetBuilderCache.subroutine);
		Iterator<AbstractInsnNode> it = insns.iterator();
		while (it.hasNext()) {
			AbstractInsnNode node = it.next();
			System.out.println(node);
		}
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

	private static class IntegratedSyncer extends Syncer {

		final Type typeToSync;

		IntegratedSyncer(Type typeToSync) {
			this.typeToSync = typeToSync;
		}

		@Override
		CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
			InsnList insns = new InsnList();

			newValue.appendTo(insns);
			packetBuilder.appendTo(insns);

			String owner = SyncASMHooks.CLASS_NAME;
			String name = SyncASMHooks.WRITE_INTEGRATED;
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, typeToSync, Type.getType(WritableDataBuf.class));

			insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

			return CodePieces.of(insns);
		}

		@Override
		ASMCondition equals(CodePiece oldValue, CodePiece newValue) {
			return CodePieces.equal(oldValue, newValue, typeToSync, true);
		}

		private static void doCompare(int compareOp, InsnList insns) {
			LabelNode notEqual = new LabelNode();
			LabelNode after = new LabelNode();
			insns.add(new InsnNode(compareOp));
			insns.add(new JumpInsnNode(IFNE, notEqual));
			insns.add(new InsnNode(ICONST_1));
			insns.add(new JumpInsnNode(GOTO, after));
			insns.add(notEqual);
			insns.add(new InsnNode(ICONST_0));
			insns.add(after);
		}

		@Override
		CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
			InsnList insns = new InsnList();

			packetBuilder.appendTo(insns);

			String owner = SyncASMHooks.CLASS_NAME;
			String name = String.format(SyncASMHooks.READ_PRIMITIVE, typeToSync.getClassName());
			String desc = Type.getMethodDescriptor(typeToSync, Type.getType(DataBuf.class));

			insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));

			return CodePieces.of(insns);
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
			InsnList insns = new InsnList();
			syncer.appendTo(insns);
			newValue.appendTo(insns);
			oldValue.appendTo(insns);

			String owner = TypeSyncer.CLASS_NAME;
			String name = TypeSyncer.METHOD_EQUAL;
			Type objectType = Type.getType(Object.class);
			String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, objectType, objectType);

			insns.add(new MethodInsnNode(INVOKEINTERFACE, owner, name, desc));
			return CodePieces.makeCondition(CodePieces.of(insns), IFNE, IFEQ);
		}

		@Override
		CodePiece write(CodePiece newValue, CodePiece packetBuilder) {
			InsnList insns = new InsnList();
			syncer.appendTo(insns);
			newValue.appendTo(insns);
			packetBuilder.appendTo(insns);

			String owner = TypeSyncer.CLASS_NAME;
			String name = TypeSyncer.METHOD_WRITE;
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(WritableDataBuf.class));

			insns.add(new MethodInsnNode(INVOKEINTERFACE, owner, name, desc));
			return CodePieces.of(insns);
		}

		@Override
		CodePiece read(CodePiece oldValue, CodePiece packetBuilder) {
			InsnList insns = new InsnList();

			syncer.appendTo(insns);
			oldValue.appendTo(insns);
			packetBuilder.appendTo(insns);

			String owner = TypeSyncer.CLASS_NAME;
			String name = TypeSyncer.METHOD_READ;
			Type objectType = Type.getType(Object.class);
			String desc = Type.getMethodDescriptor(objectType, objectType, Type.getType(DataBuf.class));

			insns.add(new MethodInsnNode(INVOKEINTERFACE, owner, name, desc));
			if (!ASMUtils.isPrimitive(actualType) || !actualType.equals(objectType)) {
				insns.add(new TypeInsnNode(CHECKCAST, actualType.getInternalName()));
			}
			return CodePieces.of(insns);
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
			InsnList insns = new InsnList();
			simplePacket.appendTo(insns);

			String owner = SyncASMHooks.CLASS_NAME;
			String name = String.format(SyncASMHooks.SEND_PACKET, type.getSimpleName());
			String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(SimplePacket.class));

			insns.add(new MethodInsnNode(INVOKESTATIC, owner, name, desc));
			return CodePieces.of(insns);
		}
	}

	private static class PacketTargetCustom extends SyncPacketTarget {

		private final CodePiece customTarget;

		PacketTargetCustom(CodePiece customTarget) {
			this.customTarget = customTarget;
		}

		@Override
		CodePiece send(CodePiece simplePacket) {
			InsnList insns = new InsnList();
			simplePacket.appendTo(insns);
			customTarget.appendTo(insns);

			String owner = SimplePacket.CLASS_NAME;
			String name = SimplePacket.METHOD_SEND_TO;
			String desc = Type.getMethodDescriptor(Type.getType(SimplePacket.class), Type.getType(PacketTarget.class));

			insns.add(new MethodInsnNode(INVOKEINTERFACE, owner, name, desc));
			insns.add(new InsnNode(POP));
			return CodePieces.of(insns);
		}
	}

}
